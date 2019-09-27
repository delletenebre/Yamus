package kg.delletenebre.yamus.api.response


import kotlinx.serialization.Serializable

@Serializable
data class StationTracks(
    val batchId: String = "",
    val id: Id = Id(),
    val pumpkin: Boolean = false,
    val sequence: List<Sequence> = listOf()
) {
    @Serializable
    data class Id(
        val tag: String = "",
        val type: String = ""
    )

    @Serializable
    data class Sequence(
        val liked: Boolean,
        val track: Track,
        val type: String
    )
}