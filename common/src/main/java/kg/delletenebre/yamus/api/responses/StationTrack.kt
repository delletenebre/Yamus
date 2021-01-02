package kg.delletenebre.yamus.api.responses

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
data class StationTrack(
    @Json(name = "liked") val liked: Boolean = false,
    @Json(name = "track") val track: Track = Track(),
    @Json(name = "type") val type: String = ""
)