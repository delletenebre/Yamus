package kg.delletenebre.yamus.api.response

import kotlinx.serialization.Serializable

@Serializable
data class PlaylistIds(val uid: Int, val kind: Int)