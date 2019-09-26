package kg.delletenebre.yamus.api.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DownloadInfo (
    val host: String = "",
    val path: String = "",
    val ts: String = "",
    val region: String = "",
    val s: String = "",
    @SerialName("regional-hosts")
    val regionalHosts: List<String> = listOf()
)