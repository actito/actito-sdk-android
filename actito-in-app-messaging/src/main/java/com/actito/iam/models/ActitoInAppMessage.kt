package com.actito.iam.models

import android.os.Parcelable
import com.actito.Actito
import com.actito.internal.moshi
import com.squareup.moshi.JsonClass
import kotlinx.parcelize.Parcelize
import org.json.JSONObject

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

    public fun toJson(): JSONObject {
        val jsonStr = adapter.toJson(this)
        return JSONObject(jsonStr)
    }

    public companion object {
        private val adapter = Actito.moshi.adapter(ActitoInAppMessage::class.java)

        public const val TYPE_BANNER: String = "re.notifica.inappmessage.Banner"
        public const val TYPE_CARD: String = "re.notifica.inappmessage.Card"
        public const val TYPE_FULLSCREEN: String = "re.notifica.inappmessage.Fullscreen"

        public const val CONTEXT_LAUNCH: String = "launch"
        public const val CONTEXT_FOREGROUND: String = "foreground"

        public fun fromJson(json: JSONObject): ActitoInAppMessage {
            val jsonStr = json.toString()
            return requireNotNull(adapter.fromJson(jsonStr))
        }
    }

    @Parcelize
    @JsonClass(generateAdapter = true)
    public data class Action(
        val label: String?,
        val destructive: Boolean,
        val url: String?,
    ) : Parcelable {

        public fun toJson(): JSONObject {
            val jsonStr = adapter.toJson(this)
            return JSONObject(jsonStr)
        }

        public companion object {
            private val adapter = Actito.moshi.adapter(Action::class.java)

            public fun fromJson(json: JSONObject): Action {
                val jsonStr = json.toString()
                return requireNotNull(adapter.fromJson(jsonStr))
            }
        }
    }

    public enum class ActionType {
        PRIMARY,
        SECONDARY,
        ;

        public val rawValue: String
            get() = when (this) {
                PRIMARY -> "primary"
                SECONDARY -> "secondary"
            }

        override fun toString(): String {
            return rawValue
        }
    }
}
