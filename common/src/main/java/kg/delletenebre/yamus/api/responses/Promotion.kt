package kg.delletenebre.yamus.api.responses

import com.squareup.moshi.JsonClass
import com.squareup.moshi.Json


@JsonClass(generateAdapter = true)
data class Promotion(
    @Json(name = "albums") val albums: List<WrappedAlbum> = listOf(),
    //@Json(name = "background") val background: String = "",
    //@Json(name = "category") val category: String = "",
    //@Json(name = "description") val description: String = "",
    //@Json(name = "heading") val heading: String = "",
    //@Json(name = "imagePosition") val imagePosition: String = "",
    @Json(name = "playlists") val playlists: List<WrappedPlaylist> = listOf(),
    //@Json(name = "promoId") val promoId: String = "",
    @Json(name = "promotionType") val promotionType: String = ""
    //@Json(name = "startDate") val startDate: String = "",
    //@Json(name = "subtitle") val subtitle: String = "",
    //@Json(name = "subtitleUrl") val subtitleUrl: String = "",
    //@Json(name = "title") val title: String = "",
    //@Json(name = "titleUrl") val titleUrl: String = ""
)