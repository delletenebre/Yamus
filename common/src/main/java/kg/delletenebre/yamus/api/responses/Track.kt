package kg.delletenebre.yamus.api.responses

import kotlinx.serialization.Serializable

@Serializable
data class Track(
        val id: String = "null",
        val albums: List<Album> = listOf(),
        val artists: List<Artist> = listOf(),
        val available: Boolean = false,
        val contentWarning: String = "",
        var coverUri: String = "",
        val durationMs: Long = 0,
        val fileSize: Int = 0,
        val ogImage: String = "",
        val title: String = "",
        val error: String = ""
) {
    companion object {
        const val DOWNLOAD_STATUS_PROGRESS = "progress"
        const val DOWNLOAD_STATUS_DOWNLOADED = "downloaded"
        const val DOWNLOAD_STATUS_ERROR = "error"
    }
    var downloadStatus = ""
    var downloadProgress = 0
    var playingState: String = ""


    fun getUniqueId(): String {
        var result = id
        if (albums.isNotEmpty()) {
            result += ":${albums[0].id}"
        }
        return result
    }

    fun getArtistName(): String {
        val names = artists.map {
            it.name
        }
        return names.joinToString(", ")
    }
}