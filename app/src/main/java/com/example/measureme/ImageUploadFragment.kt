package com.example.measureme

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.net.ConnectException
import java.net.SocketTimeoutException

var TAG: String = "ImageUpload"

val serverAddress: String = "http://192.168.1.87:8080/"
//val serverAddress: String = "http://192.168.1.22:8080/"

val api: ApiService = Retrofit.Builder()
    .baseUrl(serverAddress)
    .addConverterFactory(GsonConverterFactory.create())
    .build()
    .create(ApiService::class.java)



@Composable
fun ImageUploadFragment(sharingTargetViewModel: SharingTargetViewModel) {
    val context = LocalContext.current
    sharingTargetViewModel.image.value?.let { selectedImageUri ->
        Log.i(TAG, "Displaying $selectedImageUri")
        var endPoint = mutableStateOf<Point?>(null)
        Column() {
            Text(text = "huehue")
            Box(modifier = Modifier.fillMaxWidth()) {
                AsyncImage(
                    model = selectedImageUri.toString(),
                    contentDescription = "taki obrazek",
                    modifier = Modifier.pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = {
                                var point = arrayOf<Int>(it.x.toInt(), it.y.toInt())
                                Log.i(TAG, "DRAG START ${point[0]}, ${point[1]}")
                                sharingTargetViewModel.measurement = mutableListOf()
                                sharingTargetViewModel.measurement.add(point)
//                                  pointStart.x = it.x.toInt()
//                                  pointStart.y = it.y.toInt()
                            },
                            onDrag = { pointerInputChange, _ ->
                                Log.i(TAG, "DRAG ${pointerInputChange.position}")
                                var point = arrayOf<Int>(pointerInputChange.position.x.toInt(), pointerInputChange.position.y.toInt())
                                endPoint.value = point
                            },
                            onDragEnd = {
                                sharingTargetViewModel.measurement.add(endPoint.value!!)
                                sharingTargetViewModel.measurement.let { pointList ->
                                    sendMeasurement(pointList, sharingTargetViewModel.imageId.value)
                                    Log.i(TAG, "START ${pointList[0][0]},  ${pointList[0][1]}, END ${pointList[1][0]},  ${pointList[1][1]}")
                                }

                            }
                        )
                    }
                )
                Canvas(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    sharingTargetViewModel.corners.value.forEach { point  ->
                        drawCircle(Color.Red, 5f, Offset(point[0].toFloat(), point[1].toFloat()))
                    }
                    sharingTargetViewModel.measurement.forEach { point ->
                        drawCircle(Color.Blue, 15f, Offset(point[0].toFloat(), point[1].toFloat()))
                    }
                }
            }

            Button(onClick = {
                sendImage(selectedImageUri, sharingTargetViewModel, context)
            }) {
                Text(text = "Measure me!")
            }
            Text(text = sharingTargetViewModel.corners.value.display())
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


fun sendMeasurement(pointList: PointList2, imageId: String)
{
    GlobalScope.launch {
        Log.i(TAG, "wysylam $imageId points $pointList")
        val h = ImageMeasurement(imageId, pointList)
        val response = api.measure(h)
        if (response.isSuccessful) {
            Log.i(TAG, "HUEHUEHUHEUHHEUHEUHEUEH")
        }
    }
}

@OptIn(DelicateCoroutinesApi::class)
fun sendImage(file: File, sharingTargetViewModel: SharingTargetViewModel) {
    val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
    val imagePart = MultipartBody.Part.createFormData(
        "image",
        file.name,
        requestFile
    )
    GlobalScope.launch {
        try {
            val response = api.uploadImage(imagePart)
            Log.i(TAG, "Sending image ${file.path}, response ${response.isSuccessful}")
            if (response.isSuccessful) {
                response.body()?.let {
                    it.corners?.let { pointList ->
                        sharingTargetViewModel.corners.value = pointList
                    }
                    it.id.let { imageId ->
                        sharingTargetViewModel.imageId.value = imageId
                    }
                }
                Log.i(TAG, "ID ${response.body()?.id}")


            } else {
                // Image upload failed, handle the error

                Log.i(TAG, "response not successful")
            }
        }
        catch (e: ConnectException) {
            Log.e(TAG, "ConnectException")
        }
        catch (e: SocketTimeoutException) {
            Log.e(TAG, "SocketTimeoutException")
        }
    }
}

fun sendImage(uri: Uri, sharingTargetViewModel: SharingTargetViewModel, context: Context) {
    val path = getFilePath(uri, context)
    val file = File(path)
    if (file.exists()) {
        sendImage(file, sharingTargetViewModel)
    }
    else {
        Log.i("URL", "No such file ${file.absolutePath}")
    }
}

//fun Intent.parseImageContent() : Uri? {
//    val imageContent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//        getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
//    } else {
//        getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as? Uri
//    }
//    if(imageContent != null)
//    {
//        Log.i(TAG, "parsed $imageContent")
//        return imageContent
//    }
//    return null
//}