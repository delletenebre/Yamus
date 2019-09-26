package kg.delletenebre.yamus.api.response

import kotlinx.serialization.Serializable

@Serializable
data class Tracks(
    val invocationInfo: InvocationInfo = InvocationInfo(),
    val result: List<Track> = listOf()
)