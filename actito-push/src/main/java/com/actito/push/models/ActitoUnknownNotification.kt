package com.actito.push.models

import android.net.Uri
import android.os.Parcelable
import com.actito.Actito
import com.actito.internal.moshi
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize
import org.json.JSONObject

/**
 * Represents a notification received on the device that was not sent by Actito.
 *
 * Use this class when handling push notifications that come from other sources
 * or custom integrations.
 *
 * @property messageId Firebase message identifier, if available.
 * @property messageType Firebase-defined message type.
 * @property senderId Sender identifier associated with the message.
 * @property collapseKey Collapse key used by Firebase to group messages.
 * @property from Sender of the message.
 * @property to Destination field of the message.
 * @property sentTime Timestamp (in milliseconds since epoch) indicating when the message was sent by Firebase.
 * @property ttl Time-to-live for the message, expressed in seconds.
 * @property priority Priority assigned to the message when it was delivered.
 * @property originalPriority Original priority specified when the message was sent.
 * @property notification Optional notification payload containing system-handled UI properties.
 * @property data Custom key-value data payload included with the message.
 */
@Parcelize
@JsonClass(generateAdapter = true)
public data class ActitoUnknownNotification(
    val messageId: String?,
    val messageType: String?,
    val senderId: String?,
    val collapseKey: String?,
    val from: String?,

    @Deprecated("This attribute was deprecated in Firebase Messaging. Check their documentation for more information.")
    val to: String?,

    val sentTime: Long,
    val ttl: Long,
    val priority: Int,
    val originalPriority: Int,
    val notification: Notification?,
    val data: Map<String, String?>,
) : Parcelable {

    /**
     * Serializes [ActitoUnknownNotification] into a JSON object.
     */
    public fun toJson(): JSONObject {
        val jsonStr = adapter.toJson(this)
        return JSONObject(jsonStr)
    }

    public companion object {
        private val adapter = Actito.moshi.adapter(ActitoUnknownNotification::class.java)

        /**
         * Creates an [ActitoUnknownNotification] instance from a JSON object.
         *
         * @param json The JSON representation of the unknown notification.
         * @return A parsed [ActitoUnknownNotification] instance.
         * @throws IllegalArgumentException If the JSON cannot be parsed.
         */
        public fun fromJson(json: JSONObject): ActitoUnknownNotification {
            val jsonStr = json.toString()
            return requireNotNull(adapter.fromJson(jsonStr))
        }
    }

    /**
     * Represents the payload of a notification not sent by Actito.
     *
     * @property title The notification title.
     * @property titleLocalizationKey Localization key for the notification title, if provided.
     * @property titleLocalizationArgs Arguments for localized title formatting.
     * @property body The notification body text.
     * @property bodyLocalizationKey Localization key for the notification body, if provided.
     * @property bodyLocalizationArgs Arguments for localized body formatting.
     * @property icon Resource name of the icon to display in the notification.
     * @property imageUrl Optional URL of an image to display in the notification.
     * @property sound Sound to play when the notification is received.
     * @property tag Tag identifying the notification for replacement or grouping.
     * @property color Color of the notification (as a hex string or platform-specific color code).
     * @property clickAction Action triggered when the notification is clicked.
     * @property channelId Identifier of the notification channel.
     * @property link Optional deep link URL associated with the notification.
     * @property ticker Ticker text displayed briefly on the status bar.
     * @property sticky Whether the notification should remain visible until explicitly dismissed.
     * @property localOnly Whether the notification is local-only.
     * @property defaultSound Whether to play the default notification sound.
     * @property defaultVibrateSettings Whether to apply the default vibration pattern.
     * @property defaultLightSettings Whether to apply the default light settings.
     * @property notificationPriority Priority of the notification.
     * @property visibility Visibility of the notification on the lock screen.
     * @property notificationCount Count of notifications to display in a badge.
     * @property eventTime Timestamp of the event associated with the notification.
     * @property lightSettings Light settings for the notification, if applicable.
     * @property vibrateSettings Vibration pattern for the notification, if applicable.
     *
     */
    @Parcelize
    @JsonClass(generateAdapter = true)
    public data class Notification(
        val title: String?,
        val titleLocalizationKey: String?,
        val titleLocalizationArgs: List<String>?,
        val body: String?,
        val bodyLocalizationKey: String?,
        val bodyLocalizationArgs: List<String>?,
        val icon: String?,
        val imageUrl: Uri?,
        val sound: String?,
        val tag: String?,
        val color: String?,
        val clickAction: String?,
        val channelId: String?,
        val link: Uri?,
        val ticker: String?,
        val sticky: Boolean,
        val localOnly: Boolean,
        val defaultSound: Boolean,
        val defaultVibrateSettings: Boolean,
        val defaultLightSettings: Boolean,
        val notificationPriority: Int?,
        val visibility: Int?,
        val notificationCount: Int?,
        val eventTime: Long?,
        val lightSettings: List<Int>?,
        val vibrateSettings: List<Long>?,
    ) : Parcelable {

        /**
         * Serializes [Notification] into a JSON object.
         *
         */
        public fun toJson(): JSONObject {
            val jsonStr = adapter.toJson(this)
            return JSONObject(jsonStr)
        }

        public companion object {
            private val adapter = Actito.moshi.adapter(Notification::class.java)

            /**
             * Creates an [Notification] instance from a JSON object.
             *
             * @param json The JSON representation of the notification.
             * @return A parsed [Notification] instance.
             * @throws IllegalArgumentException If the JSON cannot be parsed.
             */
            public fun fromJson(json: JSONObject): Notification {
                val jsonStr = json.toString()
                return requireNotNull(adapter.fromJson(jsonStr))
            }
        }
    }
}
