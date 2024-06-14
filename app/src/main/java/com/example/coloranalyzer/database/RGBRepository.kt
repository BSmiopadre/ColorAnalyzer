package com.example.coloranalyzer.database

import androidx.annotation.WorkerThread
import kotlinx.coroutines.flow.Flow

class RGBRepository(private val rgbDao: RGBDao) {

    // reference to the data list; repository invokes this fun to get the data
    val allData : Flow<List<RGB>> = rgbDao.getAllData()

    // insert an RGB value in the db
    @WorkerThread
    suspend fun insert (rgb: RGB) {
        rgbDao.insert(rgb)
    }

    // delete from db 5 minutes old data
    @WorkerThread
    suspend fun deleteOldData (timeLimit: Long) {
        rgbDao.deleteOldData(timeLimit)
    }

}