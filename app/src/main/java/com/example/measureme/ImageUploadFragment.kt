package com.example.measureme

import android.content.ContentResolver
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import coil.compose.AsyncImage
import kotlinx.coroutines.flow.collectLatest

var TAG: String = "ImageUpload"

fun createPointConverter(imageUri: Uri, contentResolver: ContentResolver) : PointConverter {
    val src = ImageDecoder.createSource(contentResolver, imageUri)
    val bitmap = ImageDecoder.decodeBitmap(
        src
    ) { decoder, _, _ ->
        decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
        decoder.isMutableRequired = true
    }
    return PointConverter(bitmap.width, bitmap.height)
}


@Composable
fun ImageUploadFragment(imageMeasureViewModel: ImageMeasureViewModel) {

    LaunchedEffect(Unit) {
        snapshotFlow { imageMeasureViewModel.imageUri.value }
            .collectLatest {
                Log.i(TAG, "AAAAAAAAAAAAAAAAAA NOWY OBRAZEK")
                imageMeasureViewModel.imageResult.value = ImageResult.NOT_SEND
            }
    }
    val context = LocalContext.current
    imageMeasureViewModel.imageUri.value?.let { selectedImageUri ->
        Log.i(TAG, "Displaying $selectedImageUri")

        val pointConverter = createPointConverter(selectedImageUri, context.contentResolver)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                AsyncImage(
                    model = selectedImageUri.toString(),
                    contentDescription = "taki obrazek",
                    modifier = Modifier
                        .measureOnDrag(imageMeasureViewModel, pointConverter)
                        .drawMeasurement(
                            imageMeasureViewModel.startPoint.value,
                            imageMeasureViewModel.endPoint.value
                        )
                        .onSizeChanged { imageSize ->
                            pointConverter.setDisplaySize(imageSize.width, imageSize.height)
                        }
                        .drawCorners(
                            imageMeasureViewModel.corners as ArrayList<Point>,
                            pointConverter
                        )
                )
            }
            Text(text = "Corners ${imageMeasureViewModel.corners.size} ${imageMeasureViewModel.corners.display()}")

            when(imageMeasureViewModel.imageResult.value) {
                ImageResult.NOT_SEND -> {
                    Button(onClick = { imageMeasureViewModel.sendImage(selectedImageUri, context) }
                    ) {
                        Text(text = "Wyślij zdjęcie!")
                    }
                }
                ImageResult.A4_FOUND -> {
                    Text(
                        text = "Zdjecie wysłano. Kartka znaleziona!",
                        modifier = Modifier.background(Color.Green)
                    )
                }
                ImageResult.A4_NOT_FOUND -> {
                    Text(
                        text = "Zdjecie wysłano. Kartka nie znaleziona!",
                        modifier = Modifier.background(Color.Red)
                    )
                }

                else -> {}
            }
            Text(text = "Zmierzono ${imageMeasureViewModel.measurementCm.value} cm")
            
            if(imageMeasureViewModel.showPopup.value)
            {
                MyPopup(imageResult = imageMeasureViewModel.imageResult.value) {
                    imageMeasureViewModel.showPopup.value = false
                }
            }
        }
    }
}

@Composable
fun MyPopup(imageResult: ImageResult, onDismiss:() -> Unit) {
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
                .background(Color.Green)
                .clip(RoundedCornerShape(8.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                when(imageResult)
                {
                    ImageResult.A4_FOUND -> Text(text = "Obrazek dotarł! \n Kartka znaleziona!")
                    ImageResult.A4_NOT_FOUND -> Text(text = "Obrazek dotarł! \n Kartka nie znaleziona:(")
                    ImageResult.NOT_SEND -> Text(text = "Obrazek z jakiegoś powodu nie został wysłany:(")
                    else -> {Text(text = "Co to sie porobiło!")}
                }
                Button(onClick = onDismiss) {
                    Text(text = "OK")
                }
            }
        }
    }
}




