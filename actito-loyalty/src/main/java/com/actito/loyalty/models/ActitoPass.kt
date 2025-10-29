package com.actito.loyalty.models

import android.os.Parcelable
import com.actito.Actito
import com.actito.internal.moshi
import com.actito.internal.parcelize.ActitoExtraParceler
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.WriteWith
import org.json.JSONObject
import java.util.Date

@Parcelize
@JsonClass(generateAdapter = true)
public data class ActitoPass(
    val id: String,
    val type: PassType?,
    val version: Int,
    val passbook: String?,
    val template: String?,
    val serial: String,
    val barcode: String,
    val redeem: Redeem,
    val redeemHistory: List<Redemption>,
    val limit: Int,
    val token: String,
    val data: @WriteWith<ActitoExtraParceler> Map<String, Any> = mapOf(),
    val date: Date,
    val googlePaySaveLink: String?,
) : Parcelable {

    public fun toJson(): JSONObject {
        val jsonStr = adapter.toJson(this)
        return JSONObject(jsonStr)
    }

    public companion object {
        private val adapter = Actito.moshi.adapter(ActitoPass::class.java)

        public fun fromJson(json: JSONObject): ActitoPass {
            val jsonStr = json.toString()
            return requireNotNull(adapter.fromJson(jsonStr))
        }
    }

    @Parcelize
    @JsonClass(generateAdapter = false)
    public enum class Redeem : Parcelable {
        @Json(name = "once")
        ONCE,

        @Json(name = "limit")
        LIMIT,

        @Json(name = "always")
        ALWAYS,
    }

    @Parcelize
    @JsonClass(generateAdapter = false)
    public enum class PassType : Parcelable {
        @Json(name = "boarding")
        BOARDING,

        @Json(name = "coupon")
        COUPON,

        @Json(name = "ticket")
        TICKET,

        @Json(name = "generic")
        GENERIC,

        @Json(name = "card")
        CARD,
    }

    @Parcelize
    @JsonClass(generateAdapter = true)
    public data class Redemption(
        val comments: String?,
        val date: Date,
    ) : Parcelable
}
