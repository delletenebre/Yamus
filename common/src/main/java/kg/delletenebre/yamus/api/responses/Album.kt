package kg.delletenebre.yamus.api.responses

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
data class Album(
    @Json(name = "artists") val artists: List<Artist> = listOf(),
    @Json(name = "available") val available: Boolean = true,
    @Json(name = "coverUri") val coverUri: String = "",
    @Json(name = "id") val id: Long = 0,
    @Json(name = "title") val title: String = "",
    @Json(name = "trackCount") val trackCount: Int = 0,
    @Json(name = "year") val year: Long = 0

)