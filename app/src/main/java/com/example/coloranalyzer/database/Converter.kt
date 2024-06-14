package com.example.coloranalyzer.database

import android.graphics.Color
import androidx.room.TypeConverter

/** this is a util class for data conversion, because complex data cannot be stored in a Room Database */
class Converter {

    companion object{

        @TypeConverter
        @JvmStatic
        fun colorToLString(color: Color?): String {

            return "${color?.red()}, ${color?.green()}, ${color?.blue()}"
        }

        @TypeConverter
        @JvmStatic
        fun stringToColor(savedData: String): Color {

            val list = savedData.split(",")
            val red = list[0].toFloat()
            val green = list[1].toFloat()
            val blue = list[2].toFloat()

            return Color.valueOf(red, green, blue)
        }

    }
}