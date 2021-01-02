package kg.delletenebre.yamus.api.responses

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
data class TrackId(
    @Json(name = "id") val id: String = "",
    @Json(name = "albumId") val albumId: String = ""
) {
    val trackId: String get() = if (albumId.isNotEmpty()) { "$id:$albumId" } else { id }
}