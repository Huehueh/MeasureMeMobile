package com.example.measureme

import android.util.Log
import androidx.compose.runtime.mutableStateOf


class PointConverter(val imageWidth: Int, val imageHeight:Int) {
    val TAG: String = "PointConverter"
    var displayWidth= mutableStateOf(0)
    var displayHeight = mutableStateOf(0)

    fun setDisplaySize(width: Int, height: Int) {
        Log.i(TAG, "DISPLAY SIZE w $width h $height")
        displayWidth.value = width
        displayHeight.value = height
    }

    fun getDisplayPosition(imagePosition:Point):Point {
        var result = arrayOf<Int>()
        val p1 = imagePosition[0] * displayWidth.value/imageWidth
        val p2 = imagePosition[1] * displayHeight.value/imageHeight
        result += p1
        result += p2
        Log.i(TAG, "getDisplayPosition: Point ${imagePosition[0]} ${imagePosition[1]}  result ${result[0]}  ${result[1]}")
        Log.i(TAG, "getDisplayPosition: display w $displayWidth h $displayHeight ; image w $imageWidth h $imageHeight")
        return result
    }

    fun getImagePosition(displayPosition: Point): Point {
        var result = arrayOf<Int>()
        val p1 = displayPosition[0] * imageWidth/displayWidth.value
        val p2 = displayPosition[1] * imageHeight/displayHeight.value
        result += p1
        result += p2
        Log.i(TAG, "getDisplayPosition: Point ${displayPosition[0]} ${displayPosition[1]}  result ${result[0]}  ${result[1]}")
        Log.i(TAG, "getDisplayPosition: display w $displayWidth h $displayHeight ; image w $imageWidth h $imageHeight")
        return result
    }
}