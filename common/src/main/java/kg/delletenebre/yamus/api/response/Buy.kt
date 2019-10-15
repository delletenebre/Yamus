package kg.delletenebre.yamus.api.response

import kotlinx.serialization.Serializable

@Serializable
data class Buy(
        val link: String = "",
        val commerceModel: String = ""
)