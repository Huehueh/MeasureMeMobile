package com.example.measureme

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Parcelable
import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

val api: ApiService = Retrofit.Builder()
    .baseUrl(serverAddress)
    .addConverterFactory(GsonConverterFactory.create())
    .build()
    .create(ApiService::class.java)

var result = mutableStateOf<String>("")

@Composable
//fun ImageUploadFragment(intent: Intent) {
fun ImageUploadFragment(sharingTargetViewModel: SharingTargetViewModel) {
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    selectedImageUri = sharingTargetViewModel.image.value
    val context = LocalContext.current
    Log.i("SharingTargetViewModel", "ImageUploadFragment DISPLAY $selectedImageUri")
    Column() {
        AsyncImage(
            model = selectedImageUri.toString(),
            contentDescription = "taki obrazek"
        )
        Button(onClick = {
            selectedImageUri?.let { sendImage(it, context) }
        }) {
            Text(text = "Upload image")
        }
        Text(text = result.value)
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

@OptIn(DelicateCoroutinesApi::class)
fun sendImage(file: File) {
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
                val corners = response.body()?.corners
                if (corners != null) {
                    result.value = corners.display()
                }
            } else {
                // Image upload failed, handle the error

                Log.i(TAG, "response not successful")
            }
        }
        catch (e: ConnectException) {
            Log.e(TAG, "ConnectException")
            result.value = "ConnectException"
        }
        catch (e: SocketTimeoutException) {
            Log.e(TAG, "SocketTimeoutException")
            result.value = "SocketTimeoutException"
        }
    }
}

fun sendImage(uri: Uri, context: Context) {
    val path = getFilePath(uri, context)
    val file = File(path)
    if (file.exists()) {
        sendImage(file)
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