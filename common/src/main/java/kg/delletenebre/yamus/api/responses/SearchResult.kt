package kg.delletenebre.yamus.api.responses

import kotlinx.serialization.ContextualSerialization
import kotlinx.serialization.Serializable

@Serializable
data class SearchResult(
    val type: String = "",
    val order: Int = 0,
    val perPage: Int = 0,
    val total: Int = 0,
    @ContextualSerialization
    val result: Any?
)
