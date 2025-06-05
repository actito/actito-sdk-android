package com.actito.loyalty.internal.network.push

import com.actito.loyalty.models.ActitoPass
import com.actito.utilities.moshi.UseDefaultsWhenNull
import com.squareup.moshi.JsonClass
import java.util.Date

@JsonClass(generateAdapter = true)
internal data class FetchPassResponse(
    val pass: Pass,
) {
    @UseDefaultsWhenNull
    @JsonClass(generateAdapter = true)
    internal data class Pass(
        val _id: String,
        val version: Int,
        val passbook: String?,
        val template: String?,
        val barcode: String,
        val serial: String,
        val redeem: ActitoPass.Redeem,
        val redeemHistory: List<ActitoPass.Redemption>,
        val limit: Int,
        val token: String,
        val data: Map<String, Any>?,
        val date: Date,
    )
}

@JsonClass(generateAdapter = true)
internal data class FetchPassbookTemplateResponse(
    val passbook: Passbook,
) {

    @UseDefaultsWhenNull
    @JsonClass(generateAdapter = true)
    internal data class Passbook(
        val passStyle: ActitoPass.PassType,
    )
}

@JsonClass(generateAdapter = true)
internal data class FetchSaveLinksResponse(
    val saveLinks: SaveLinks?,
) {

    @JsonClass(generateAdapter = true)
    internal data class SaveLinks(
        val googlePay: String?,
    )
}

@JsonClass(generateAdapter = true)
internal data class FetchUpdatedSerialsResponse(
    val serialNumbers: List<String>,
    val lastUpdated: String,
)
