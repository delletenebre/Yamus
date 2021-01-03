package kg.delletenebre.yamus.api.responses

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
data class UserTracksLibrary(
    @Json(name = "revision") val revision: Int = 0,
    @Json(name = "tracks") val tracks: List<TrackId> = listOf()
)