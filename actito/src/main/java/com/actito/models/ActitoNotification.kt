package com.actito.models

import android.content.Context
import android.os.Parcelable
import com.actito.Actito
import com.actito.internal.moshi
import com.actito.internal.parcelize.ActitoExtraParceler
import com.actito.utilities.parcelize.NotificationContentDataParceler
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.WriteWith
import org.json.JSONObject
import java.util.Date

/**
 * Represents a notification delivered by Actito.
 *
 * An [ActitoNotification] contains the payload of a notification, including
 * its content, actions, attachments, and additional metadata.
 * Notifications may be partial, meaning that only a subset of fields is provided
 * and additional data may need to be fetched.
 *
 * @property id Unique identifier of the notification.
 * @property partial Indicates whether this notification is partial. When `true`,
 * the notification does not contain the full payload.
 * @property type Type of the notification. This value is defined by Actito and
 * is used to distinguish different notification behaviors.
 * @property time Timestamp indicating when the notification was generated.
 * @property title Optional title displayed in the notification.
 * @property subtitle Optional subtitle displayed in the notification.
 * @property message Main message body of the notification.
 * @property content Structured content elements associated with the notification.
 * @property actions List of actions that can be performed from the notification.
 * @property attachments List of attachments included with the notification.
 * @property extra Collection of key-value pairs used to add extra information to
 * the notification.
 */
@Parcelize
@JsonClass(generateAdapter = true)
public data class ActitoNotification(
    val id: String,
    val partial: Boolean = false,
    val type: String,
    val time: Date,
    val title: String?,
    val subtitle: String?,
    val message: String,
    val content: List<Content> = listOf(),
    val actions: List<Action> = listOf(),
    val attachments: List<Attachment> = listOf(),
    val extra: @WriteWith<ActitoExtraParceler> Map<String, Any> = mapOf(),
) : Parcelable {

    /**
     * Serializes [ActitoNotification] into a JSON object.
     */
    public fun toJson(): JSONObject {
        val jsonStr = adapter.toJson(this)
        return JSONObject(jsonStr)
    }

    public companion object {
        /**
         * Will only open the app without showing additional UI.
         */
        public const val TYPE_NONE: String = "re.notifica.notification.None"

        /**
         * Displays a simple alert-style notification.
         */
        public const val TYPE_ALERT: String = "re.notifica.notification.Alert"

        /**
         * Displays the OS built-in browser and loads a provided URL.
         */
        public const val TYPE_IN_APP_BROWSER: String = "re.notifica.notification.InAppBrowser"

        /**
         * Displays a native web view and loads a provided HTML markup.
         */
        public const val TYPE_WEB_VIEW: String = "re.notifica.notification.WebView"

        /**
         * Displays a native web view and loads a provided URL.
         */
        public const val TYPE_URL: String = "re.notifica.notification.URL"

        /**
         * Opens a URL or a custom URL scheme, automatically deciding how to display it.
         */
        public const val TYPE_URL_RESOLVER: String = "re.notifica.notification.URLResolver"

        /**
         * Opens a URL using a custom URL scheme to drive users directly into a view on the app.
         */
        public const val TYPE_URL_SCHEME: String = "re.notifica.notification.URLScheme"

        /**
         * Displays an image gallery with provided images.
         */
        public const val TYPE_IMAGE: String = "re.notifica.notification.Image"

        /**
         * Plays a Youtube, Vimeo or uploaded MP4 video.
         */
        public const val TYPE_VIDEO: String = "re.notifica.notification.Video"

        /**
         * Displays a native map with provided location markers.
         */
        public const val TYPE_MAP: String = "re.notifica.notification.Map"

        /**
         * Prompts the user to rate the application.
         */
        public const val TYPE_RATE: String = "re.notifica.notification.Rate"

        /**
         * Displays a Google Wallet compatible card created on Actito.
         */
        public const val TYPE_PASSBOOK: String = "re.notifica.notification.Passbook"

        /**
         * Opens an application store page.
         */
        public const val TYPE_STORE: String = "re.notifica.notification.Store"

        private val adapter = Actito.moshi.adapter(ActitoNotification::class.java)

        /**
         * Creates an [ActitoNotification] instance from a JSON object.
         *
         * @param json The JSON representation of the notification.
         * @return A parsed [ActitoNotification] instance.
         * @throws IllegalArgumentException If the JSON cannot be parsed.
         */
        public fun fromJson(json: JSONObject): ActitoNotification {
            val jsonStr = json.toString()
            return requireNotNull(adapter.fromJson(jsonStr))
        }
    }

    /**
     * Represents a structured content element within a notification.
     *
     * @property type The content type identifier.
     * @property data The content payload.
     */
    @Parcelize
    @JsonClass(generateAdapter = true)
    public data class Content(
        val type: String,
        val data: @WriteWith<NotificationContentDataParceler> Any,
    ) : Parcelable {

        /**
         * Serializes [Content] into a JSON object.
         */
        public fun toJson(): JSONObject {
            val jsonStr = adapter.toJson(this)
            return JSONObject(jsonStr)
        }

        public companion object {
            /**
             * HTML content to be rendered.
             */
            public const val TYPE_HTML: String = "re.notifica.content.HTML"

            /**
             * PKPass associated with a Passbook notification.
             */
            public const val TYPE_PK_PASS: String = "re.notifica.content.PKPass"

            /**
             * Google Play Store details page for a specific application.
             */
            public const val TYPE_GOOGLE_PLAY_DETAILS: String = "re.notifica.content.GooglePlayDetails"

            /**
             * Google Play Store developer page for a specific developer.
             */
            public const val TYPE_GOOGLE_PLAY_DEVELOPER: String = "re.notifica.content.GooglePlayDeveloper"

            /**
             * Google Play Store search results page for a specific query.
             */
            public const val TYPE_GOOGLE_PLAY_SEARCH: String = "re.notifica.content.GooglePlaySearch"

            /**
             * Google Play Store collection page for a specific collection.
             */
            public const val TYPE_GOOGLE_PLAY_COLLECTION: String = "re.notifica.content.GooglePlayCollection"

            /**
             * Opens an AppGallery application details page for a specific application.
             */
            public const val TYPE_APP_GALLERY_DETAILS: String = "re.notifica.content.AppGalleryDetails"

            /**
             * Opens an AppGallery search results page for a specific query.
             */
            public const val TYPE_APP_GALLERY_SEARCH: String = "re.notifica.content.AppGallerySearch"

            private val adapter = Actito.moshi.adapter(Content::class.java)

            /**
             * Creates an [Content] instance from a JSON object.
             *
             * @param json The JSON representation of the content.
             * @return A parsed [Content] instance.
             * @throws IllegalArgumentException If the JSON cannot be parsed.
             */
            public fun fromJson(json: JSONObject): Content {
                val jsonStr = json.toString()
                return requireNotNull(adapter.fromJson(jsonStr))
            }
        }
    }

    /**
     * Represents an action that can be triggered from a notification.
     * @property type Type of the action.
     * @property label User-visible label of the action.
     * @property target Optional target associated with the action.
     * @property camera Whether the action requires keyboard input.
     * @property keyboard Whether the action requires camera input.
     * @property destructive Whether the action is destructive.
     * @property icon Optional platform-specific icon configuration for the action.
     */
    @Parcelize
    @JsonClass(generateAdapter = true)
    public data class Action(
        val type: String,
        val label: String,
        val target: String?,
        val camera: Boolean,
        val keyboard: Boolean,
        val destructive: Boolean?,
        val icon: Icon?,
    ) : Parcelable {

        /**
         * Returns the localized label for this action, if available.
         *
         * The SDK attempts to resolve the action label as a string resource using
         * the configured notification action label prefix. If no matching resource
         * is found, the raw label value is returned.
         *
         * @param context Android context used to resolve string resources.
         * @return The localized label if available, otherwise the original label.
         */
        public fun getLocalizedLabel(context: Context): String {
            val prefix = Actito.options?.notificationActionLabelPrefix ?: ""
            val resourceName = "$prefix$label"
            val resource = context.resources.getIdentifier(resourceName, "string", context.packageName)

            return if (resource == 0) label else context.getString(resource)
        }

        /**
         * Resolves the Android drawable resource associated with this action's icon.
         *
         * If no Android-specific icon is defined or the resource cannot be found,
         * this method returns `0`.
         *
         * @param context Android context used to resolve drawable resources.
         * @return The drawable resource ID, or `0` if unavailable.
         */
        public fun getIconResource(context: Context): Int {
            val icon = icon?.android ?: return 0

            return context.resources.getIdentifier(icon, "drawable", "android")
        }

        /**
         * Serializes [Action] into a JSON object.
         */
        public fun toJson(): JSONObject {
            val jsonStr = adapter.toJson(this)
            return JSONObject(jsonStr)
        }

        public companion object {
            /**
             * Uses deep links to open the application to a specific screen or triggers a deep link
             * in another app that supports the provided URI.
             */
            public const val TYPE_APP: String = "re.notifica.action.App"

            /**
             * Opens the target URL in the system’s default web browser.
             */

            public const val TYPE_BROWSER: String = "re.notifica.action.Browser"

            /**
             * Action that collects a response from the user.
             *
             * This action can capture a simple confirmation, text input via the keyboard,
             * or media input using the device camera, depending on its configuration.
             */
            public const val TYPE_CALLBACK: String = "re.notifica.action.Callback"

            /**
             * Executes custom behavior in the application.
             *
             * This action requires additional client-side implementation to handle
             * the associated payload.
             */
            public const val TYPE_CUSTOM: String = "re.notifica.action.Custom"

            /**
             * Opens the device’s default email application with a prefilled recipient.
             */
            public const val TYPE_MAIL: String = "re.notifica.action.Mail"

            /**
             * Opens the device’s default SMS application with a prefilled recipient.
             */
            public const val TYPE_SMS: String = "re.notifica.action.SMS"

            /**
             * Opens the default Telephone application to phone call a provided phone number.
             */
            public const val TYPE_TELEPHONE: String = "re.notifica.action.Telephone"

            /**
             * Opens the target URL inside an in-app browser.
             */
            public const val TYPE_IN_APP_BROWSER: String = "re.notifica.action.InAppBrowser"

            @Deprecated(
                message = "The WebView action type becomes a backwards compatible alias. Use the InAppBrowser action type instead.",
                replaceWith = ReplaceWith("ActitoNotification.Action.TYPE_IN_APP_BROWSER"),
            )
            public const val TYPE_WEB_VIEW: String = "re.notifica.action.WebView"

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

        /**
         * Defines platform-specific icons for a notification action.
         *
         * @property android Resource identifier for Android.
         * @property ios Resource identifier for iOS.
         * @property web Resource identifier for Web.
         */
        @Parcelize
        @JsonClass(generateAdapter = true)
        public data class Icon(
            val android: String?,
            val ios: String?,
            val web: String?,
        ) : Parcelable {

            /**
             * Serializes [Icon] into a JSON object.
             */
            public fun toJson(): JSONObject {
                val jsonStr = adapter.toJson(this)
                return JSONObject(jsonStr)
            }

            public companion object {
                private val adapter = Actito.moshi.adapter(Icon::class.java)

                /**
                 * Creates an [Icon] instance from a JSON object.
                 *
                 * @param json The JSON representation of the icon.
                 * @return A parsed [Icon] instance.
                 * @throws IllegalArgumentException If the JSON cannot be parsed.
                 */
                public fun fromJson(json: JSONObject): Icon {
                    val jsonStr = json.toString()
                    return requireNotNull(adapter.fromJson(jsonStr))
                }
            }
        }
    }

    /**
     * Represents an attachment included with a notification.
     *
     * @property mimeType MIME type of the attachment.
     * @property uri URI pointing to the attachment resource.
     */
    @Parcelize
    @JsonClass(generateAdapter = true)
    public data class Attachment(
        val mimeType: String,
        val uri: String,
    ) : Parcelable {

        /**
         * Serializes [Attachment] into a JSON object.
         */
        public fun toJson(): JSONObject {
            val jsonStr = adapter.toJson(this)
            return JSONObject(jsonStr)
        }

        public companion object {
            private val adapter = Actito.moshi.adapter(Attachment::class.java)

            /**
             * Creates an [Attachment] instance from a JSON object.
             *
             * @param json The JSON representation of the attachment.
             * @return A parsed [Attachment] instance.
             * @throws IllegalArgumentException If the JSON cannot be parsed.
             */
            public fun fromJson(json: JSONObject): Attachment {
                val jsonStr = json.toString()
                return requireNotNull(adapter.fromJson(jsonStr))
            }
        }
    }

    /**
     * Represents the type of a notification, determining how it will be displayed
     * or handled by the application.
     */
    @Suppress("ktlint:standard:trailing-comma-on-declaration-site")
    public enum class NotificationType {
        /**
         * Will only open the app without showing additional UI.
         */
        NONE,

        /**
         * Displays a simple alert-style notification.
         */
        ALERT,

        /**
         * Displays the OS built-in browser and loads a provided URL.
         */
        IN_APP_BROWSER,

        /**
         * Displays a native web view and loads a provided HTML markup.
         */
        WEB_VIEW,

        /**
         * Displays a native web view and loads a provided URL.
         */
        URL,

        /**
         * Opens a URL or a custom URL scheme, automatically deciding how to display it.
         */
        URL_RESOLVER,

        /**
         * Opens a URL using a custom URL scheme to drive users directly into a view on the app.
         */
        URL_SCHEME,

        /**
         * Displays an image gallery with provided images.
         */
        IMAGE,

        /**
         * Plays a Youtube, Vimeo or uploaded MP4 video.
         */
        VIDEO,

        /**
         * Displays a native map with provided location markers.
         */
        MAP,

        /**
         * Prompts the user to rate the application.
         */
        RATE,

        /**
         * Displays a Google Wallet compatible card created on Actito.
         */
        PASSBOOK,

        /**
         * Opens an application store page.
         */
        STORE;

        public companion object {
            /**
             * Converts a string type into its corresponding [NotificationType] enum value.
             *
             * @param type The string representation of the notification type.
             * @return The matching [NotificationType] or `null` if the type is unknown.
             */
            public fun from(type: String): NotificationType? =
                when (type) {
                    TYPE_NONE -> NONE
                    TYPE_ALERT -> ALERT
                    TYPE_IN_APP_BROWSER -> IN_APP_BROWSER
                    TYPE_WEB_VIEW -> WEB_VIEW
                    TYPE_URL -> URL
                    TYPE_URL_RESOLVER -> URL_RESOLVER
                    TYPE_URL_SCHEME -> URL_SCHEME
                    TYPE_IMAGE -> IMAGE
                    TYPE_VIDEO -> VIDEO
                    TYPE_MAP -> MAP
                    TYPE_RATE -> RATE
                    TYPE_PASSBOOK -> PASSBOOK
                    TYPE_STORE -> STORE
                    else -> null
                }
        }
    }
}
