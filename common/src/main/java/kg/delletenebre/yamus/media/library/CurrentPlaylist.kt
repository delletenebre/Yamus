package kg.delletenebre.yamus.media.library

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.support.v4.media.MediaDescriptionCompat
import android.support.v4.media.MediaMetadataCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.android.exoplayer2.upstream.FileDataSource
import com.google.android.exoplayer2.upstream.FileDataSource.FileDataSourceException
import kg.delletenebre.yamus.App
import kg.delletenebre.yamus.api.YandexApi
import kg.delletenebre.yamus.api.YandexCache
import kg.delletenebre.yamus.api.response.Track
import kg.delletenebre.yamus.media.R
import kg.delletenebre.yamus.media.datasource.YandexDataSourceFactory
import kg.delletenebre.yamus.media.extensions.*
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext


object CurrentPlaylist: CoroutineScope {
    private val job = SupervisorJob()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job

    const val TYPE_NONE = "none"
    const val TYPE_TRACKS = "tracks"
    const val TYPE_STATION = "station"
    private const val YAMUS_USER_AGENT = "Yandex-Music-API"
    private const val YAMUS_HEADER_X_YANDEX_MUSIC_CLIENT = "WindowsPhone/3.20"

    var id: String = ""
    var batchId: String = ""
    var type: String = TYPE_NONE
    var isBuffering = false

//    private val tracks_ = MutableLiveData<MutableList<Track>>().apply {
//        value = mutableListOf()
//    }
//    val tracks: LiveData<MutableList<Track>> = tracks_
    var tracks: MutableList<Track> = mutableListOf()
    var mediaSource = ConcatenatingMediaSource()
    var tracksMetadata: MutableList<MediaMetadataCompat> = mutableListOf()
    private var currentJob: Job? = null

    private val httpDataSourceFactory = YandexDataSourceFactory(YAMUS_USER_AGENT)

    init {
        httpDataSourceFactory.defaultRequestProperties
                .set("X-Yandex-Music-Client", YAMUS_HEADER_X_YANDEX_MUSIC_CLIENT)
    }

    suspend fun updatePlaylist(id: String, tracks: List<Track>, type: String = TYPE_NONE) {
        isBuffering = true
        withContext(Dispatchers.Default) {
            this@CurrentPlaylist.id = id
            this@CurrentPlaylist.type = type
            this@CurrentPlaylist.tracks = tracks.toMutableList()
            this@CurrentPlaylist.tracksMetadata = tracks.map {
                MediaMetadataCompat.Builder()
                    .from(it)
                    .apply {
                        withContext(Dispatchers.IO) {
                            albumArt = loadAlbumArt(it.coverUri)
                        }
                    }
                    .build()
            }.toMutableList()
            mediaSource.clear()
            mediaSource.addMediaSources(tracksMetadata.toMediaSource())
            currentJob = null
            isBuffering = false
        }
    }

    suspend fun addTracksToPlaylist(tracks: List<Track>) {
        this.tracks.addAll(tracks)

        val newMetadata = tracks.map {
            MediaMetadataCompat.Builder()
                    .from(it)
                    .apply {
                        albumArt = loadAlbumArt(it.coverUri)
                    }
                    .build()
        }
        tracksMetadata.addAll(newMetadata)
        mediaSource.addMediaSources(newMetadata.toMediaSource())
    }

    fun removeTrack(index: Int) {
        tracks.removeAt(index)
        tracksMetadata.removeAt(index)
        mediaSource.removeMediaSource(index)
    }

    private suspend fun loadAlbumArt(url: String): Bitmap {
        return withContext(Dispatchers.IO) {
            try {
                Glide.with(App.instance.applicationContext)
                        .applyDefaultRequestOptions(RequestOptions()
                                .fallback(R.drawable.default_album_art)
                                .diskCacheStrategy(DiskCacheStrategy.RESOURCE))
                        .asBitmap()
                        .diskCacheStrategy(DiskCacheStrategy.DATA)
                        .load(YandexApi.getImageUrl(url, AndroidAutoBrowser.NOTIFICATION_LARGE_ICON_SIZE))
                        .submit(AndroidAutoBrowser.NOTIFICATION_LARGE_ICON_SIZE, AndroidAutoBrowser.NOTIFICATION_LARGE_ICON_SIZE)
                        .get()
            } catch (t: Throwable) {
                (App.instance.applicationContext.getDrawable(R.drawable.default_album_art) as BitmapDrawable).bitmap
            }
        }
    }

    fun MediaMetadataCompat.Builder.from(track: Track): MediaMetadataCompat.Builder {
        var artistName = ""
        if (track.artists.isNotEmpty()) {
            artistName = track.artists[0].name
        }

        var albumTitle = ""
        var albumGenre = ""
        if (track.albums.isNotEmpty()) {
            val trackAlbum = track.albums[0]
            albumTitle = trackAlbum.title
            albumGenre = trackAlbum.genre

        }

        id = track.id
        title = track.title
        artist = artistName
        album = albumTitle
        duration = track.durationMs
        genre = albumGenre

        val trackMediaUri = YandexCache.getTrackPathOrNull(track) ?: track.id
        mediaUri = trackMediaUri
        albumArtUri = YandexApi.getImageUrl(track.coverUri, 400)
//        flag = MediaItem.FLAG_PLAYABLE
        explicit = if (track.contentWarning == "explicit") {
            1
        } else {
            0
        }

        displayTitle = track.title
        displaySubtitle = artistName
        displayDescription = albumTitle
        displayIconUri = YandexApi.getImageUrl(track.coverUri, 400)

        // Add downloadStatus to force the creation of an "extras" bundle in the resulting
        // MediaMetadataCompat object. This is needed to send accurate metadata to the
        // media session during updates.
        downloadStatus = if (trackMediaUri != track.id) {
            MediaDescriptionCompat.STATUS_DOWNLOADED
        } else {
            MediaDescriptionCompat.STATUS_NOT_DOWNLOADED
        }

        // Allow it to be used in the typical builder style.
        return this
    }

    private fun MediaMetadataCompat.toMediaSource(): ProgressiveMediaSource {
        val dataSourceFactory = if (mediaUri.toString().endsWith(YandexCache.MUSIC_FILE_CONTAINER)) {
            val dataSpec = DataSpec(mediaUri)
            val fileDataSource = FileDataSource()
            try {
                fileDataSource.open(dataSpec)
            } catch (e: FileDataSourceException) {
                e.printStackTrace()
            }
            DataSource.Factory { fileDataSource }
        } else {
            httpDataSourceFactory
        }
        return ProgressiveMediaSource.Factory(dataSourceFactory)
                .setTag(description)
                .createMediaSource(mediaUri)
    }

    private fun List<MediaMetadataCompat>.toMediaSource(): List<ProgressiveMediaSource> {
        return map {
            it.toMediaSource()
        }
    }
}