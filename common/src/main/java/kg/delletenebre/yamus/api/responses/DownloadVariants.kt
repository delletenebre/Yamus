package kg.delletenebre.yamus.api.responses

import kotlinx.serialization.Serializable

@Serializable
data class DownloadVariants(
    val bitrateInKbps: Int,
    val codec: String,
    val downloadInfoUrl: String,
    val gain: Boolean,
    val preview: Boolean
)