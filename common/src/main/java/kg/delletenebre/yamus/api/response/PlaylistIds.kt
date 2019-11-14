package kg.delletenebre.yamus.api.response

import kotlinx.serialization.Serializable

@Serializable
data class PlaylistIds(val uid: Long, val kind: Int)