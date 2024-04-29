package com.example.measureme

import android.content.Context
import android.graphics.ImageDecoder
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import coil.compose.AsyncImage

var TAG: String = "ImageUpload"

@Composable
fun ImageUploadFragment(sharingTargetViewModel: SharingTargetViewModel) {
    val context = LocalContext.current
    val connHandler = ConnectionHandler(sharingTargetViewModel)
    sharingTargetViewModel.image.value?.let { selectedImageUri ->
        Log.i(TAG, "Displaying $selectedImageUri")
        val src = ImageDecoder.createSource(context.contentResolver, selectedImageUri)
        val bitmap = ImageDecoder.decodeBitmap(
                src
        ) { decoder, _, _ ->
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
            decoder.isMutableRequired = true
        }
        val pointConverter = PointConverter(bitmap.width, bitmap.height)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Text(text = "Size ${bitmap.width} ${bitmap.height}")
            Box(modifier = Modifier.fillMaxWidth()) {
                AsyncImage(
                    model = selectedImageUri.toString(),
                    contentDescription = "taki obrazek",
                    modifier = Modifier
                        .pointerInput(Unit) {
                            if (sharingTargetViewModel.imageResult.value == SharingTargetViewModel.ImageResult.A4_FOUND) {
                                detectDragGestures(
                                    onDragStart = {
                                        val point = arrayOf<Int>(it.x.toInt(), it.y.toInt())
                                        Log.i(TAG, "DRAG START ${point[0]}, ${point[1]}")
                                        sharingTargetViewModel.startPoint.value = point
                                    },
                                    onDrag = { pointerInputChange, _ ->
                                        Log.i(TAG, "DRAG ${pointerInputChange.position}")
                                        val point = arrayOf<Int>(
                                            pointerInputChange.position.x.toInt(),
                                            pointerInputChange.position.y.toInt()
                                        )
                                        sharingTargetViewModel.endPoint.value = point
                                    },
                                    onDragEnd = {
                                        sharingTargetViewModel.let {
                                            if (it.startPoint.value != null && it.endPoint.value != null) {
                                                it.vector = ArrayList()
                                                it.vector += pointConverter.getImagePosition(it.startPoint.value!!)
                                                it.vector += pointConverter.getImagePosition(it.endPoint.value!!)
                                                if (sharingTargetViewModel.imageMeasured.value) {
                                                    connHandler.sendMeasurement(
                                                        it.vector,
                                                        it.imageId.value
                                                    )
                                                }
                                            }
                                        }
                                    }
                                )
                            }
                        }
                        .drawWithContent {
                            drawContent()
                            sharingTargetViewModel.let {
                                Log.i(TAG, "AAAAAAAAAAAAAAAAAA ${it.corners.size}")
                                it.corners.forEach { point ->
                                    val displayPoint = pointConverter.getDisplayPosition(point)
                                    drawCircle(
                                        Color.Red,
                                        5f,
                                        Offset(displayPoint[0].toFloat(), displayPoint[1].toFloat())
                                    )
                                }
                                if (it.startPoint.value != null && it.endPoint.value != null) {
                                    drawCircle(Color.Blue, 15f, it.startPoint.value!!.toOffset())
                                    drawCircle(Color.Blue, 15f, it.endPoint.value!!.toOffset())
                                    drawLine(
                                        Color.Blue,
                                        it.startPoint.value!!.toOffset(),
                                        it.endPoint.value!!.toOffset(),
                                        5.0f
                                    )
                                }

                            }
                        }
                        .onSizeChanged {
                            pointConverter.setDisplaySize(it.width, it.height)
                        }
                )
            }
            Text(text = "Corners ${sharingTargetViewModel.corners.size} ${sharingTargetViewModel.corners.display()}")

            Button(onClick = {
                connHandler.sendImage(selectedImageUri, context)
            }) {
                Text(text = "Measure me!")
            }


            
            Text(text = "Zmierzono ${sharingTargetViewModel.measurement.value} cm")
            
            if(sharingTargetViewModel.showPopup.value)
            {
                MyPopup(imageResult = sharingTargetViewModel.imageResult.value) {
                    sharingTargetViewModel.showPopup.value = false
                }
            }
        }
    }
}

@Composable
fun MyPopup(imageResult: SharingTargetViewModel.ImageResult, onDismiss:() -> Unit) {
    Popup(
        alignment = Alignment.Center,
        properties = PopupProperties(
            excludeFromSystemGesture = true
        ),
        onDismissRequest = onDismiss
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction = 0.8f)
                .background(Color.White)
                .clip(RoundedCornerShape(4.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
//                    .fillMaxSize()
                    .align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                when(imageResult)
                {
                    SharingTargetViewModel.ImageResult.A4_FOUND -> Text(text = "Obrazek dotarł! \n Kartka znaleziona!")
                    SharingTargetViewModel.ImageResult.A4_NOT_FOUND -> Text(text = "Obrazek dotarł! \n Kartka nie znaleziona:(")
                    SharingTargetViewModel.ImageResult.NOT_SEND -> Text(text = "Obrazek z jakiegoś powodu nie został wysłany:(")
                    else -> {Text(text = "Co to sie porobiło!")}
                }
                Button(onClick = onDismiss) {
                    Text(text = "OK")
                }
            }
        }
    }
}

fun getFilePath(uri: Uri, context: Context) : String{
    val filePathHelper = FilePathHelper()
    var path = if (filePathHelper.getPathnew(uri, context) != null) {
        filePathHelper.getPathnew(uri, context).lowercase();
    } else {
        filePathHelper.getFilePathFromURI(uri, context).lowercase();
    }
    return path
}


