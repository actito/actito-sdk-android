package com.actito.internal.room

import androidx.room.TypeConverter
import com.actito.Actito
import com.actito.InternalActitoApi
import com.actito.internal.moshi
import com.actito.models.ActitoNotification

@InternalActitoApi
public class ActitoNotificationConverter {

    @TypeConverter
    public fun fromJson(str: String?): ActitoNotification? {
        if (str == null) return null

        val adapter = Actito.moshi.adapter(ActitoNotification::class.java)
        return adapter.fromJson(str)
    }

    @TypeConverter
    public fun toJson(notification: ActitoNotification?): String? {
        if (notification == null) return null

        val adapter = Actito.moshi.adapter(ActitoNotification::class.java)
        return adapter.toJson(notification)
    }
}
