package kg.delletenebre.yamus.api.response

import kotlinx.serialization.Serializable

@Serializable
data class Track(
        val id: String,
        var playlistId: String = "",
        val albums: List<Album> = listOf(),
        val artists: List<Artist> = listOf(),
        val available: Boolean = false,
        val availableForPremiumUsers: Boolean = false,
        val availableFullWithoutPermission: Boolean = false,
        val contentWarning: String = "",
        var coverUri: String = "",
        val durationMs: Long = 0,
        val fileSize: Int = 0,
        val lyricsAvailable: Boolean = false,
        val major: Major = Major(),
        val normalization: Normalization = Normalization(),
        val ogImage: String = "",
        val previewDurationMs: Int = 0,
        val realId: String = "",
        val storageDir: String = "",
        val title: String = "",
        val type: String = "",
        val version: String = ""
) {
    @Serializable
    data class Major(
            val id: Int = 0,
            val name: String = ""
    )

    @Serializable
    data class Normalization(
            val gain: Double = 0.0,
            val peak: Int = 0
    )

    fun getTrackId(): String {
        var result = id
        if (albums.isNotEmpty()) {
            result += ":${albums[0].id}"
        }
        return result
    }
}