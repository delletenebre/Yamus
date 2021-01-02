package kg.delletenebre.yamus.api.responses

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
data class Station(
    @Json(name = "station") val station: StationData
) {
    @JsonClass(generateAdapter = true)
    data class StationData(
        @Json(name = "id") val id: Id,
        @Json(name = "icon") val icon: Icon,
        @Json(name = "name") val name: String
    ) {
        fun getId(): String = "${id.type}:${id.tag}"

        @JsonClass(generateAdapter = true)
        data class Id(
            @Json(name = "tag") val tag: String = "",
            @Json(name = "type") val type: String = ""
        )

        @JsonClass(generateAdapter = true)
        data class Icon(
            @Json(name = "backgroundColor") val backgroundColor: String = "#ffffff",
            @Json(name = "imageUrl") val imageUrl: String = ""
        )
    }
}