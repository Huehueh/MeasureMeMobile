package com.example.measureme

import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

typealias Point = Array<Int>
typealias PointList = Array<Point>

fun PointList.display() : String {
    var ret = ""
    forEach { point ->
        ret += "("
        ret += point[0]
        ret += ", "
        ret += point[1]
        ret += ")"
    }
    return ret
}

data class UploadResponse(
    val corners: PointList
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

interface ApiService {
    @Multipart
    @POST("upload")
    suspend fun uploadImage(
        @Part image:MultipartBody.Part
    ) : Response<UploadResponse>

}

