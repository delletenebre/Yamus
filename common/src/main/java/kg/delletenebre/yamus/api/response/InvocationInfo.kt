package kg.delletenebre.yamus.api.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class InvocationInfo(
    @SerialName("exec-duration-millis")
    val execDurationMillis: String = "",
    val hostname: String = "",
    @SerialName("req-id")
    val reqId: String = ""
)