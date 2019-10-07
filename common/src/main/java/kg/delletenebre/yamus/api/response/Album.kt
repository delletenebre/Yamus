package kg.delletenebre.yamus.api.response

import kotlinx.serialization.Serializable

@Serializable
data class Album(
        val artists: List<Artist> = listOf(),
        val available: Boolean = false,
        val availableForMobile: Boolean = false,
        val availableForPremiumUsers: Boolean = false,
        val availablePartially: Boolean = false,
        val bests: List<Int> = listOf(),
        val buy: List<String> = listOf(),
        val contentWarning: String = "",
        val coverUri: String = "",
        val genre: String = "",
        val id: Int = 0,
        val labels: List<Label> = listOf(),
        val ogImage: String = "",
        val recent: Boolean = false,
        val releaseDate: String = "",
        val title: String = "",
        val trackCount: Int = 0,
        val trackPosition: TrackPosition = TrackPosition(),
        val type: String = "",
        val veryImportant: Boolean = false,
        val year: Int = 0,
        val version: String = ""
) {
    @Serializable
    data class Label(
            val id: Int = 0,
            val name: String = ""
    )

    @Serializable
    data class TrackPosition(
            val index: Int = 0,
            val volume: Int = 0
    )
}