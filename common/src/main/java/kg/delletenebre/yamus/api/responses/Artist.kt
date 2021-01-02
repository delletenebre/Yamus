package kg.delletenebre.yamus.api.responses

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
data class Artist(
    @Json(name = "available") val available: Boolean = true,
    @Json(name = "cover") val cover: Cover = Cover(),
    @Json(name = "genres") val genres: List<String> = listOf(),
    @Json(name = "id") val id: String = "",
    @Json(name = "name") val name: String = "",
    @Json(name = "ogImage") val ogImage: String = "",
    @Json(name = "various") val various: Boolean = false
) {
    @JsonClass(generateAdapter = true)
    data class Cover(
        @Json(name = "prefix") val prefix: String = "",
        @Json(name = "type") val type: String = "",
        @Json(name = "uri") val uri: String = ""
    )
}