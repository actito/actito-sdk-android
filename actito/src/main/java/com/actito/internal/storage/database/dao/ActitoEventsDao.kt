package com.actito.internal.storage.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.actito.internal.storage.database.entities.ActitoEventEntity

@Dao
internal interface ActitoEventsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: ActitoEventEntity)

    @Update
    suspend fun update(event: ActitoEventEntity)

    @Query("SELECT * FROM events")
    suspend fun find(): List<ActitoEventEntity>

    @Delete
    suspend fun delete(event: ActitoEventEntity)

    @Query("DELETE FROM events")
    suspend fun clear()
}
