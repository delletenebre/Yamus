package kg.delletenebre.yamus.api.response

import kotlinx.serialization.Serializable

@Serializable
data class User(
        val account: Account = Account(),
        val permissions: Permissions = Permissions(),
        val premiumRegion: Int = 0,
        val skipsPerHour: Int = 0,
        val stationExists: Boolean = false,
        val subscription: Subscription = Subscription()
) {
        @Serializable
        data class Account(
                val birthday: String = "",
                val displayName: String = "",
                val firstName: String = "",
                val fullName: String = "",
                val hostedUser: Boolean = false,
                val login: String = "",
                val now: String = "",
                val secondName: String = "",
                val serviceAvailable: Boolean = false,
                val uid: Int = 0
        )

        @Serializable
        data class Permissions(
                @Transient
                val default: List<String> = listOf(),
                val until: String = "",
                val values: List<String> = listOf()
        )

        @Serializable
        data class Subscription(
                val end: String = ""
        )
}