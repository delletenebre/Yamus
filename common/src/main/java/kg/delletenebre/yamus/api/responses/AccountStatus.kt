package kg.delletenebre.yamus.api.responses

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AccountStatus(
    @Json(name = "account") val account: Account = Account(),
    @Json(name = "defaultEmail") val defaultEmail: String = "",
    @Json(name = "permissions") val permissions: Permissions = Permissions(),
    @Json(name = "plus") val plus: Plus = Plus(),
    @Json(name = "subscription") val subscription: Subscription = Subscription()
) {
    @JsonClass(generateAdapter = true)
    data class Account(
        @Json(name = "birthday") val birthday: String = "",
        @Json(name = "displayName") val displayName: String = "",
        @Json(name = "firstName") val firstName: String = "",
        @Json(name = "fullName") val fullName: String = "",
        @Json(name = "hostedUser") val hostedUser: Boolean = false,
        @Json(name = "login") val login: String = "",
        @Json(name = "now") val now: String = "",
        @Json(name = "region") val region: Int = 0,
        @Json(name = "registeredAt") val registeredAt: String = "",
        @Json(name = "secondName") val secondName: String = "",
        @Json(name = "serviceAvailable") val serviceAvailable: Boolean = false,
        @Json(name = "uid") val uid: String = ""
    )

    @JsonClass(generateAdapter = true)
    data class Permissions(
        @Json(name = "default") val default: List<String> = listOf(),
        @Json(name = "until") val until: String = "",
        @Json(name = "values") val values: List<String> = listOf()
    )

    @JsonClass(generateAdapter = true)
    data class Plus(
        @Json(name = "hasPlus") val hasPlus: Boolean = false
    )

    @JsonClass(generateAdapter = true)
    data class Subscription(
        @Json(name = "autoRenewable") val autoRenewable: List<AutoRenewable> = listOf(),
        @Json(name = "end") var end: String = ""
    ) {

        @JsonClass(generateAdapter = true)
        data class AutoRenewable(
            @Json(name = "expires") val expires: String = "",
            @Json(name = "product") val product: Product = Product(),
            @Json(name = "finished") val finished: Boolean = true
        ) {
            @JsonClass(generateAdapter = true)
            data class Product(
                @Json(name = "type") val type: String = "",
                @Json(name = "duration") val duration: Int = 0,
                @Json(name = "price") val price: Price = Price(),
                @Json(name = "plus") val plus: Boolean = false
            ) {
                @JsonClass(generateAdapter = true)
                data class Price(
                    @Json(name = "amount") val amount: String = "",
                    @Json(name = "currency") val currency: String = ""
                )
            }
        }
    }
}

