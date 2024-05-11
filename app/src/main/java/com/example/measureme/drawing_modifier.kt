package com.example.measureme

import android.util.Log
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput

fun Modifier.measureOnDrag(viewModel: ImageMeasureViewModel, pointConverter: PointConverter)  = pointerInput(Unit) {

    if (viewModel.imageResult.value == ImageResult.A4_FOUND) {
        detectDragGestures(
            onDragStart = { offset ->
                val point = arrayOf(offset.x.toInt(), offset.y.toInt())
                Log.i(TAG, "DRAG START ${point[0]}, ${point[1]}")
                viewModel.startPoint.value = point
            },
            onDrag = { pointerInputChange, _ ->
                Log.i(TAG, "DRAG ${pointerInputChange.position}")
                val point = arrayOf(
                    pointerInputChange.position.x.toInt(),
                    pointerInputChange.position.y.toInt()
                )
                viewModel.endPoint.value = point
            },
            onDragEnd = {
                if(viewModel.startPoint.value != null && viewModel.endPoint.value != null)
                {
                    viewModel.measureFromStartToEndPoint()
                }
            }
        )
    }
}

fun Modifier.drawCorners(corners:ArrayList<Point>, pointConverter: PointConverter) = drawWithContent {
    drawContent()
    corners.forEach { point ->
        val displayPoint = pointConverter.getDisplayPosition(point)
        drawCircle(
            Color.Red,
            5f,
            displayPoint.toOffset()
        )
    }
}

fun Modifier.drawMeasurement(startPoint: Point?, endPoint: Point?) = drawWithContent {
    drawContent()
    if(startPoint == null || endPoint == null)
    {
        return@drawWithContent
    }

    val startOffset = startPoint.toOffset()
    val endOffset = endPoint.toOffset()

    drawCircle(Color.Blue, 15f, startOffset)
    drawCircle(Color.Blue, 15f, endOffset)
    drawLine(Color.Blue, startOffset, endOffset, 5.0f)
}