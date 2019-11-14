package kg.delletenebre.yamus.api.responses

import kotlinx.serialization.Serializable

@Serializable
data class Profile(
        val account: Account = Account(),
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
                val uid: Long = 0
        )

        @Serializable
        data class Subscription(var end: String = "")
}