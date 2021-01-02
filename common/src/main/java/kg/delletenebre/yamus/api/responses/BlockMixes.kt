package kg.delletenebre.yamus.api.responses

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
data class BlockMixes(
    @Json(name = "entities") val entities: List<Entity>
) {
    @JsonClass(generateAdapter = true)
    data class Entity(
        @Json(name = "data") val data: Data,
        @Json(name = "id") val id: String,
        @Json(name = "type") val type: String
    ) {
        @JsonClass(generateAdapter = true)
        data class Data(
            @Json(name = "backgroundImageUri") val backgroundImageUri: String,
            @Json(name = "title") val title: String,
            @Json(name = "url") val url: String,
            @Json(name = "urlScheme") val urlScheme: String
        )
    }
}

