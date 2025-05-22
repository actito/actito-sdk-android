package com.actito.internal.room

import androidx.room.TypeConverter
import com.actito.InternalActitoApi
import java.util.Date

@InternalActitoApi
public class ActitoDateTypeConverter {

    @TypeConverter
    public fun fromTimestamp(timestamp: Long?): Date? {
        return timestamp?.let { Date(it) }
    }

    @TypeConverter
    public fun toTimestamp(date: Date?): Long? {
        return date?.time
    }
}
