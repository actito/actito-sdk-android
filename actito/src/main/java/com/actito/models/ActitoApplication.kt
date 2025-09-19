package com.actito.models

import android.os.Parcelable
import com.actito.Actito
import com.actito.internal.moshi
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize
import org.json.JSONObject

@Parcelize
@JsonClass(generateAdapter = true)
public data class ActitoApplication(
    val id: String,
    val name: String,
    val category: String,
    val services: Map<String, Boolean>,
    val inboxConfig: InboxConfig?,
    val regionConfig: RegionConfig?,
    val userDataFields: List<UserDataField>,
    val actionCategories: List<ActionCategory>,
    val enforceSizeLimit: Boolean,
) : Parcelable {

    public fun toJson(): JSONObject {
        val jsonStr = adapter.toJson(this)
        return JSONObject(jsonStr)
    }

    public companion object {
        private val adapter = Actito.moshi.adapter(ActitoApplication::class.java)

        public fun fromJson(json: JSONObject): ActitoApplication {
            val jsonStr = json.toString()
            return requireNotNull(adapter.fromJson(jsonStr))
        }
    }

    @Parcelize
    @JsonClass(generateAdapter = true)
    public data class InboxConfig(
        val useInbox: Boolean = false,
        val useUserInbox: Boolean = false,
        val autoBadge: Boolean = false,
    ) : Parcelable {

        public fun toJson(): JSONObject {
            val jsonStr = adapter.toJson(this)
            return JSONObject(jsonStr)
        }

        public companion object {
            private val adapter = Actito.moshi.adapter(InboxConfig::class.java)

            public fun fromJson(json: JSONObject): InboxConfig {
                val jsonStr = json.toString()
                return requireNotNull(adapter.fromJson(jsonStr))
            }
        }
    }

    @Parcelize
    @JsonClass(generateAdapter = true)
    public data class RegionConfig(
        val proximityUUID: String?,
    ) : Parcelable {

        public fun toJson(): JSONObject {
            val jsonStr = adapter.toJson(this)
            return JSONObject(jsonStr)
        }

        public companion object {
            private val adapter = Actito.moshi.adapter(RegionConfig::class.java)

            public fun fromJson(json: JSONObject): RegionConfig {
                val jsonStr = json.toString()
                return requireNotNull(adapter.fromJson(jsonStr))
            }
        }
    }

    @Parcelize
    @JsonClass(generateAdapter = true)
    public data class UserDataField(
        val type: String,
        val key: String,
        val label: String,
    ) : Parcelable {

        public fun toJson(): JSONObject {
            val jsonStr = adapter.toJson(this)
            return JSONObject(jsonStr)
        }

        public companion object {
            private val adapter = Actito.moshi.adapter(UserDataField::class.java)

            public fun fromJson(json: JSONObject): UserDataField {
                val jsonStr = json.toString()
                return requireNotNull(adapter.fromJson(jsonStr))
            }
        }
    }

    @Parcelize
    @JsonClass(generateAdapter = true)
    public data class ActionCategory(
        val type: String,
        val name: String,
        val description: String?,
        val actions: List<ActitoNotification.Action>,
    ) : Parcelable {

        public fun toJson(): JSONObject {
            val jsonStr = adapter.toJson(this)
            return JSONObject(jsonStr)
        }

        public companion object {
            private val adapter = Actito.moshi.adapter(ActionCategory::class.java)

            public fun fromJson(json: JSONObject): ActionCategory {
                val jsonStr = json.toString()
                return requireNotNull(adapter.fromJson(jsonStr))
            }
        }
    }

    public object ServiceKeys {
        public const val OAUTH2: String = "oauth2"
        public const val RICH_PUSH: String = "richPush"
        public const val LOCATION_SERVICES: String = "locationServices"
        public const val APNS: String = "apns"
        public const val GCM: String = "gcm"
        public const val WEBSOCKETS: String = "websockets"
        public const val PASSBOOK: String = "passbook"
        public const val IN_APP_PURCHASE: String = "inAppPurchase"
        public const val INBOX: String = "inbox"
        public const val STORAGE: String = "storage"
    }
}
