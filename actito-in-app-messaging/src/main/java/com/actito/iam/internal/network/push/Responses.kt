package com.actito.iam.internal.network.push

import com.squareup.moshi.JsonClass
import com.actito.iam.models.ActitoInAppMessage
import com.actito.utilities.moshi.UseDefaultsWhenNull

@JsonClass(generateAdapter = true)
internal data class InAppMessageResponse(
    val message: Message,
) {

    @UseDefaultsWhenNull
    @JsonClass(generateAdapter = true)
    data class Message(
        val _id: String,
        val name: String,
        val type: String,
        val context: List<String> = listOf(),
        val title: String?,
        val message: String?,
        val image: String?,
        val landscapeImage: String?,
        val delaySeconds: Int = 0,
        val primaryAction: Action?,
        val secondaryAction: Action?,
    ) {

        @UseDefaultsWhenNull
        @JsonClass(generateAdapter = true)
        data class Action(
            val label: String?,
            val destructive: Boolean = false,
            val url: String?,
        )

        fun toModel(): ActitoInAppMessage {
            return ActitoInAppMessage(
                id = _id,
                name = name,
                type = type,
                context = context,
                title = title,
                message = message,
                image = image,
                landscapeImage = landscapeImage,
                delaySeconds = delaySeconds,
                primaryAction = primaryAction?.let {
                    ActitoInAppMessage.Action(
                        label = it.label,
                        destructive = it.destructive,
                        url = it.url,
                    )
                },
                secondaryAction = secondaryAction?.let {
                    ActitoInAppMessage.Action(
                        label = it.label,
                        destructive = it.destructive,
                        url = it.url,
                    )
                },
            )
        }
    }
}
