package com.example.coloranalyzer

import android.graphics.Color
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy

typealias ColorListener = (r: Double, g: Double, b:Double) -> Unit

class RGBAnalyzer (private val listener: ColorListener): ImageAnalysis.Analyzer {

    /** analyze the image and set the result in the lambda "listener" */
    override fun analyze(image: ImageProxy)
    {
        val bitmap = image.toBitmap()

        val width = bitmap.width
        val height = bitmap.height
        var redSum = 0.0
        var greenSum = 0.0
        var blueSum = 0.0

        for (y in 0 until height) {
            for (x in 0 until width) {
                val pixel = bitmap.getPixel(x, y)
                redSum += Color.red(pixel)
                greenSum += Color.green(pixel)
                blueSum += Color.blue(pixel)
            }
        }

        val pixelCount = width * height

        // set the average values
        val averageRed = (redSum / pixelCount)
        val averageGreen = (greenSum / pixelCount)
        val averageBlue = (blueSum / pixelCount)

        // pass the average RGB values to the listener
        listener(averageRed, averageGreen, averageBlue)
        image.close()
    }
}