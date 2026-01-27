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

/**
 * Represents a digital pass issued by Actito.
 *
 * An [ActitoPass] can be used for loyalty programs, coupons, tickets, or other
 * redeemable items. It includes metadata, redemption history, and optional
 * integrations with mobile wallets like Apple Wallet or Google Pay.
 *
 * @property id Unique identifier of the pass.
 * @property type Optional type of the pass.
 * @property version Version of the pass.
 * @property passbook Optional Apple Wallet passbook URL or identifier.
 * @property template Optional template identifier used to generate the pass.
 * @property serial Serial number of the pass.
 * @property barcode Barcode value associated with the pass.
 * @property redeem Redemption behavior of the pass.
 * @property redeemHistory History of past redemptions for this pass.
 * @property limit Maximum number of times the pass can be redeemed.
 * @property token Token associated with the pass for secure validation.
 * @property data Additional custom data associated with the pass.
 * @property date Timestamp indicating when the pass was created or issued.
 * @property googlePaySaveLink Optional link to save the pass to Google Pay.
 */
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

    /**
     * Serializes [ActitoPass] into a JSON object.
     */
    public fun toJson(): JSONObject {
        val jsonStr = adapter.toJson(this)
        return JSONObject(jsonStr)
    }

    public companion object {
        private val adapter = Actito.moshi.adapter(ActitoPass::class.java)

        /**
         * Creates an [ActitoPass] instance from a JSON object.
         *
         * @param json The JSON representation of the pass.
         * @return A parsed [ActitoPass] instance.
         * @throws IllegalArgumentException If the JSON cannot be parsed.
         */
        public fun fromJson(json: JSONObject): ActitoPass {
            val jsonStr = json.toString()
            return requireNotNull(adapter.fromJson(jsonStr))
        }
    }

    /**
     * Defines how a pass or offer can be redeemed by a user.
     *
     * This value controls whether redemption is limited or repeatable.
     */
    @Parcelize
    @JsonClass(generateAdapter = false)
    public enum class Redeem : Parcelable {
        /**
         * The pass can be redeemed only once.
         */
        @Json(name = "once")
        ONCE,

        /**
         * The pass can be redeemed a limited number of times.
         */
        @Json(name = "limit")
        LIMIT,

        /**
         * The pass can be redeemed an unlimited number of times.
         */
        @Json(name = "always")
        ALWAYS,
    }

    /**
     * Represents the type of digital pass.
     */
    @Parcelize
    @JsonClass(generateAdapter = false)
    public enum class PassType : Parcelable {
        /**
         * Boarding pass, typically used for flights or transportation.
         */
        @Json(name = "boarding")
        BOARDING,

        /**
         * Coupon pass, used for discounts or promotional offers.
         */
        @Json(name = "coupon")
        COUPON,

        /**
         * Ticket pass, such as for events or reservations.
         */
        @Json(name = "ticket")
        TICKET,

        /**
         * Generic pass with no predefined structure.
         */
        @Json(name = "generic")
        GENERIC,

        /**
         * Card-style pass, commonly used for loyalty or membership cards.
         */
        @Json(name = "card")
        CARD,
    }

    /**
     * Represents a single redemption record for an Actito pass.
     *
     * Each [Redemption] records the time and optional comments
     * when a pass was redeemed.
     *
     * @property comments Optional comments associated with the redemption.
     * @property date Timestamp when the pass was redeemed.
     */
    @Parcelize
    @JsonClass(generateAdapter = true)
    public data class Redemption(
        val comments: String?,
        val date: Date,
    ) : Parcelable
}
