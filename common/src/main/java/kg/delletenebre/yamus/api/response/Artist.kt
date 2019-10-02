package kg.delletenebre.yamus.api.response

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

@Serializable
data class Artist(
        val id: Int = 0,
        val name: String = "",
        val various: Boolean = false,
        val composer: Boolean = false,
        val cover: Cover = Cover(),
        val ogImage: String = "",
        val genres: List<String> = listOf(),
        val counts: Counts = Counts(),
        val available: Boolean = true,
        val ratings: Ratings = Ratings(),
        val links: List<SocialLink> = listOf(),
        val ticketsAvailable: Boolean = false,
        @Transient
        val decomposed: List<String> = listOf()


) {
    @Serializable
    data class Cover(
            val prefix: String = "",
            val type: String = "",
            val uri: String = ""
    )

    @Serializable
    data class Counts(
            val tracks: Int = 0,
            val directAlbums: Int = 0,
            val alsoAlbums: Int = 0,
            val alsoTracks: Int = 0
    )

    @Serializable
    data class Ratings(
            val week: Int = 0,
            val month: Int = 0,
            val day: Int = 0
    )

    @Serializable
    data class SocialLink(
            val title: String = "",
            val href: String = "",
            val type: String = ""
    )
}