package kg.delletenebre.yamus.api.responses

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
data class BlockPersonalPlaylists(
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
            @Json(name = "data") val playlist: Playlist
        )
    }
}

