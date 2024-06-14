package com.example.coloranalyzer

import android.app.Application
import com.example.coloranalyzer.database.RGBRepository
import com.example.coloranalyzer.database.RGBRoomDatabase

class Application: Application() {

    private val database by lazy { RGBRoomDatabase.getDatabase(this) }
    val repository by lazy { RGBRepository(database.rgbDao()) }

}