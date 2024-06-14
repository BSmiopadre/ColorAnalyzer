package com.example.coloranalyzer.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RGBDao {

    @Query("SELECT * FROM RGB_table ORDER BY timestamp DESC")
    fun getAllData(): Flow<List<RGB>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(rgb: RGB)

    @Query("DELETE FROM RGB_table")
    suspend fun deleteAll()

    @Query("DELETE FROM RGB_table WHERE timestamp < :timeLimit")
    suspend fun deleteOldData(timeLimit: Long)
}