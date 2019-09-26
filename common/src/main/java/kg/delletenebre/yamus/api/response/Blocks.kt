package kg.delletenebre.yamus.api.response


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Optional

@Serializable
data class Blocks(
    val invocationInfo: InvocationInfo,
    val result: Result
) {
    @Serializable
    data class Result(
        val blocks: List<Block>,
        val contentId: String,
        val pumpkin: Boolean
    ) {
        @Serializable
        data class Block(
            val data: Data,
            val description: String,
            val entities: List<Entity>,
            val id: String,
            val title: String,
            val type: String,
            val typeForFrom: String
        ) {
            @Serializable
            data class Data(
                val isWizardPassed: Boolean
            )

            @Serializable
            data class Entity(
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
        }
    }
}