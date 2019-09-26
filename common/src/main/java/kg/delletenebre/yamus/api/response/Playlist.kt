package kg.delletenebre.yamus.api.response

import kotlinx.serialization.Serializable

@Serializable
data class Playlist(
    val available: Boolean = false,
    val backgroundColor: String = "",
    val collective: Boolean = false,
    val cover: Cover,
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
    val owner: Owner,
    @Transient
    val prerolls: List<String> = listOf(),
    val revision: Int = 0,
    val snapshot: Int = 0,
    val tags: List<Tag>,
    val textColor: String = "",
    val title: String,
    val trackCount: Int = 0,
    val uid: Int = 0,
    val visibility: String
) {
    @Serializable
    data class Cover(
        val custom: Boolean,
        val dir: String,
        val type: String,
        val uri: String,
        val version: String
    )

    @Serializable
    data class Owner(
        val login: String,
        val name: String,
        val sex: String,
        val uid: Int,
        val verified: Boolean
    )

    @Serializable
    data class Tag(
        val id: String,
        val value: String
    )
}