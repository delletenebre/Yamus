package kg.delletenebre.yamus.api.response

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class Artist(
        val composer: Boolean = false,
        val cover: Cover = Cover(),
        @Transient
        val decomposed: List<String> = listOf(),
        val genres: List<String> = listOf(),
        val id: Int = 0,
        val name: String = "",
        val various: Boolean = false
) {
    @Serializable
    data class Cover(
            val prefix: String = "",
            val type: String = "",
            val uri: String = ""
    )
}