package kg.delletenebre.yamus.api.responses

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
data class PlaylistId(
    @Json(name = "kind") val kind: Int,
    @Json(name = "uid") val uid: Long
)