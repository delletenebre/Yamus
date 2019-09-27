package kg.delletenebre.yamus.api.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Station(
        val adParams: AdParams = AdParams(),
        val explanation: String = "",
        val settings: Settings = Settings(),
        val settings2: Settings2 = Settings2(),
        @SerialName("station")
        val data: Data = Data()
) {
    fun getId(): String {
        return "${data.id.type}:${data.id.tag}"
    }

    @Serializable
    data class AdParams(
        val adVolume: Int = 0,
        val categoryId: String = "",
        val genreId: Int = 0,
        val genreName: String = "",
        val otherParams: String = "",
        val pageRef: String = "",
        val partnerId: String = "",
        val targetRef: String = ""
    )

    @Serializable
    data class Settings(
        val diversity: String = "",
        val energy: Int = 0,
        val language: String = "",
        val mood: Int = 0
    )

    @Serializable
    data class Settings2(
        val diversity: String = "",
        val language: String = "",
        val moodEnergy: String = ""
    )

    @Serializable
    data class Data(
            val geocellIcon: GeocellIcon = GeocellIcon(),
            val icon: Icon = Icon(),
            val id: Id = Id(),
            val idForFrom: String = "",
            val mtsIcon: MtsIcon = MtsIcon(),
            val name: String = "",
            val restrictions: Restrictions = Restrictions(),
            val restrictions2: Restrictions2 = Restrictions2()
    ) {
        @Serializable
        data class GeocellIcon(
            val backgroundColor: String = "",
            val imageUrl: String = ""
        )

        @Serializable
        data class Icon(
            val backgroundColor: String = "#ffffff",
            val imageUrl: String = ""
        )

        @Serializable
        data class Id(
            val tag: String = "",
            val type: String = ""
        )

        @Serializable
        data class MtsIcon(
            val backgroundColor: String = "",
            val imageUrl: String = ""
        )

        @Serializable
        data class Restrictions(
                val diversity: Diversity = Diversity(),
                val energy: Energy = Energy(),
                val language: Language = Language(),
                val mood: Mood = Mood()
        ) {
            @Serializable
            data class Diversity(
                    val name: String = "",
                    val possibleValues: List<PossibleValue> = listOf(),
                    val type: String = ""
            )

            @Serializable
            data class Energy(
                val max: Max = Max(),
                val min: Min = Min(),
                val name: String = "",
                val type: String = ""
            )

            @Serializable
            data class Language(
                val name: String = "",
                val possibleValues: List<PossibleValue> = listOf(),
                val type: String = ""
            )

            @Serializable
            data class Mood(
                val max: Max = Max(),
                val min: Min = Min(),
                val name: String = "",
                val type: String = ""
            )
        }

        @Serializable
        data class Restrictions2(
                val diversity: Diversity = Diversity(),
                val language: Language = Language(),
                val moodEnergy: MoodEnergy = MoodEnergy()
        ) {
            @Serializable
            data class Diversity(
                val name: String = "",
                val possibleValues: List<PossibleValue> = listOf(),
                val type: String = ""
            )

            @Serializable
            data class Language(
                val name: String = "",
                val possibleValues: List<PossibleValue> = listOf(),
                val type: String = ""
            )

            @Serializable
            data class MoodEnergy(
                val name: String = "",
                val possibleValues: List<PossibleValue> = listOf(),
                val type: String = ""
            )
        }
    }

    @Serializable
    data class PossibleValue(
            val name: String = "",
            val value: String = ""
    )

    @Serializable
    data class Max(
            val name: String = "",
            val value: Int = 0
    )

    @Serializable
    data class Min(
            val name: String = "",
            val value: Int = 0
    )
}