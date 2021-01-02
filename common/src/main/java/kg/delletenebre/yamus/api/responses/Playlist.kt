package kg.delletenebre.yamus.api.responses

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
data class Playlist(
    @Json(name = "available") val available: Boolean = true,
    @Json(name = "durationMs") val durationMs: Long = 0,
    @Json(name = "kind") val kind: Long = 0,
    @Json(name = "modified") val modified: String = "",
    @Json(name = "ogImage") val ogImage: String = "",
    @Json(name = "revision") val revision: Int = 0,
    @Json(name = "title") val title: String = "",
    @Json(name = "trackCount") val trackCount: Int = 0,
    @Json(name = "uid") val uid: Long = 0
)