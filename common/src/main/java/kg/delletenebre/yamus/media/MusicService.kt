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

import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.support.v4.media.MediaBrowserCompat.MediaItem
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaControllerCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.media.MediaBrowserServiceCompat
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.audio.AudioAttributes
import com.google.android.exoplayer2.ext.mediasession.MediaSessionConnector
import kg.delletenebre.yamus.api.YandexMusic
import kg.delletenebre.yamus.media.actions.DislikeActionProvider
import kg.delletenebre.yamus.media.actions.FavoriteActionProvider
import kg.delletenebre.yamus.media.library.AndroidAutoBrowser
import kg.delletenebre.yamus.media.library.CurrentPlaylist
import kotlinx.coroutines.*

open class MusicService : MediaBrowserServiceCompat() {
    private lateinit var becomingNoisyReceiver: BecomingNoisyReceiver
    private lateinit var notificationManager: NotificationManagerCompat
    private lateinit var notificationBuilder: NotificationBuilder
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

    /**
     * Configure ExoPlayer to handle audio focus for us.
     * See [Player.AudioComponent.setAudioAttributes] for details.
     */
    private val exoPlayer: ExoPlayer by lazy {
        ExoPlayerFactory.newSimpleInstance(this).apply {
            setAudioAttributes(this@MusicService.audioAttributes, true)
            addListener(object : Player.EventListener {
//                override fun onTracksChanged(trackGroups: TrackGroupArray?, trackSelections: TrackSelectionArray?) {}
//                override fun onLoadingChanged(isLoading: Boolean) {}
//                override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {}
//                override fun onRepeatModeChanged(repeatMode: Int) {}
//                override fun onPlayerError(error: ExoPlaybackException?) {}
//                override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters?) {}
                override fun onPositionDiscontinuity(reason: Int) {
                    if (reason == Player.DISCONTINUITY_REASON_PERIOD_TRANSITION
                            || reason == Player.DISCONTINUITY_REASON_SEEK_ADJUSTMENT) {
                        if (CurrentPlaylist.type == CurrentPlaylist.TYPE_STATION) {
                            val stationId = CurrentPlaylist.id
                            serviceScope.launch {
                                val trackId = CurrentPlaylist.tracks[1].getTrackId()
                                YandexMusic.getStationFeedback(
                                        stationId,
                                        YandexMusic.STATION_FEEDBACK_TYPE_TRACK_STARTED,
                                        CurrentPlaylist.batchId,
                                        trackId
                                )
                            }

                            if (reason == Player.DISCONTINUITY_REASON_SEEK_ADJUSTMENT) {
                                serviceScope.launch {
                                    val trackId = CurrentPlaylist.tracks[0].getTrackId()
                                    YandexMusic.getStationFeedback(
                                            stationId,
                                            YandexMusic.STATION_FEEDBACK_TYPE_SKIP,
                                            CurrentPlaylist.batchId,
                                            trackId,
                                            60
                                    )
                                }
                            }

                            if (exoPlayer.currentWindowIndex == 2) {
                                CurrentPlaylist.removeTrack(0)
                            }

                            if (CurrentPlaylist.tracks.size == 3) {
                                serviceScope.launch {
                                    val queue = CurrentPlaylist.tracks[1].id
                                    val stationTracks = YandexMusic.getStationTracks(stationId, queue)
                                    val tracks = stationTracks.sequence.map {
                                        it.track
                                    }
                                    CurrentPlaylist.addTracksToPlaylist(tracks)
                                }
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
//
//        YandexApi.init(applicationContext)

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
        mediaController = MediaControllerCompat(this, mediaSession).also {
            it.registerCallback(MediaControllerCallback())
        }

        notificationBuilder = NotificationBuilder(this)
        notificationManager = NotificationManagerCompat.from(this)

        becomingNoisyReceiver =
            BecomingNoisyReceiver(context = this, sessionToken = mediaSession.sessionToken)

        // ExoPlayer will manage the MediaSession for us.
        mediaSessionConnector = MediaSessionConnector(mediaSession).also { connector ->
            connector.setPlayer(exoPlayer)
            connector.setPlaybackPreparer(YamusPlaybackPreparer(exoPlayer))
            connector.setQueueNavigator(YamusQueueNavigator(mediaSession))
            connector.setCustomActionProviders(
                    FavoriteActionProvider(this),
                    DislikeActionProvider(this)
            )
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

        /**
         * By stopping playback, the player will transition to [Player.STATE_IDLE]. This will
         * cause a state change in the MediaSession, and (most importantly) call
         * [MediaControllerCallback.onPlaybackStateChanged]. Because the playback state will
         * be reported as [PlaybackStateCompat.STATE_NONE], the service will first remove
         * itself as a foreground service, and will then call [stopSelf].
         */
        exoPlayer.stop(true)
    }

    override fun onDestroy() {
        mediaSession.run {
            isActive = false
            release()
        }

        // Cancel coroutines when the service is going away.
        serviceJob.cancel()
    }

    /**
     * Returns the "root" media ID that the client should request to get the list of
     * [MediaItem]s to browse/play.
     */
    override fun onGetRoot(
        clientPackageName: String,
        clientUid: Int,
        rootHints: Bundle?
    ): BrowserRoot? {
        val isKnownCaller = packageValidator.isKnownCaller(clientPackageName, clientUid)
        val rootExtras = Bundle().apply {
            putBoolean(
                AndroidAutoBrowser.MEDIA_SEARCH_SUPPORTED,
                isKnownCaller || AndroidAutoBrowser.SEARCHABLE_BY_UNKNOWN_CALLER
            )
            putBoolean(CONTENT_STYLE_SUPPORTED, true)
            putInt(CONTENT_STYLE_BROWSABLE_HINT, CONTENT_STYLE_GRID)
            putInt(CONTENT_STYLE_PLAYABLE_HINT, CONTENT_STYLE_LIST)
            //putString(CONTENT_STYLE_GROUP_TITLE_HINT, "Albums")
        }

        return if (isKnownCaller) {
            BrowserRoot(AndroidAutoBrowser.MEDIA_LIBRARY_PATH_ROOT, rootExtras)
        } else {
            BrowserRoot(AndroidAutoBrowser.MEDIA_LIBRARY_PATH_EMPTY, rootExtras)
        }
    }

    override fun onLoadChildren(
        path: String,
        result: Result<List<MediaItem>>
    ) {
        serviceScope.launch {
            result.sendResult(AndroidAutoBrowser.getItems(path))
        }

        result.detach()
    }

    /**
     * Returns a list of [MediaItem]s that match the given search query
     */
    override fun onSearch(
        query: String,
        extras: Bundle?,
        result: Result<List<MediaItem>>
    ) {
//        serviceScope.launch {
//            val children = browseTree.getItems(path).map { item ->
//                MediaItem(item.description, item.flag)
//            }
//            result.sendResult(children)
//        }

        result.detach()
    }

    /**
     * Removes the [NOW_PLAYING_NOTIFICATION] notification.
     *
     * Since `stopForeground(false)` was already called (see
     * [MediaControllerCallback.onPlaybackStateChanged], it's possible to cancel the notification
     * with `notificationManager.cancel(NOW_PLAYING_NOTIFICATION)` if minSdkVersion is >=
     * [Build.VERSION_CODES.LOLLIPOP].
     *
     * Prior to [Build.VERSION_CODES.LOLLIPOP], notifications associated with a foreground
     * service remained marked as "ongoing" even after calling [Service.stopForeground],
     * and cannot be cancelled normally.
     *
     * Fortunately, it's possible to simply call [Service.stopForeground] a second time, this
     * time with `true`. This won't change anything about the service's state, but will simply
     * remove the notification.
     */
    private fun removeNowPlayingNotification() {
        stopForeground(true)
    }

    /**
     * Class to receive callbacks about state changes to the [MediaSessionCompat]. In response
     * to those callbacks, this class:
     *
     * - Build/update the service's notification.
     * - Register/unregister a broadcast receiver for [AudioManager.ACTION_AUDIO_BECOMING_NOISY].
     * - Calls [Service.startForeground] and [Service.stopForeground].
     */
    private inner class MediaControllerCallback : MediaControllerCompat.Callback() {
        override fun onMetadataChanged(metadata: MediaMetadataCompat?) {
            mediaController.playbackState?.let { updateNotification(it) }

        }

        override fun onPlaybackStateChanged(state: PlaybackStateCompat?) {
            state?.let { updateNotification(it) }
        }

        private fun updateNotification(state: PlaybackStateCompat) {
            val updatedState = state.state

            // Skip building a notification when state is "none" and metadata is null.
            val notification = if (mediaController.metadata != null
                    && updatedState != PlaybackStateCompat.STATE_NONE) {
                notificationBuilder.buildNotification(mediaSession.sessionToken)
            } else {
                null
            }

            when (updatedState) {
                PlaybackStateCompat.STATE_BUFFERING,
                PlaybackStateCompat.STATE_PLAYING -> {
                    becomingNoisyReceiver.register()

                    /**
                     * This may look strange, but the documentation for [Service.startForeground]
                     * notes that "calling this method does *not* put the service in the started
                     * state itself, even though the name sounds like it."
                     */
                    if (notification != null) {
                        notificationManager.notify(NOW_PLAYING_NOTIFICATION, notification)

                        if (!isForegroundService) {
                            ContextCompat.startForegroundService(
                                applicationContext,
                                Intent(applicationContext, this@MusicService.javaClass)
                            )
                            startForeground(NOW_PLAYING_NOTIFICATION, notification)
                            isForegroundService = true
                        }
                    }
                }
                else -> {
                    becomingNoisyReceiver.unregister()

                    if (isForegroundService) {
                        stopForeground(false)
                        isForegroundService = false

                        // If playback has ended, also stop the service.
                        if (updatedState == PlaybackStateCompat.STATE_NONE) {
                            stopSelf()
                        }

                        if (notification != null) {
                            notificationManager.notify(NOW_PLAYING_NOTIFICATION, notification)
                        } else {
                            removeNowPlayingNotification()
                        }
                    }
                }
            }
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

/** Content styling constants */
private const val CONTENT_STYLE_GROUP_TITLE_HINT = "android.media.browse.CONTENT_STYLE_GROUP_TITLE_HINT"
private const val CONTENT_STYLE_BROWSABLE_HINT = "android.media.browse.CONTENT_STYLE_BROWSABLE_HINT"
private const val CONTENT_STYLE_PLAYABLE_HINT = "android.media.browse.CONTENT_STYLE_PLAYABLE_HINT"
private const val CONTENT_STYLE_SUPPORTED = "android.media.browse.CONTENT_STYLE_SUPPORTED"
private const val CONTENT_STYLE_LIST = 1
private const val CONTENT_STYLE_GRID = 2