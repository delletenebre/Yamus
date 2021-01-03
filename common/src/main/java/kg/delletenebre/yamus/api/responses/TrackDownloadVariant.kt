package kg.delletenebre.yamus.api.responses

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
data class TrackDownloadVariant (
    @Json(name = "host") val host: String = "",
    @Json(name = "path") val path: String = "",
    @Json(name = "ts") val ts: String = "",
    @Json(name = "region") val region: String = "",
    @Json(name = "s") val s: String = "",
    @Json(name = "regional-hosts") val regionalHosts: List<String> = listOf()
)