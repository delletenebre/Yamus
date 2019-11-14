package kg.delletenebre.yamus.api.responses

import kotlinx.serialization.Serializable

@Serializable
data class Playlist(
        val available: Boolean = false,
        val uid: Long = 0,
        val kind: Long = 0,
        val title: String = "",
        val revision: Int = 0,
        val trackCount: Int = 0,
        val durationMs: Long = 0,
        val modified: String = "",
        val ogImage: String = ""
)