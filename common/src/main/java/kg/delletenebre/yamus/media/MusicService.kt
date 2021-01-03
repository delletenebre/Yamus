/*
 * Copyright 2017 Google Inc. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package kg.delletenebre.yamus.media

import android.app.Notification
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat.MediaItem
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.media.MediaBrowserServiceCompat
import androidx.media.session.MediaButtonReceiver
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import com.google.android.exoplayer2.ui.PlayerNotificationManager
import com.google.android.exoplayer2.util.NotificationUtil
import kg.delletenebre.yamus.App
import kg.delletenebre.yamus.GlideApp
import kg.delletenebre.yamus.api.YandexApi
import kg.delletenebre.yamus.media.actions.*
import kg.delletenebre.yamus.media.extensions.fullDescription
import kg.delletenebre.yamus.media.extensions.id
import kg.delletenebre.yamus.media.extensions.uniqueId
import kg.delletenebre.yamus.media.library.CurrentPlaylist
import kg.delletenebre.yamus.media.library.MediaLibrary
import kotlinx.coroutines.*
import java.util.*


open class MusicService : MediaBrowserServiceCompat() {
    companion object {
        private const val CHANNEL_ID: String = "yamus_playback_channel"
        private const val NOTIFICATION_ID: Int = 1
    }

    private lateinit var becomingNoisyReceiver: BecomingNoisyReceiver
    private lateinit var playerNotificationManager: PlayerNotificationManager
    private lateinit var packageValidator: PackageValidator

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    protected lateinit var mediaSession: MediaSessionCompat
    protected lateinit var mediaController: MediaControllerCompat
    protected lateinit var mediaSessionConnector: MediaSessionConnector


    private var isForegroundService = false

    private val audioAttributes = AudioAttributes.Builder()
        .setContentType(C.CONTENT_TYPE_MUSIC)
        .setUsage(C.USAGE_MEDIA)
        .build()

    private val exoPlayer: ExoPlayer by lazy {
        ExoPlayerFactory.newSimpleInstance(this).apply {
            setAudioAttributes(this@MusicService.audioAttributes, true)
            addListener(object : Player.EventListener {
                override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                    super.onPlayerStateChanged(playWhenReady, playbackState)

                    val state = if (playWhenReady && playbackState == Player.STATE_READY) {
                        // Active playback.
                        "STATE_PLAYING"
                    } else if (playWhenReady) {
                        // Not playing because playback ended, the player is buffering, stopped or
                        // failed. Check playbackState and player.getPlaybackError for details.
                        if (playbackState == Player.STATE_BUFFERING) {
                            "STATE_BUFFERING"
                        } else {
                            ""
                        }
                    } else {
                        // Paused by app.
                        "STATE_PAUSED"
                    }
//                    val track = CurrentPlaylist.track.value
//                    track?.playState = state
//                    CurrentPlaylist.track.postValue(track)
                    CurrentPlaylist.playbackState.postValue(state)
                }

                override fun onTracksChanged(trackGroups: TrackGroupArray?, trackSelections: TrackSelectionArray?) {
                    super.onTracksChanged(trackGroups, trackSelections)
                    val track = CurrentPlaylist.tracks.getOrNull(currentWindowIndex)
                    //CurrentPlaylist.currentTrack.postValue(track)
//                    CurrentPlaylist.track.postValue(CurrentPlaylist.Track.fromMetadata(track))
                }

                override fun onPositionDiscontinuity(reason: Int) {
                    super.onPositionDiscontinuity(reason)
                    if (reason == Player.DISCONTINUITY_REASON_PERIOD_TRANSITION
                            || reason == Player.DISCONTINUITY_REASON_SEEK_ADJUSTMENT) {
                        if (CurrentPlaylist.type == CurrentPlaylist.TYPE_STATION) {
                            CurrentPlaylist.loading = true
                            val stationId = CurrentPlaylist.id
                            serviceScope.launch {
                                if (reason == Player.DISCONTINUITY_REASON_SEEK_ADJUSTMENT) {
                                    val trackId = CurrentPlaylist.tracks[0].uniqueId
                                    YandexApi.getStationFeedback(
                                            stationId,
                                            YandexApi.StationEvent.skip,
                                            CurrentPlaylist.batchId,
                                            trackId,
                                            60
                                    )
                                }

                                val trackId = CurrentPlaylist.tracks[1].uniqueId
                                YandexApi.getStationFeedback(
                                        stationId,
                                        YandexApi.StationEvent.trackStarted,
                                        CurrentPlaylist.batchId,
                                        trackId
                                )
                                CurrentPlaylist.removeTrack(0)

                                if (CurrentPlaylist.tracks.size <= 3) {
                                    val queue = CurrentPlaylist.tracks[1].id
                                    val (_, tracks) = YandexApi.getStationTracks(stationId, queue)
                                    CurrentPlaylist.addTracksToPlaylist(tracks)
                                }
                                CurrentPlaylist.loading = false
                            }
                        }
                    }
                }
            })
        }
    }

    @ExperimentalCoroutinesApi
    override fun onCreate() {
        super.onCreate()

        // Build a PendingIntent that can be used to launch the UI.
        val sessionActivityPendingIntent =
            packageManager?.getLaunchIntentForPackage(packageName)?.let { sessionIntent ->
                PendingIntent.getActivity(this, 0, sessionIntent, 0)
            }

        mediaSession = MediaSessionCompat(this, "MusicService")
            .apply {
                setSessionActivity(sessionActivityPendingIntent)
                isActive = true
            }
        sessionToken = mediaSession.sessionToken

        // Because ExoPlayer will manage the MediaSession, add the service as a callback for
        // state changes.
        mediaController = MediaControllerCompat(this, mediaSession)

        becomingNoisyReceiver =
            BecomingNoisyReceiver(context = this, sessionToken = mediaSession.sessionToken)

        initializePlayerNotificationManager()

        // ExoPlayer will manage the MediaSession for us.
        mediaSessionConnector = MediaSessionConnector(mediaSession).also { connector ->
            connector.setPlayer(exoPlayer)
            connector.setMediaButtonEventHandler(YamusMediaButtonEventHandler())
            connector.setPlaybackPreparer(YamusPlaybackPreparer(exoPlayer))
            connector.setQueueNavigator(YamusQueueNavigator(mediaSession))
            val alterPositionLike = App.instance.getBooleanPreference("like_alter_position")
            val alterPositionDislike = App.instance.getBooleanPreference("dislike_alter_position")
            when {
                alterPositionLike and alterPositionDislike -> {
                    connector.setCustomActionProviders(
                            FavoriteActionProvider(this),
                            DislikeActionProvider(this),
                            PrevActionProvider(this),
                            NextActionProvider(this),
                            RepeatModeActionProvider(this),
                            ShuffleModeActionProvider(this)
                    )
                }
                alterPositionLike -> {
                    connector.setCustomActionProviders(
                            FavoriteActionProvider(this),
                            NextActionProvider(this),
                            PrevActionProvider(this),
                            DislikeActionProvider(this),
                            RepeatModeActionProvider(this),
                            ShuffleModeActionProvider(this)
                    )
                }
                alterPositionDislike -> {
                    connector.setCustomActionProviders(
                            PrevActionProvider(this),
                            DislikeActionProvider(this),
                            FavoriteActionProvider(this),
                            NextActionProvider(this),
                            RepeatModeActionProvider(this),
                            ShuffleModeActionProvider(this)
                    )
                }
                else -> {
                    connector.setCustomActionProviders(
                            PrevActionProvider(this),
                            NextActionProvider(this),
                            FavoriteActionProvider(this),
                            DislikeActionProvider(this),
                            RepeatModeActionProvider(this),
                            ShuffleModeActionProvider(this)
                    )
                }
            }
        }

        packageValidator = PackageValidator(this, R.xml.allowed_media_browser_callers)
    }

    /**
     * This is the code that causes Yamus to stop playing when swiping it away from recents.
     * The choice to do this is app specific. Some apps stop playback, while others allow playback
     * to continue and allow uses to stop it with the notification.
     */
    override fun onTaskRemoved(rootIntent: Intent) {
        super.onTaskRemoved(rootIntent)
        exoPlayer.stop(true)
    }

    override fun onDestroy() {
        mediaSession.run {
            isActive = false
            release()
        }
        playerNotificationManager.setPlayer(null)
        exoPlayer.release()

        serviceJob.cancel()
    }

    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot? {
        val isKnownCaller = packageValidator.isKnownCaller(clientPackageName, clientUid)
        val rootExtras = Bundle().apply {
            putBoolean(
                MediaLibrary.MEDIA_SEARCH_SUPPORTED,
                isKnownCaller || MediaLibrary.SEARCHABLE_BY_UNKNOWN_CALLER
            )
            putBoolean(MediaLibrary.CONTENT_STYLE_SUPPORTED, true)
            putInt(MediaLibrary.CONTENT_STYLE_BROWSABLE_HINT, MediaLibrary.CONTENT_STYLE_LIST)
            putInt(MediaLibrary.CONTENT_STYLE_PLAYABLE_HINT, MediaLibrary.CONTENT_STYLE_LIST)
        }

        return if (isKnownCaller) {
            BrowserRoot(MediaLibrary.PATH_ROOT, rootExtras)
        } else {
            BrowserRoot(MediaLibrary.PATH_EMPTY, rootExtras)
        }
    }

    override fun onLoadChildren(
        path: String,
        result: Result<List<MediaItem>>
    ) {
        result.detach()
        serviceScope.launch {
            result.sendResult(MediaLibrary.getFolder(path))
        }
    }

    /**
     * Returns a list of [MediaItem]s that match the given search query
     */
    override fun onSearch(
        query: String,
        extras: Bundle?,
        result: Result<List<MediaItem>>
    ) {
        Toast.makeText(App.instance.applicationContext, "onSearch: $query", Toast.LENGTH_LONG).show()
        Log.d("ahoha", "query: $query")
//        serviceScope.launch {
//            result.sendResult(MediaLibrary.getItems(query))
//        }

        result.detach()
    }

    private fun initializePlayerNotificationManager() {
        val context = this
        NotificationUtil.createNotificationChannel(context, CHANNEL_ID,
                R.string.playback_channel_name, R.string.playback_channel_description,
                NotificationUtil.IMPORTANCE_LOW)
        playerNotificationManager = YamusPlayerNotificationManager(context,
                CHANNEL_ID, NOTIFICATION_ID,
                DescriptionAdapter(),
                YamusNotificationListener(),
                NotificationCustomActionReceiver()
        )
        playerNotificationManager.setUseChronometer(true)
        playerNotificationManager.setSmallIcon(R.drawable.ic_notification)
        playerNotificationManager.setMediaSessionToken(sessionToken)
        playerNotificationManager.setPlayer(exoPlayer)
    }

    private class YamusPlayerNotificationManager(
            context: Context, 
            channelId: String, 
            notificationId: Int,
            mediaDescriptionAdapter: MediaDescriptionAdapter,
            notificationListener: NotificationListener,
            customActionReceiver: CustomActionReceiver
    ) : PlayerNotificationManager(
            context,
            channelId,
            notificationId,
            mediaDescriptionAdapter,
            notificationListener,
            customActionReceiver
    ) {
        override fun getActionIndicesForCompactView(actionNames: MutableList<String>, player: Player): IntArray {
            val pauseActionIndex = actionNames.indexOf(ACTION_PAUSE)
            val playActionIndex = actionNames.indexOf(ACTION_PLAY)
            val skipPreviousActionIndex = actionNames.indexOf(CustomActionsHelper.CUSTOM_ACTION_PREV)
            val nextActionName = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                CustomActionsHelper.CUSTOM_ACTION_NEXT
            } else {
                ACTION_NEXT
            }
            val skipNextActionIndex = actionNames.indexOf(nextActionName)

            val actions = mutableListOf<Int>()
            if (skipPreviousActionIndex != -1) {
                actions.add(skipPreviousActionIndex)
            }
            val playWhenReady = player.playWhenReady
            if (pauseActionIndex != -1 && playWhenReady) {
                actions.add(pauseActionIndex)
            } else if (playActionIndex != -1 && !playWhenReady) {
                actions.add(playActionIndex)
            }
            if (skipNextActionIndex != -1) {
                actions.add(skipNextActionIndex)
            }

            return actions.toIntArray()
        }

        override fun getActions(player: Player): MutableList<String> {
            var enablePrevious = false
            var enableNext = false
            val timeline = player.currentTimeline
            if (!timeline.isEmpty && !player.isPlayingAd) {
                val window = Timeline.Window()
                timeline.getWindow(player.currentWindowIndex, window)
                enablePrevious = window.isSeekable || !window.isDynamic || player.hasPrevious()
                enableNext = window.isDynamic || player.hasNext()
            }

            val stringActions = ArrayList<String>()

            val currentTrack = CurrentPlaylist.tracks.getOrNull(player.currentWindowIndex)
            if (currentTrack != null) {
                if (YandexApi.getLikedTracksIds().contains(currentTrack.uniqueId)) {
                    stringActions.add(CustomActionsHelper.CUSTOM_ACTION_UNLIKE)
                } else {
                    stringActions.add(CustomActionsHelper.CUSTOM_ACTION_LIKE)
                }

                if (enablePrevious) {
                    stringActions.add(CustomActionsHelper.CUSTOM_ACTION_PREV)
                }

                if (isPlaying(player)) {
                    stringActions.add(ACTION_PAUSE)
                } else {
                    stringActions.add(ACTION_PLAY)
                }

                if (enableNext) {
                    stringActions.add(CustomActionsHelper.CUSTOM_ACTION_NEXT)
                }

                stringActions.add(CustomActionsHelper.CUSTOM_ACTION_DISLIKE)
            }

            return stringActions
        }

        private fun isPlaying(player: Player): Boolean {
            return (player.playbackState != Player.STATE_ENDED
                    && player.playbackState != Player.STATE_IDLE
                    && player.playWhenReady)
        }
    }

    private inner class DescriptionAdapter : PlayerNotificationManager.MediaDescriptionAdapter {
        override fun getCurrentContentTitle(player: Player): String {
            return getDescription(player.currentWindowIndex)?.title.toString()
        }

        override fun getCurrentContentText(player: Player): String? {
            return getDescription(player.currentWindowIndex)?.subtitle.toString()
        }

        override fun getCurrentLargeIcon(player: Player,
                                         callback: PlayerNotificationManager.BitmapCallback?): Bitmap? {
            GlideApp.with(baseContext)
                    .asBitmap()
                    .placeholder(R.drawable.default_album_art)
                    .load(getDescription(player.currentWindowIndex)?.iconUri)
                    .into(object : CustomTarget<Bitmap>(){
                        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                            callback?.onBitmap(resource)
                        }
                        override fun onLoadCleared(placeholder: Drawable?) {
                            callback?.onBitmap((placeholder as BitmapDrawable).bitmap)
                        }
                    })
            return null
        }

        override fun createCurrentContentIntent(player: Player): PendingIntent? {
            return mediaController.sessionActivity
        }

        private fun getDescription(index: Int): MediaDescriptionCompat? {
            return CurrentPlaylist.tracks.getOrNull(index)?.fullDescription
        }
    }

    inner class YamusNotificationListener : PlayerNotificationManager.NotificationListener {
        override fun onNotificationPosted(notificationId: Int, notification: Notification?, ongoing: Boolean) {
            if (ongoing) {
                becomingNoisyReceiver.register()
                if (!isForegroundService) {
                    ContextCompat.startForegroundService(
                            applicationContext,
                            Intent(applicationContext, this@MusicService.javaClass)
                    )
                    startForeground(notificationId, notification)
                    isForegroundService = true
                }
            } else {
                becomingNoisyReceiver.unregister()
                if (isForegroundService) {
                    stopForeground(notification == null)
                    isForegroundService = false
                }
            }
        }

        override fun onNotificationCancelled(notificationId: Int, dismissedByUser: Boolean) {
            super.onNotificationCancelled(notificationId, dismissedByUser)
            becomingNoisyReceiver.unregister()
            stopForeground(true)
            isForegroundService = false
        }
    }

    private inner class NotificationCustomActionReceiver : PlayerNotificationManager.CustomActionReceiver {
        private val skipToPreviousAction = NotificationCompat.Action(
                R.drawable.exo_controls_previous,
                getString(R.string.playback_action_skip_to_previous),
                MediaButtonReceiver.buildMediaButtonPendingIntent(this@MusicService, PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS))
        private val skipToNextAction = NotificationCompat.Action(
                R.drawable.exo_controls_next,
                getString(R.string.playback_action_skip_to_next),
                MediaButtonReceiver.buildMediaButtonPendingIntent(this@MusicService, PlaybackStateCompat.ACTION_SKIP_TO_NEXT))
        private val likeAction = NotificationCompat.Action(
                R.drawable.ic_favorite_border,
                getString(R.string.playback_action_like),
                buildPendingIntent(CustomActionsHelper.CUSTOM_ACTION_LIKE))
        private val unlikeAction = NotificationCompat.Action(
                R.drawable.ic_favorite,
                getString(R.string.playback_action_unlike),
                buildPendingIntent(CustomActionsHelper.CUSTOM_ACTION_UNLIKE))
        private val dislikeAction = NotificationCompat.Action(
                R.drawable.ic_thumb_down,
                getString(R.string.playback_action_dislike),
                buildPendingIntent(CustomActionsHelper.CUSTOM_ACTION_DISLIKE))

        override fun getCustomActions(player: Player): MutableList<String> {
            val customActions = mutableListOf<String>()
            customActions.add(CustomActionsHelper.CUSTOM_ACTION_PREV)
            customActions.add(CustomActionsHelper.CUSTOM_ACTION_NEXT)
            customActions.add(CustomActionsHelper.CUSTOM_ACTION_LIKE)
            customActions.add(CustomActionsHelper.CUSTOM_ACTION_UNLIKE)
            customActions.add(CustomActionsHelper.CUSTOM_ACTION_DISLIKE)
            return customActions
        }

        override fun createCustomActions(context: Context, instanceId: Int)
                : MutableMap<String, NotificationCompat.Action> {
            val actions = mutableMapOf<String, NotificationCompat.Action>()
            actions[CustomActionsHelper.CUSTOM_ACTION_PREV] = skipToPreviousAction
            actions[CustomActionsHelper.CUSTOM_ACTION_NEXT] = skipToNextAction
            actions[CustomActionsHelper.CUSTOM_ACTION_LIKE] = likeAction
            actions[CustomActionsHelper.CUSTOM_ACTION_UNLIKE] = unlikeAction
            actions[CustomActionsHelper.CUSTOM_ACTION_DISLIKE] = dislikeAction
            return actions
        }

        override fun onCustomAction(player: Player, action: String, intent: Intent) {
            Log.d("ahoha", "customAction: $action")
            val track = CurrentPlaylist.tracks.getOrNull(player.currentWindowIndex)
            if (track != null) {
                when (action) {
                    CustomActionsHelper.CUSTOM_ACTION_LIKE ->
                        CustomActionsHelper.like(playerNotificationManager, track.uniqueId)
                    CustomActionsHelper.CUSTOM_ACTION_UNLIKE ->
                        CustomActionsHelper.unlike(playerNotificationManager, track.uniqueId)
                    CustomActionsHelper.CUSTOM_ACTION_DISLIKE ->
                        CustomActionsHelper.dislike(player, track.uniqueId)
                }
            }
        }

        private fun buildPendingIntent(action: String, instanceId: Int = Random().nextInt()): PendingIntent {
            val intent = Intent(action).setPackage(packageName)
            return PendingIntent.getBroadcast(
                    this@MusicService, instanceId, intent, PendingIntent.FLAG_CANCEL_CURRENT)
        }
    }
}

/**
 * Helper class for listening for when headphones are unplugged (or the audio
 * will otherwise cause playback to become "noisy").
 */
private class BecomingNoisyReceiver(
    private val context: Context,
    sessionToken: MediaSessionCompat.Token
) : BroadcastReceiver() {

    private val noisyIntentFilter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
    private val controller = MediaControllerCompat(context, sessionToken)

    private var registered = false

    fun register() {
        if (!registered) {
            context.registerReceiver(this, noisyIntentFilter)
            registered = true
        }
    }

    fun unregister() {
        if (registered) {
            context.unregisterReceiver(this)
            registered = false
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
            controller.transportControls.pause()
        }
    }
}

/*
 * (Media) Session events
 */
const val NETWORK_FAILURE = "kg.delletenebre.yamus.media.session.NETWORK_FAILURE"