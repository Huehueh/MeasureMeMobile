package com.example.measureme

import android.content.Context
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
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

class ConnectionHandler(private val serverAddress: String, private val imageMeasureViewModel: ImageMeasureViewModel) {

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(1, TimeUnit.MINUTES)
        .readTimeout(1, TimeUnit.MINUTES)
        .writeTimeout(1, TimeUnit.MINUTES)
        .build()

    private val api: ApiService = Retrofit.Builder()
        .baseUrl(serverAddress)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ApiService::class.java)

    val coroutineExceptionHandler = CoroutineExceptionHandler(){_, throwable ->
        throwable.printStackTrace()
    }

    init {
        Log.i(TAG, "STARTED")
    }

    @OptIn(DelicateCoroutinesApi::class)
    fun sendMeasurement(pointList: PointList, imageId: String)
    {
        GlobalScope.launch(Dispatchers.IO + coroutineExceptionHandler) {
            Log.i(TAG, "wysylam $imageId points $pointList")
            val h = ImageMeasurement(imageId, pointList)
            val response = api.measure(h)
            if (response.isSuccessful) {
                Log.i(TAG, "HUEHUEHUHEUHHEUHEUHEUEH")
                response.body()?.let {
                    imageMeasureViewModel.measurementCm.value = it.measurement
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
                val corners = if (response.isSuccessful) response.body()!!.corners.display() else "NO CORNERS"
                Log.i(TAG, "Sending image ${file.path}, response $corners")
                if (response.isSuccessful) {
                    response.body()?.let {
                        imageMeasureViewModel.corners = it.corners
                        imageMeasureViewModel.imageId.value = it.id
                        imageMeasureViewModel.startPoint.value = null
                        imageMeasureViewModel.endPoint.value = null
                        imageMeasureViewModel.imageResult.value = ImageResult.A4_FOUND

                        Log.i(TAG, "CORNERS ${imageMeasureViewModel.corners.size}")
                    }

                } else {
                    // Image upload failed, handle the error
                    imageMeasureViewModel.imageResult.value = ImageResult.NOT_SEND // TODO: INNY STAN
                    Log.i(TAG, "a4 not found ${response.code()}")
                }
            }
            catch (e: ConnectException) {
                imageMeasureViewModel.imageResult.value = ImageResult.NOT_SEND
                Log.e(TAG, "ConnectException $e")
            }
            catch (e: SocketTimeoutException) {
                imageMeasureViewModel.imageResult.value = ImageResult.NOT_SEND
                Log.e(TAG, "SocketTimeoutException $e")
            }
            imageMeasureViewModel.showPopup.value = true
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

    private fun getFilePath(uri: Uri, context: Context) : String{
        val filePathHelper = FilePathHelper()
        val path = if (filePathHelper.getPathnew(uri, context) != null) {
            filePathHelper.getPathnew(uri, context).lowercase();
        } else {
            filePathHelper.getFilePathFromURI(uri, context).lowercase();
        }
        return path
    }
}