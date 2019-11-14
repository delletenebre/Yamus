package kg.delletenebre.yamus.api.responses

import kotlinx.serialization.Serializable

@Serializable
data class Station(val id: Id = Id(), val icon: Icon = Icon(), val name: String = "") {
    fun getId(): String {
        return "${id.type}:${id.tag}"
    }

    @Serializable
    data class Id(val tag: String = "", val type: String = "")

    @Serializable
    data class Icon(val backgroundColor: String = "#ffffff", val imageUrl: String = "")
}