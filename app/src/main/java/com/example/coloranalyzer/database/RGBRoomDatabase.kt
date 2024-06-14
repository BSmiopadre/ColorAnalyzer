package com.example.coloranalyzer.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [RGB::class], version = 1, exportSchema = false)
@TypeConverters(Converter::class)
abstract class RGBRoomDatabase: RoomDatabase() {

    abstract fun rgbDao(): RGBDao

    companion object {

        @Volatile
        private var INSTANCE : RGBRoomDatabase? = null

        fun getDatabase(context: Context): RGBRoomDatabase {

            return INSTANCE ?: synchronized(this){

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    RGBRoomDatabase::class.java,
                    "RGB_Database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}