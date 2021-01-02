package kg.delletenebre.yamus.api.responses

import com.squareup.moshi.JsonClass
import com.squareup.moshi.Json


@JsonClass(generateAdapter = true)
data class Settings(
    @Json(name = "inAppProducts")
    val inAppProducts: List<InAppProduct>,
    @Json(name = "nativeProducts")
    val nativeProducts: List<NativeProduct>,
    @Json(name = "promoCodesEnabled")
    val promoCodesEnabled: Boolean,
    @Json(name = "webPaymentUrl")
    val webPaymentUrl: String
)

@JsonClass(generateAdapter = true)
data class InAppProduct(
    @Json(name = "available")
    val available: Boolean,
    @Json(name = "commonPeriodDuration")
    val commonPeriodDuration: String,
    @Json(name = "customPayload")
    val customPayload: String,
    @Json(name = "debug")
    val debug: Boolean,
    @Json(name = "description")
    val description: String,
    @Json(name = "duration")
    val duration: Int,
    @Json(name = "feature")
    val feature: String,
    @Json(name = "features")
    val features: List<String>,
    @Json(name = "plus")
    val plus: Boolean,
    @Json(name = "price")
    val price: Price,
    @Json(name = "productId")
    val productId: String,
    @Json(name = "trialDuration")
    val trialDuration: Int,
    @Json(name = "type")
    val type: String,
    @Json(name = "vendorTrialAvailable")
    val vendorTrialAvailable: Boolean
)

@JsonClass(generateAdapter = true)
data class NativeProduct(
    @Json(name = "available")
    val available: Boolean,
    @Json(name = "buttonAdditionalText")
    val buttonAdditionalText: String,
    @Json(name = "buttonText")
    val buttonText: String,
    @Json(name = "cheapest")
    val cheapest: Boolean,
    @Json(name = "commonPeriodDuration")
    val commonPeriodDuration: String,
    @Json(name = "customPayload")
    val customPayload: String,
    @Json(name = "debug")
    val debug: Boolean,
    @Json(name = "description")
    val description: String,
    @Json(name = "duration")
    val duration: Int,
    @Json(name = "familySub")
    val familySub: Boolean,
    @Json(name = "fbImage")
    val fbImage: String,
    @Json(name = "fbName")
    val fbName: String,
    @Json(name = "feature")
    val feature: String,
    @Json(name = "features")
    val features: List<String>,
    @Json(name = "licenceTextParts")
    val licenceTextParts: List<LicenceTextPart>,
    @Json(name = "paymentMethodTypes")
    val paymentMethodTypes: List<String>,
    @Json(name = "plus")
    val plus: Boolean,
    @Json(name = "price")
    val price: PriceX,
    @Json(name = "productId")
    val productId: String,
    @Json(name = "title")
    val title: String,
    @Json(name = "trialAvailable")
    val trialAvailable: Boolean,
    @Json(name = "trialDuration")
    val trialDuration: Int,
    @Json(name = "trialPeriodDuration")
    val trialPeriodDuration: String,
    @Json(name = "type")
    val type: String
)

@JsonClass(generateAdapter = true)
data class Price(
    @Json(name = "amount")
    val amount: Double,
    @Json(name = "currency")
    val currency: String
)

@JsonClass(generateAdapter = true)
data class LicenceTextPart(
    @Json(name = "text")
    val text: String,
    @Json(name = "url")
    val url: String
)

@JsonClass(generateAdapter = true)
data class PriceX(
    @Json(name = "amount")
    val amount: Double,
    @Json(name = "currency")
    val currency: String
)