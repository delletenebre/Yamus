package kg.delletenebre.yamus.api.responses

import kotlinx.serialization.Serializable

@Serializable
data class Artist(
        val id: String = "",
        val name: String = "",
        val various: Boolean = false,
        val cover: Cover = Cover(),
        val ogImage: String = "",
        val genres: List<String> = listOf(),
        val available: Boolean = true
) {
    @Serializable
    data class Cover(
            val prefix: String = "",
            val type: String = "",
            val uri: String = ""
    )
}