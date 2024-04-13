package com.example.measureme.ui.theme

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*


data class UploadResponse(
    val corners: Array<Array<Int>>
) {

}

interface ApiService {
    @Multipart
    @POST("upload")
    suspend fun uploadImage(
        @Part image:MultipartBody.Part
    ) : Response<UploadResponse>

}

