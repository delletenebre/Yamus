package kg.delletenebre.yamus.api.responses

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
data class TrackDownloadInfo(
    @Json(name = "codec") val codec: String = "",
    @Json(name = "downloadInfoUrl") val downloadInfoUrl: String = "",
    @Json(name = "bitrateInKbps") val bitrateInKbps: Int = 0
)