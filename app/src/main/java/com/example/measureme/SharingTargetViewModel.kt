package com.example.measureme

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION
import android.os.Parcelable
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SharingTargetViewModel @Inject constructor(
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
    var serverAddress = mutableStateOf("http://192.168.1.22:8080/")
    var image = mutableStateOf<Uri?>(null)
    var corners: MutableList<Point> = ArrayList()
    var imageMeasured = mutableStateOf(false)
    var imageId = mutableStateOf("")
    var vector: MutableList<Point> = ArrayList()
    var measurement = mutableStateOf(0f)

    var startPoint :MutableState<Point?> = mutableStateOf(null)
    var endPoint :MutableState<Point?> = mutableStateOf(null)

    enum class ImageResult {
        NOT_SEND,
        A4_FOUND,
        A4_NOT_FOUND,
        UNKNOWN
    }
    var imageResult:MutableState<ImageResult> = mutableStateOf(ImageResult.UNKNOWN)
    var showPopup = mutableStateOf(false)

    fun setImage(str:Uri?) {
        image.value = str
        Log.i("SharingTargetViewModel", "setImage ${image.value}")
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

