package com.example.measureme

import android.content.Context
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit

class ConnectionHandler(val sharingTargetViewModel: SharingTargetViewModel) {

//    val serverAddress: String = "http://192.168.1.87:8080/"
//    val serverAddress: String = "http://192.168.1.22:8080/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(1, TimeUnit.MINUTES)
        .readTimeout(1, TimeUnit.MINUTES)
        .writeTimeout(1, TimeUnit.MINUTES)
        .build()

    private val api: ApiService = Retrofit.Builder()
        .baseUrl(sharingTargetViewModel.serverAddress.value)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ApiService::class.java)

    @OptIn(DelicateCoroutinesApi::class)
    fun sendMeasurement(pointList: PointList, imageId: String)
    {
        GlobalScope.launch {
            Log.i(TAG, "wysylam $imageId points $pointList")
            val h = ImageMeasurement(imageId, pointList)
            val response = api.measure(h)
            if (response.isSuccessful) {
                Log.i(TAG, "HUEHUEHUHEUHHEUHEUHEUEH")
                response.body()?.let {
                    sharingTargetViewModel.measurement.value = it.measurement
                }
            }
        }
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
                Log.i(TAG, "Sending image ${file.path}, response ${response.isSuccessful} corners ${response.body()!!.corners.display()}")
                if (response.isSuccessful) {
                    response.body()?.let {
                        sharingTargetViewModel.corners = it.corners
                        sharingTargetViewModel.imageId.value = it.id
                        sharingTargetViewModel.imageMeasured.value = true
                        sharingTargetViewModel.startPoint.value = null
                        sharingTargetViewModel.endPoint.value = null
                        sharingTargetViewModel.imageResult.value = SharingTargetViewModel.ImageResult.A4_FOUND

                        Log.i(TAG, "CORNERS ${sharingTargetViewModel.corners.size}")
                    }

                } else {
                    // Image upload failed, handle the error
                    sharingTargetViewModel.imageResult.value = SharingTargetViewModel.ImageResult.NOT_SEND
                    Log.i(TAG, "response not successful")
                }
            }
            catch (e: ConnectException) {
                sharingTargetViewModel.imageResult.value = SharingTargetViewModel.ImageResult.NOT_SEND
                Log.e(TAG, "ConnectException")
            }
            catch (e: SocketTimeoutException) {
                sharingTargetViewModel.imageResult.value = SharingTargetViewModel.ImageResult.NOT_SEND
                Log.e(TAG, "SocketTimeoutException")
            }
            sharingTargetViewModel.showPopup.value = true
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
}