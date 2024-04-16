package com.example.measureme

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Build.VERSION
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import dagger.assisted.Assisted
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
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

    var image = mutableStateOf<Uri?>(null)
    fun setImage(str:Uri?) {
        image.value = str
        Log.i("SharingTargetViewModel", "setImage ${image.value}")
    }
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

