package kg.delletenebre.yamus.media.library

import android.support.v4.media.MediaMetadataCompat
import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.source.ConcatenatingMediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.android.exoplayer2.upstream.FileDataSource
import com.google.android.exoplayer2.upstream.FileDataSource.FileDataSourceException
import kg.delletenebre.yamus.api.YandexCache
import kg.delletenebre.yamus.media.datasource.YandexDataSourceFactory
import kg.delletenebre.yamus.media.extensions.fullDescription
import kg.delletenebre.yamus.media.extensions.id
import kg.delletenebre.yamus.media.extensions.mediaUri


object CurrentPlaylist {
    const val TYPE_NONE = "none"
    const val TYPE_TRACKS = "tracks"
    const val TYPE_STATION = "station"
    private const val YAMUS_USER_AGENT = "Yandex-Music-API"
    private const val YAMUS_HEADER_X_YANDEX_MUSIC_CLIENT = "WindowsPhone/3.20"

    var id: String = ""
    var batchId: String = ""
    var type: String = TYPE_NONE
    var loading = false
    val player = MutableLiveData<ExoPlayer?>()

    val playbackState = MutableLiveData<String>().apply {
        value = ""
    }
    val currentTrack = MutableLiveData<MediaMetadataCompat?>()
    val track = MutableLiveData<Track>()

    val mediaSource = ConcatenatingMediaSource()
    var tracks: MutableList<MediaMetadataCompat> = mutableListOf()

    private val httpDataSourceFactory = YandexDataSourceFactory(YAMUS_USER_AGENT)

    init {
        httpDataSourceFactory.defaultRequestProperties
                .set("X-Yandex-Music-Client", YAMUS_HEADER_X_YANDEX_MUSIC_CLIENT)
    }

    fun updatePlaylist(
            id: String,
            tracks: List<MediaMetadataCompat>,
            type: String = TYPE_NONE,
            batchId: String = ""
    ) {
        loading = true
        this.id = id
        this.type = type
        this.tracks.clear()
        this.tracks.addAll(tracks)
        this.batchId = batchId
        mediaSource.clear()
        mediaSource.addMediaSources(this.tracks.toMediaSource())
        loading = false
    }

    fun addTracksToPlaylist(tracks: List<MediaMetadataCompat>) {
        this.tracks.addAll(tracks)
        mediaSource.addMediaSources(tracks.toMediaSource())
    }

    fun removeTrack(index: Int) {
        tracks.removeAt(index)
        mediaSource.removeMediaSource(index)
    }

    fun updateTrack(track: MediaMetadataCompat) {
        val index = tracks.indexOfFirst { it.id == track.id }
        if (index > -1) {
            Log.d("ahoha", "updateTrack: ${track.mediaUri}")
            tracks[index] = track
            mediaSource.removeMediaSource(index)
            mediaSource.addMediaSource(index, track.toMediaSource())
        }
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
                .setTag(fullDescription)
                .createMediaSource(mediaUri)
    }

    private fun List<MediaMetadataCompat>.toMediaSource(): List<ProgressiveMediaSource> {
        return map {
            it.toMediaSource()
        }
    }

    class Track {
        var mediaId: String = ""
        var playState: String = ""
        var downloadState: String = ""

        companion object {
            fun fromMetadata(metadata: MediaMetadataCompat?): Track {
                val track = Track()
                track.mediaId = metadata?.id ?: ""
                return track
            }
        }
    }
}