package kg.delletenebre.yamus.api

import android.content.Context
import android.support.v4.media.MediaMetadataCompat
import android.util.Log
import com.github.kittinunf.fuel.coroutines.awaitStringResponseResult
import com.github.kittinunf.fuel.httpDownload
import kg.delletenebre.yamus.App
import kg.delletenebre.yamus.media.extensions.*
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import org.jaudiotagger.tag.images.ArtworkFactory
import java.io.File
import kotlin.math.ln
import kotlin.math.pow

object YandexCache {
    const val MUSIC_FILE_CONTAINER = ".mp3"
    const val PREFERENCE_KEY_TRACKS_DIR = "cached_tracks_path"

    private lateinit var CACHE_DIR: File
    lateinit var defaultTracksDir: File

    fun init(context: Context) {
        defaultTracksDir = context.getExternalFilesDir("tracks")!!
        CACHE_DIR = context.externalCacheDir!!
    }

    fun getTrackPathOrNull(trackId: String): String? {
        val file = getTrackFile(trackId)
        if (file.exists()) {
            return file.absolutePath
        }

        return null
    }

    suspend fun downloadTrack(track: MediaMetadataCompat, downloadProgressListener: DownloadProgressListener? = null) {
        val file = getTrackFile(track.id)
        if (file.exists()) {
            file.delete()
        }
        val url = YandexApi.getDirectUrl(track.id, false)
        val (_, _, result) = url.httpDownload()
                .fileDestination { _, _ -> file }
                .progress { readBytes, totalBytes ->
                    val progress = (readBytes / totalBytes.toDouble()) * 100
                    downloadProgressListener?.onUpdate(progress.toLong())
                }
                .awaitStringResponseResult()
        result.fold(
            {
                addID3Tags(track)
                downloadProgressListener?.onFinish()
            },
            { error ->
                Log.w("ahoha", "downloadTrack() error: ${error.response}")
                error.printStackTrace()
            }
        )
    }

    fun downloadedTracksCount(): Int {
        var filesCount = 0
        val tracksFolder = getTracksDirectory()
        if (tracksFolder.exists()) {
            filesCount = tracksFolder.listFiles()?.size ?: 0
        }
        return filesCount
    }

    fun downloadedTracksSize(): String {
        val tracksFolder = getTracksDirectory()
        if (tracksFolder.exists()) {
            return humanReadableFileSize(getFileSize(tracksFolder))
        }
        return "0 B"
    }

    fun clear(context: Context) {
        getTracksDirectory().deleteRecursively()
        //tracksDirectoryPath = context.getExternalFilesDir("tracks")!!
    }

    fun getTracksDirectory(): File {
        val folder = File(
                App.instance.getStringPreference(
                        PREFERENCE_KEY_TRACKS_DIR,
                        defaultTracksDir.absolutePath
                )
        )
        if (!folder.exists()) {
            folder.mkdirs()
        }

        return folder
    }

    private fun getTrackFile(trackId: String): File {
        return File(getTracksDirectory(), trackId + MUSIC_FILE_CONTAINER)
    }

    private fun getFileSize(file: File?): Long {
        if (file == null || !file.exists()) {
            return 0
        }
        if (!file.isDirectory) {
            return file.length()
        }
        val dirs: MutableList<File> = mutableListOf()
        dirs.add(file)
        var result: Long = 0
        while (dirs.isNotEmpty()) {
            val dir = dirs.removeAt(0)
            if (!dir.exists()) {
                continue
            }
            val listFiles = dir.listFiles()
            if (listFiles == null || listFiles.isEmpty()) {
                continue
            }
            for (child in listFiles) {
                result += child.length()
                if (child.isDirectory) {
                    dirs.add(child)
                }
            }
        }
        return result
    }

    private fun addID3Tags(track: MediaMetadataCompat) {
        if (App.instance.getStringPreference("cache_quality").endsWith("mp3")) {
            try {
                val file = getTrackFile(track.id)
                if (file.exists()) {
                    val audioFile = AudioFileIO.read(file)
                    val tag = audioFile.tagOrCreateAndSetDefault
                    tag.setField(FieldKey.ARTIST, track.artist)
                    tag.setField(FieldKey.TITLE, track.title)
                    tag.setField(FieldKey.ALBUM, track.album)
                    tag.setField(FieldKey.YEAR, track.year.toString())
                    val artUrl = track.artUri.toString()
                    val artFile = File(CACHE_DIR, "${track.id}.jpg")
                    artUrl.httpDownload()
                            .fileDestination { _, _ -> artFile }
                            .responseString { _, _, result ->
                                result.fold(
                                    {
                                        val artwork = ArtworkFactory.createArtworkFromFile(artFile)
                                        tag.setField(artwork)
                                        artFile.delete()
                                        audioFile.commit()
                                    },
                                    { error ->
                                        artFile.delete()
                                        Log.w("ahoha", "addID3Tags server error: ${error.response}")
                                    }
                                )
                            }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun humanReadableFileSize(bytes: Long): String {
        val unit = 1024
        if (bytes < unit) return "$bytes B"
        val exp = (ln(bytes.toDouble()) / ln(unit.toDouble())).toInt()
        val pre = "KMGTPE"[exp - 1] + "i"
        return String.format("%.1f %sB", bytes / unit.toDouble().pow(exp.toDouble()), pre)
    }
}
