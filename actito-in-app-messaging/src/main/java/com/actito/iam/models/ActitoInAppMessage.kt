package com.actito.iam.models

import android.os.Parcelable
import com.actito.Actito
import com.actito.internal.moshi
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize
import org.json.JSONObject

/**
 * Represents an in-app message delivered by Actito.
 *
 * An [ActitoInAppMessage] defines content that can be displayed directly within
 * the application. Messages may include text, images, and actions for user interaction.
 *
 * @property id Unique identifier of the in-app message.
 * @property name Human-readable name of the message.
 * @property type Type of the message.
 * @property context List of contexts where the message should be displayed.
 * @property title Optional title of the message.
 * @property message Optional body text of the message.
 * @property image Optional portrait image URL associated with the message.
 * @property landscapeImage Optional landscape image URL associated with the message.
 * @property delaySeconds Delay before displaying the message, in seconds.
 * @property primaryAction Optional primary action associated with the message.
 * @property secondaryAction Optional secondary action associated with the message.
 */
@Parcelize
@JsonClass(generateAdapter = true)
public data class ActitoInAppMessage(
    val id: String,
    val name: String,
    val type: String,
    val context: List<String>,
    val title: String?,
    val message: String?,
    val image: String?,
    val landscapeImage: String?,
    val delaySeconds: Int,
    val primaryAction: Action?,
    val secondaryAction: Action?,
) : Parcelable {

    /**
     * Serializes [ActitoInAppMessage] into a JSON object.
     */
    public fun toJson(): JSONObject {
        val jsonStr = adapter.toJson(this)
        return JSONObject(jsonStr)
    }

    public companion object {
        private val adapter = Actito.moshi.adapter(ActitoInAppMessage::class.java)

        /**
         * In-app message displayed as a banner.
         */
        public const val TYPE_BANNER: String = "re.notifica.inappmessage.Banner"

        /**
         * In-app message displayed as a card.
         */
        public const val TYPE_CARD: String = "re.notifica.inappmessage.Card"

        /**
         * In-app message displayed in full screen.
         */
        public const val TYPE_FULLSCREEN: String = "re.notifica.inappmessage.Fullscreen"

        /**
         * Display in-app message when the app is launched.
         */
        public const val CONTEXT_LAUNCH: String = "launch"

        /**
         * Display in-app message when the app enters the foreground.
         */
        public const val CONTEXT_FOREGROUND: String = "foreground"

        /**
         * Creates an [ActitoInAppMessage] instance from a JSON object.
         *
         * @param json The JSON representation of the in-app message.
         * @return A parsed [ActitoInAppMessage] instance.
         * @throws IllegalArgumentException If the JSON cannot be parsed.
         */
        public fun fromJson(json: JSONObject): ActitoInAppMessage {
            val jsonStr = json.toString()
            return requireNotNull(adapter.fromJson(jsonStr))
        }
    }

    /**
     * Represents an action associated with an in-app message.
     *
     * An [Action] defines a user interaction option for an in-app
     * message, such as opening a URL or performing an operation.
     *
     * @property label Optional label displayed for the action.
     * @property destructive Indicates whether the action is destructive.
     * @property url Optional target URL triggered by the action.
     */
    @Parcelize
    @JsonClass(generateAdapter = true)
    public data class Action(
        val label: String?,
        val destructive: Boolean,
        val url: String?,
    ) : Parcelable {

        /**
         * Serializes [Action] into a JSON object.
         */
        public fun toJson(): JSONObject {
            val jsonStr = adapter.toJson(this)
            return JSONObject(jsonStr)
        }

        public companion object {
            private val adapter = Actito.moshi.adapter(Action::class.java)

            /**
             * Creates an [Action] instance from a JSON object.
             *
             * @param json The JSON representation of the action.
             * @return A parsed [Action] instance.
             * @throws IllegalArgumentException If the JSON cannot be parsed.
             */
            public fun fromJson(json: JSONObject): Action {
                val jsonStr = json.toString()
                return requireNotNull(adapter.fromJson(jsonStr))
            }
        }
    }

    /**
     * Represents the role of an action within a notification or in-app message.
     */
    @Suppress("ktlint:standard:trailing-comma-on-declaration-site")
    public enum class ActionType {
        /**
         * Primary action.
         */
        PRIMARY,

        /**
         * Secondary action.
         */
        SECONDARY;

        /**
         * String representation of the Action types.
         */
        public val rawValue: String
            get() = when (this) {
                PRIMARY -> "primary"
                SECONDARY -> "secondary"
            }

        /**
         * Returns the serialized string representation of this action type.
         */
        override fun toString(): String = rawValue
    }
}
