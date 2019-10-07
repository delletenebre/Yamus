package kg.delletenebre.yamus.api

import android.content.Context
import com.tonyodev.fetch2.*
import com.tonyodev.fetch2core.Downloader
import com.tonyodev.fetch2okhttp.OkHttpDownloader
import kg.delletenebre.yamus.App
import kg.delletenebre.yamus.api.database.table.TrackEntity
import kg.delletenebre.yamus.api.response.Track
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import org.apache.commons.io.FileUtils
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import org.jaudiotagger.tag.images.ArtworkFactory
import java.io.File
import kotlin.math.ln
import kotlin.math.pow


object YandexCache {
    const val MUSIC_FILE_CONTAINER = ".mp3"
//    const val FETCH_NAMESPACE = "YandexCache"

    lateinit var TRACKS_DIR: File
    lateinit var CACHE_DIR: File
    lateinit var fetch: Fetch

    fun init(context: Context) {
        TRACKS_DIR = context.getExternalFilesDir("tracks")!!
        CACHE_DIR = context.externalCacheDir!!

        val fetchConfiguration = FetchConfiguration.Builder(context)
                .setDownloadConcurrentLimit(1)
                .setHttpDownloader(OkHttpDownloader(Downloader.FileDownloaderType.PARALLEL))
//                .setNamespace(FETCH_NAMESPACE)
//                .setNotificationManager(object : DefaultFetchNotificationManager(context) {
//                    override fun getFetchInstanceForNamespace(namespace: String): Fetch {
//                        return fetch
//                    }
//                })
                .build()
        fetch = Fetch.Impl.getInstance(fetchConfiguration)
        fetch.removeAll()
    }

    fun getTrackFile(track: Track): File {
        return File(TRACKS_DIR, track.realId + MUSIC_FILE_CONTAINER)
    }

    fun getTrackPathOrNull(track: Track): String? {
        val file = getTrackFile(track)
        if (file.exists()) {
            return file.absolutePath
        }

        return null
    }

    suspend fun downloadTracks(tracks: List<Track>) {
        saveTracksToDatabase(tracks)
        tracks.forEach {
            downloadTrack(it, false)
        }
    }

    suspend fun downloadTrack(track: Track, saveToDatabase: Boolean = true) {
        if (saveToDatabase) {
            saveTrackToDatabase(track)
        }
        withContext(Dispatchers.IO) {
            val file = getTrackFile(track)
            if (file.exists()) {
                file.delete()
            }
            val url = YandexMusic.getDirectUrl(track.realId, false)
            val request = Request(url, file.absolutePath)
            request.priority = Priority.HIGH
            request.networkType = NetworkType.ALL
            request.identifier = track.realId.toLong()
            fetch.enqueue(request)
        }
    }

    suspend fun saveTracksToDatabase(tracks: List<Track>) {
        withContext(Dispatchers.IO) {
            val entities = tracks.map {
                val data = Json.stringify(Track.serializer(), it)
                TrackEntity(it.id, data, System.currentTimeMillis())
            }
            YandexApi.database.trackDao().insert(entities)
        }
    }

    suspend fun saveTrackToDatabase(track: Track) {
        withContext(Dispatchers.IO) {
            val data = Json.stringify(Track.serializer(), track)
            val entity = TrackEntity(track.id, data, System.currentTimeMillis())
            YandexApi.database.trackDao().insert(entity)
        }
    }

    fun downloadedTracksCount(): Int {
        var filesCount = 0
        val tracksFolder = File(TRACKS_DIR.toURI())
        if (tracksFolder.exists()) {
            filesCount = tracksFolder.listFiles()?.size ?: 0
        }
        return filesCount
    }

    fun downloadedTracksSize(): String {
        val tracksFolder = File(TRACKS_DIR.toURI())
        if (tracksFolder.exists()) {
            return humanReadableFileSize(FileUtils.sizeOfDirectory(tracksFolder), false)
        }
        return "0 B"
    }

    fun clear() {
        File(TRACKS_DIR.toURI()).deleteRecursively()
    }

    fun addID3Tags(track: Track) {
        if (App.instance.getStringPreference("cache_quality").endsWith("mp3")) {
            GlobalScope.launch {
                withContext(Dispatchers.IO) {
                    try {
                        val file = getTrackFile(track)
                        if (file.exists()) {
                            val audioFile = AudioFileIO.read(file)
                            val tag = audioFile.tagOrCreateAndSetDefault
                            tag.setField(FieldKey.ARTIST, track.artists[0].name)
                            tag.setField(FieldKey.TITLE, track.title)
                            tag.setField(FieldKey.ALBUM, track.albums[0].title)
                            tag.setField(FieldKey.YEAR, track.albums[0].year.toString())

                            val artUrl = YandexApi.getImageUrl(track.coverUri, 400)
                            val artFile = File(CACHE_DIR, "${track.realId}.jpg")
                            val request = Request.Builder().url(artUrl).build()
                            val response = OkHttpClient().newCall(request).execute()
                            if (response.isSuccessful) {
                                val inputStream = response.body()!!.byteStream()
                                FileUtils.copyInputStreamToFile(inputStream, artFile)
                                FileUtils.waitFor(artFile, 5)
                                if (artFile.exists()) {
                                    val artwork = ArtworkFactory.createArtworkFromFile(artFile)
                                    tag.setField(artwork)
                                    artFile.delete()
                                }
                                audioFile.commit()
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    fun humanReadableFileSize(bytes: Long, si: Boolean = false): String {
        val unit = if (si) 1000 else 1024
        if (bytes < unit) return "$bytes B"
        val exp = (ln(bytes.toDouble()) / ln(unit.toDouble())).toInt()
        val pre = (if (si) "kMGTPE" else "KMGTPE")[exp - 1] + if (si) "" else "i"
        return String.format("%.1f %sB", bytes / unit.toDouble().pow(exp.toDouble()), pre)
    }
}