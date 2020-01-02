package kg.delletenebre.yamus.api.responses

import kotlinx.serialization.Serializable

@Serializable
data class Mix(
    val backgroundImageUri: String,
    val title: String,
    val url: String,
    val urlScheme: String
)