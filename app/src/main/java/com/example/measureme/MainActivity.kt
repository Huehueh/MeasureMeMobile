package com.example.measureme

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import coil.compose.AsyncImage
import com.example.measureme.ui.theme.ApiService
import com.example.measureme.ui.theme.MeasureMeTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.Locale


val MAIN_SCREEN = "Main screen"
val PHOTO_SCREEN = "Photo screen"




@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val sharingTargetViewModel: SharingTargetViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MeasureMeTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    Navigation(
                        navController = navController,
                        sharingTargetViewModel = sharingTargetViewModel
                    )
                }
            }
        }



    }

    @Composable
    fun Navigation(
        navController: NavHostController,
        sharingTargetViewModel: SharingTargetViewModel
    ) {
        var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
        val api = Retrofit.Builder()
            .baseUrl(" http://192.168.1.87:8080/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)

        NavHost(navController = navController, startDestination = MAIN_SCREEN) {
            composable(MAIN_SCREEN) {
                Text("Hello Kinolo!")
            }
            composable(
                route = "share_target_route",
                deepLinks = listOf(
                        navDeepLink {
                        action = Intent.ACTION_SEND
                        mimeType = "image/*"
                    }
                )
            ) {
                if(intent.isImageMimeType()) {
                    selectedImageUri = intent.parseSharedContent()
                    Log.i("URL", selectedImageUri.toString())
                    AsyncImage(
                        model = selectedImageUri.toString(),
                        contentDescription = "taki obrazek"
                    )

                    Button(onClick = {
                        selectedImageUri?.let {uri ->
                            Log.i("URL", "URI" + uri.path!!)
                            var filePathHelper = FilePathHelper()
                            var path = ""
                            if (filePathHelper.getPathnew(uri, applicationContext) != null) {
                                path = filePathHelper.getPathnew(uri, applicationContext).lowercase();
                            } else {
                                path = filePathHelper.getFilePathFromURI(uri, applicationContext).lowercase();
                            }
                            var file = File(path)
                            Log.i("URL", "PATH $path")
                            if (file.exists()) {
                                Log.i("URL", "JEST PATH $path")
                                val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                                val imagePart = MultipartBody.Part.createFormData(
                                    "image",
                                    file.name,
                                    requestFile
                                )
                                Log.i("URL", "launching $path")
                                GlobalScope.launch {
                                    val response = api.uploadImage(imagePart)
                                    Log.i("URL", "response ${response.message()}")
                                    if (response.isSuccessful) {
                                        // Image upload successful, handle the response
                                        val corners = response.body()?.corners
                                        Log.i("URL", corners.toString())
                                        // Do something with imageUrl
                                    } else {
                                        // Image upload failed, handle the error

                                        Log.i("URL", "poruta")
                                        // Handle the error
                                    }
                                }
                            }
                            else {
                                Log.i("URL", "NIE MA")
                            }

                        }
                    }) {
                        Text(text = "Upload image")
                    }
                }

            }
        }
    }
}

fun uploadImage() {

}

fun Intent.parseSharedContent() : Uri? {
    Log.i("SharingTargetViewModel", "parseSharedContent $action")
//    if (action == Intent.ACTION_SEND) {
//        Log.i("SharingTargetViewModel", "ACTION_SEND")

    if(isImageMimeType()) {
        Log.i("SharingTargetViewModel", "image")
//            val imageContent = getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as? Uri
        val imageContent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
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
//    }
    return null
}
private const val MIME_TYPE_IMAGE = "image/"
private fun Intent.isImageMimeType() = type?.startsWith(MIME_TYPE_IMAGE) == true