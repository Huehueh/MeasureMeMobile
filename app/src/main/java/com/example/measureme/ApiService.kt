package com.example.measureme

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*


data class UploadResponse(
    val corners: PointList,
    val id: String
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UploadResponse

        if (!corners.contentDeepEquals(other.corners)) return false

        return true
    }

    override fun hashCode(): Int {
        return corners.contentDeepHashCode()
    }
}

data class MeasureResponse(
    val measurement: Float
) {

}
data class ImageMeasurement (
    val id: String,
    val coordinates: PointList2
)

interface ApiService {
    @Multipart
    @POST("upload")
    suspend fun uploadImage(
        @Part image:MultipartBody.Part,
    ) : Response<UploadResponse>



    @POST("measure")
    suspend fun measure(
        @Body im: ImageMeasurement
    ) : Response<MeasureResponse>

}

