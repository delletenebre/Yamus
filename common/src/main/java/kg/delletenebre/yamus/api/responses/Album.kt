package kg.delletenebre.yamus.api.responses

import kotlinx.serialization.Serializable

@Serializable
data class Album(
        val available: Boolean = false,
        val id: Long = 0,
        val title: String = "",
        val artists: List<Artist> = listOf(),
        val trackCount: Int = 0,
        val year: Long = 0,
        val coverUri: String = ""
)