package kg.delletenebre.yamus.api.response

import kotlinx.serialization.Serializable

@Serializable
data class Playlist(
    val available: Boolean = false,
    val backgroundColor: String = "",
    val collective: Boolean = false,
    val cover: Cover = Cover(),
    val created: String = "",
    val description: String = "",
    val descriptionFormatted: String = "",
    val durationMs: Int = 0,
    val image: String = "",
    val isBanner: Boolean = false,
    val isPremiere: Boolean = false,
    val kind: Int = 0,
    val likesCount: Int = 0,
    val modified: String = "",
    val ogImage: String = "",
    val owner: Owner = Owner(),
    @Transient
    val prerolls: List<String> = listOf(),
    val revision: Int = 0,
    val snapshot: Int = 0,
    val tags: List<Tag> = listOf(),
    val textColor: String = "",
    val title: String = "",
    val trackCount: Int = 0,
    val uid: Int = 0,
    val visibility: String = ""
) {
    @Serializable
    data class Cover(
        val custom: Boolean = false,
        val dir: String = "",
        val type: String = "",
        val uri: String = "",
        val version: String = "",
        val itemsUri: List<String> = listOf()
    )

    @Serializable
    data class Owner(
        val login: String = "",
        val name: String = "",
        val sex: String = "",
        val uid: Int = 0,
        val verified: Boolean = false
    )

    @Serializable
    data class Tag(
        val id: String = "",
        val value: String = ""
    )
}