package kg.delletenebre.yamus.api.response

import kotlinx.serialization.Serializable

@Serializable
data class PlaylistTracksIds(
    val id: Int,
    val timestamp: String
)