package kg.delletenebre.yamus.api.response

import kotlinx.serialization.Serializable

@Serializable
data class Mix(
    val data: Data,
    val id: String,
    val type: String
) {
    @Serializable
    data class Data(
        val backgroundColor: String,
        val backgroundImageUri: String,
        val coverWhite: String,
        val textColor: String,
        val title: String,
        val url: String,
        val urlScheme: String
    )
}