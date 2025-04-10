package com.actito.internal.storage.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.actito.internal.storage.database.dao.ActitoEventsDao
import com.actito.internal.storage.database.entities.ActitoEventEntity

@Database(
    version = 2,
    entities = [
        ActitoEventEntity::class
    ],
)
internal abstract class ActitoDatabase : RoomDatabase() {

    abstract fun events(): ActitoEventsDao

    companion object {
        private const val DB_NAME = "notificare.db"

        internal fun create(context: Context): ActitoDatabase {
            return Room.databaseBuilder(
                context,
                ActitoDatabase::class.java,
                DB_NAME
            ).fallbackToDestructiveMigration().build()
        }
    }
}
