package com.actito.internal.network.push

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.util.Date
import com.actito.InternalActitoApi
import com.actito.models.ActitoApplication
import com.actito.models.ActitoDoNotDisturb
import com.actito.models.ActitoDynamicLink
import com.actito.models.ActitoNotification

@JsonClass(generateAdapter = true)
internal data class ApplicationResponse(
    val application: Application
) {

    @JsonClass(generateAdapter = true)
    internal data class Application(
        @Json(name = "_id") val id: String,
        val name: String,
        val category: String,
        val services: Map<String, Boolean>,
        val inboxConfig: ActitoApplication.InboxConfig?,
        val regionConfig: ActitoApplication.RegionConfig?,
        val userDataFields: List<ActitoApplication.UserDataField>,
        val actionCategories: List<ActionCategory>
    ) {

        @JsonClass(generateAdapter = true)
        internal data class ActionCategory(
            val type: String,
            val name: String,
            val description: String?,
            val actions: List<NotificationResponse.Notification.Action>,
        )

        fun toModel(): ActitoApplication {
            return ActitoApplication(
                id,
                name,
                category,
                services,
                inboxConfig,
                regionConfig,
                userDataFields,
                actionCategories.map { category ->
                    ActitoApplication.ActionCategory(
                        category.type,
                        category.name,
                        category.description,
                        category.actions.mapNotNull { it.toModel() }
                    )
                }
            )
        }
    }
}

@JsonClass(generateAdapter = true)
internal data class CreateDeviceResponse(
    val device: Device,
) {

    @JsonClass(generateAdapter = true)
    internal data class Device(
        @Json(name = "deviceID") val deviceId: String,
    )
}

@JsonClass(generateAdapter = true)
internal data class DeviceDoNotDisturbResponse(
    val dnd: ActitoDoNotDisturb?,
)

@JsonClass(generateAdapter = true)
internal data class DeviceTagsResponse(
    val tags: List<String>
)

@JsonClass(generateAdapter = true)
internal data class DeviceUserDataResponse(
    val userData: Map<String, String?>?,
)

@InternalActitoApi
@JsonClass(generateAdapter = true)
public data class NotificationResponse(
    val notification: Notification,
) {

    @JsonClass(generateAdapter = true)
    public data class Notification(
        @Json(name = "_id") val id: String,
        val partial: Boolean = false,
        val type: String,
        val time: Date,
        val title: String?,
        val subtitle: String?,
        val message: String,
        val content: List<ActitoNotification.Content> = listOf(),
        val actions: List<Action> = listOf(),
        val attachments: List<ActitoNotification.Attachment> = listOf(),
        val extra: Map<String, Any> = mapOf(),
    ) {

        @JsonClass(generateAdapter = true)
        public data class Action(
            val type: String,
            val label: String?,
            val target: String?,
            val camera: Boolean?,
            val keyboard: Boolean?,
            val destructive: Boolean?,
            val icon: ActitoNotification.Action.Icon?,
        ) {

            public fun toModel(): ActitoNotification.Action? {
                if (label == null) return null

                return ActitoNotification.Action(
                    type,
                    label,
                    target,
                    camera ?: false,
                    keyboard ?: false,
                    destructive,
                    icon
                )
            }
        }

        public fun toModel(): ActitoNotification {
            return ActitoNotification(
                id,
                partial,
                type,
                time,
                title,
                subtitle,
                message,
                content,
                actions.mapNotNull { it.toModel() },
                attachments,
                extra
            )
        }
    }
}

@JsonClass(generateAdapter = true)
internal data class ActitoUploadResponse(
    val filename: String,
)

@JsonClass(generateAdapter = true)
internal data class DynamicLinkResponse(
    val link: ActitoDynamicLink,
)
