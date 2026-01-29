package com.actito.models

import android.os.Parcelable
import com.actito.Actito
import com.actito.internal.moshi
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize
import org.json.JSONObject

/**
 * Represents an Actito application.
 *
 * An [ActitoApplication] describes the capabilities, services, and configuration
 * of an application as defined in Actito. It includes enabled services, region
 * and inbox configuration, available user data fields, and supported action categories.
 *
 * @property id Unique identifier of the application.
 * @property name Name of the application.
 * @property category Category of the application as defined in Actito.
 * @property services Map of enabled services for the application.
 * @property inboxConfig Optional inbox-related configuration.
 * @property regionConfig Optional region-related configuration.
 * @property userDataFields List of user data fields supported by the application.
 * @property actionCategories List of action categories available in the application.
 * @property enforceSizeLimit Indicates whether event payloads must respect a maximum size limit.
 * @property enforceTagRestrictions Indicates whether tag names must comply with predefined restrictions.
 * @property enforceEventNameRestrictions Indicates whether event names must comply with predefined naming rules.
 */
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
    val enforceSizeLimit: Boolean?,
    val enforceTagRestrictions: Boolean?,
    val enforceEventNameRestrictions: Boolean?,
) : Parcelable {

    /**
     * Serializes [ActitoApplication] into a JSON object.
     */
    public fun toJson(): JSONObject {
        val jsonStr = adapter.toJson(this)
        return JSONObject(jsonStr)
    }

    public companion object {
        private val adapter = Actito.moshi.adapter(ActitoApplication::class.java)

        /**
         * Creates an [ActitoApplication] instance from a JSON object.
         *
         * @param json The JSON representation of the application.
         * @return A parsed [ActitoApplication] instance.
         * @throws IllegalArgumentException If the JSON cannot be parsed.
         */
        public fun fromJson(json: JSONObject): ActitoApplication {
            val jsonStr = json.toString()
            return requireNotNull(adapter.fromJson(jsonStr))
        }
    }

    /**
     * Configuration related to inbox-based features.
     *
     * @property useInbox Whether the inbox feature is enabled for the application.
     * @property useUserInbox Whether the user inbox feature is enabled for the application.
     * @property autoBadge Whether inbox messages should automatically update the application badge count.
     */
    @Parcelize
    @JsonClass(generateAdapter = true)
    public data class InboxConfig(
        val useInbox: Boolean = false,
        val useUserInbox: Boolean = false,
        val autoBadge: Boolean = false,
    ) : Parcelable {

        /**
         * Serializes [InboxConfig] into a JSON object.
         */
        public fun toJson(): JSONObject {
            val jsonStr = adapter.toJson(this)
            return JSONObject(jsonStr)
        }

        public companion object {
            private val adapter = Actito.moshi.adapter(InboxConfig::class.java)

            /**
             * Creates an [InboxConfig] instance from a JSON object.
             *
             * @param json The JSON representation of the inbox config.
             * @return A parsed [InboxConfig] instance.
             * @throws IllegalArgumentException If the JSON cannot be parsed.
             */
            public fun fromJson(json: JSONObject): InboxConfig {
                val jsonStr = json.toString()
                return requireNotNull(adapter.fromJson(jsonStr))
            }
        }
    }

    /**
     * Configuration related to region-based features.
     *
     * @property proximityUUID Optional UUID used for beacon detection.
     */
    @Parcelize
    @JsonClass(generateAdapter = true)
    public data class RegionConfig(
        val proximityUUID: String?,
    ) : Parcelable {

        /**
         * Serializes [RegionConfig] into a JSON object.
         */
        public fun toJson(): JSONObject {
            val jsonStr = adapter.toJson(this)
            return JSONObject(jsonStr)
        }

        public companion object {
            private val adapter = Actito.moshi.adapter(RegionConfig::class.java)

            /**
             * Creates an [RegionConfig] instance from a JSON object.
             *
             * @param json The JSON representation of the region config.
             * @return A parsed [RegionConfig] instance.
             * @throws IllegalArgumentException If the JSON cannot be parsed.
             */
            public fun fromJson(json: JSONObject): RegionConfig {
                val jsonStr = json.toString()
                return requireNotNull(adapter.fromJson(jsonStr))
            }
        }
    }

    /**
     * Describes a user data field supported by an Actito application.
     *
     * User data fields define the structure of user attributes that can be
     * stored and leveraged for segmentation or personalization.
     *
     * @property type The data type of the field.
     * @property key The unique key identifying the field.
     * @property label Human-readable label for the field.
     */
    @Parcelize
    @JsonClass(generateAdapter = true)
    public data class UserDataField(
        val type: String,
        val key: String,
        val label: String,
    ) : Parcelable {

        /**
         * Serializes [UserDataField] into a JSON object.
         */
        public fun toJson(): JSONObject {
            val jsonStr = adapter.toJson(this)
            return JSONObject(jsonStr)
        }

        public companion object {
            private val adapter = Actito.moshi.adapter(UserDataField::class.java)

            /**
             * Creates an [UserDataField] instance from a JSON object.
             *
             * @param json The JSON representation of the user data field.
             * @return A parsed [UserDataField] instance.
             * @throws IllegalArgumentException If the JSON cannot be parsed.
             */
            public fun fromJson(json: JSONObject): UserDataField {
                val jsonStr = json.toString()
                return requireNotNull(adapter.fromJson(jsonStr))
            }
        }
    }

    /**
     * Groups related actions that can be triggered from notifications
     * or other engagement mechanisms.
     *
     * @property type The category type identifier.
     * @property name The name of the action category.
     * @property description Optional description explaining the purpose of the category.
     * @property actions List of actions belonging to this category.
     */
    @Parcelize
    @JsonClass(generateAdapter = true)
    public data class ActionCategory(
        val type: String,
        val name: String,
        val description: String?,
        val actions: List<ActitoNotification.Action>,
    ) : Parcelable {

        /**
         * Serializes [ActionCategory] into a JSON object.
         */
        public fun toJson(): JSONObject {
            val jsonStr = adapter.toJson(this)
            return JSONObject(jsonStr)
        }

        public companion object {
            private val adapter = Actito.moshi.adapter(ActionCategory::class.java)

            /**
             * Creates an [ActionCategory] instance from a JSON object.
             *
             * @param json The JSON representation of the action category.
             * @return A parsed [ActionCategory] instance.
             * @throws IllegalArgumentException If the JSON cannot be parsed.
             */
            public fun fromJson(json: JSONObject): ActionCategory {
                val jsonStr = json.toString()
                return requireNotNull(adapter.fromJson(jsonStr))
            }
        }
    }

    /**
     * Keys representing services that can be enabled or disabled for an application.
     *
     * These values are used as keys in the services map of [ActitoApplication],
     * where each key maps to a Boolean indicating whether the service is enabled.
     */
    public object ServiceKeys {
        /** OAuth 2.0 authentication service. */
        public const val OAUTH2: String = "oauth2"

        /** Rich push notifications with extended content. */
        public const val RICH_PUSH: String = "richPush"

        /** Location-based services, including geofencing and beacon support. */
        public const val LOCATION_SERVICES: String = "locationServices"

        /** Apple Push Notification Service (iOS). */
        public const val APNS: String = "apns"

        /** Google Cloud Messaging (Android). */
        public const val GCM: String = "gcm"

        /** WebSocket-based real-time communication. */
        public const val WEBSOCKETS: String = "websockets"

        /** Wallet functionality (passes, tickets, coupons). */
        public const val PASSBOOK: String = "passbook"

        /** In-app purchase tracking and integration. */
        public const val IN_APP_PURCHASE: String = "inAppPurchase"

        /** Inbox and message storage functionality. */
        public const val INBOX: String = "inbox"

        /** Remote file and asset storage service. */
        public const val STORAGE: String = "storage"
    }
}
