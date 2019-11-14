package kg.delletenebre.yamus.api.response


import kotlinx.serialization.Serializable

@Serializable
data class Library(
    val revision: Int,
    val tracks: List<Track>,
    val uid: Long
) {
    @Serializable
    data class Track(
        val albumId: String = "",
        val id: String,
        val timestamp: String
    )
}