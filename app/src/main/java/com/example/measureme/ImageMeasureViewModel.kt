package com.example.measureme

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION
import android.os.Parcelable
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

enum class ImageResult {
    NOT_SEND,
    A4_FOUND,
    A4_NOT_FOUND,
    NO_PICTURE
}

@HiltViewModel
class ImageMeasureViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle
) :ViewModel() {

    // TODO RETHINK IF POSSIBLE
//    val sharedContentState = savedStateHandle.getStateFlow(NavController.KEY_DEEP_LINK_INTENT, Intent())
//        .map { intent -> intent.parseSharedContent() }
//        .stateIn(
//            scope = viewModelScope,
//            started = SharingStarted.WhileSubscribed(5_000),
//            initialValue = ""
//        )


    //    val serverAddress: String = "http://192.168.1.87:8080/"
//    val serverAddress: String = "http://192.168.1.22:8080/"
    var serverAddress = "https://plac.dynu.net/"
    lateinit var connHandler: ConnectionHandler
    var imageUri = mutableStateOf<Uri?>(null)
    var imageResult:MutableState<ImageResult> = mutableStateOf(ImageResult.NO_PICTURE)

    // image data
    var corners: MutableList<Point> = ArrayList()
    var imageId = mutableStateOf("")
    var measurementCm = mutableStateOf(0f) // in cm

    var startPoint :MutableState<Point?> = mutableStateOf(null)
    var endPoint :MutableState<Point?> = mutableStateOf(null)
    
    // UI
    var showPopup = mutableStateOf(false)

    fun sendImage(uri: Uri, context: Context) {
        if(!this::connHandler.isInitialized) {
            connHandler = ConnectionHandler(serverAddress, this)
        }
        connHandler.sendImage(uri, context)
    }

    fun measureFromStartToEndPoint() {
        if(!this::connHandler.isInitialized) {
            return
        }
        val points: PointList = arrayListOf(startPoint.value!!, endPoint.value!!)
        connHandler.sendMeasurement(points, imageId.value)
    }
}
typealias Point = Array<Int>
typealias PointList = MutableList<Point>

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

fun Point.toOffset() : Offset {
    return Offset(this[0].toFloat(), this[1].toFloat())
}

fun Intent.parseSharedContent() : Uri? {
    Log.i("SharingTargetViewModel", "parseSharedContent $action")
    if (action == Intent.ACTION_SEND) {
        if(isImageMimeType()) {
            val imageContent = if (VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
            } else {
                getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as? Uri
            }
            if(imageContent != null)
            {
                Log.i("SharingTargetViewModel", imageContent.toString())
                return imageContent
            }
        }
    }
    return null
}

private const val MIME_TYPE_IMAGE = "image/"
private fun Intent.isImageMimeType() = type?.startsWith(MIME_TYPE_IMAGE) == true

