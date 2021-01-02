package kg.delletenebre.yamus.api.responses

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
data class StationQueue(
    @Json(name = "batchId") val batchId: String = "",
    @Json(name = "sequence") val sequence: List<StationTrack> = listOf()

)