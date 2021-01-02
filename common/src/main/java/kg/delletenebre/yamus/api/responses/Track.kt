package kg.delletenebre.yamus.api.responses

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
data class Track(
    @Json(name = "albums") val albums: List<Album> = listOf(),
    @Json(name = "artists") val artists: List<Artist> = listOf(),
    @Json(name = "available") val available: Boolean = false,
    @Json(name = "contentWarning") val contentWarning: String = "",
    @Json(name = "coverUri") var coverUri: String = "",
    @Json(name = "durationMs") val durationMs: Long = 0,
    @Json(name = "error") val error: String = "",
    @Json(name = "fileSize") val fileSize: Int = 0,
    @Json(name = "id") val id: String = "null",
    @Json(name = "ogImage") val ogImage: String = "",
    @Json(name = "title") val title: String = ""

) {
    companion object {
        const val DOWNLOAD_STATUS_PROGRESS = "progress"
        const val DOWNLOAD_STATUS_DOWNLOADED = "downloaded"
        const val DOWNLOAD_STATUS_ERROR = "error"
    }
    var downloadStatus = ""
    var downloadProgress = 0
    var playingState: String = ""


    val uniqueId: String get() {
        var result = id
        if (albums.isNotEmpty()) {
            result += ":${albums[0].id}"
        }
        return result
    }

    val artistName: String get() {
        val names = artists.map { it.name }
        return names.joinToString(", ")
    }
}