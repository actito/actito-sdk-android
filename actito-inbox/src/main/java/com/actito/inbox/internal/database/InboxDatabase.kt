package com.actito.inbox.internal.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.actito.inbox.internal.database.dao.InboxDao
import com.actito.inbox.internal.database.entities.InboxItemEntity
import com.actito.internal.room.ActitoDateTypeConverter
import com.actito.internal.room.ActitoNotificationConverter

@Database(
    version = 1,
    entities = [
        InboxItemEntity::class,
    ],
)
@TypeConverters(
    ActitoDateTypeConverter::class,
    ActitoNotificationConverter::class,
)
internal abstract class InboxDatabase : RoomDatabase() {

    abstract fun inbox(): InboxDao

    companion object {
        private const val DB_NAME = "notificare_inbox.db"

        internal fun create(context: Context): InboxDatabase =
            Room.databaseBuilder(
                context,
                InboxDatabase::class.java,
                DB_NAME,
            ).fallbackToDestructiveMigration().build()
    }
}
