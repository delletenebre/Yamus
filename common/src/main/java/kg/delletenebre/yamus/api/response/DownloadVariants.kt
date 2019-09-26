package kg.delletenebre.yamus.api.response

import kotlinx.serialization.Serializable

@Serializable
data class DownloadVariants(
    val invocationInfo: InvocationInfo,
    val result: List<Result>
) {
    @Serializable
    data class Result(
        val bitrateInKbps: Int,
        val codec: String,
        val downloadInfoUrl: String,
        val gain: Boolean,
        val preview: Boolean
    )
}