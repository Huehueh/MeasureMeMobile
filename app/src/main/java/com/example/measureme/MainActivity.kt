package com.example.measureme

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
import com.example.measureme.ui.theme.MeasureMeTheme
import dagger.hilt.android.AndroidEntryPoint

val MAIN_SCREEN = "Main screen"

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

//    private val sharingTargetViewModel: SharingTargetViewModel by viewModels{SharingTargetViewModelFactory(this, intent.extras)}
    private val imageMeasureViewModel: ImageMeasureViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        intent.parseSharedContent()?.let {
            imageMeasureViewModel.imageUri.value = it
        }
        setContent {
            MeasureMeTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    Navigation(
                        navController = navController,
                        imageMeasureViewModel = imageMeasureViewModel
                    )
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Navigation(
        navController: NavHostController,
        imageMeasureViewModel: ImageMeasureViewModel
    ) {
        Column() {
            TextField(
                value = imageMeasureViewModel.serverAddress,
                onValueChange = { imageMeasureViewModel.serverAddress = it },
                label = { Text(text = "IP serwera") }
            )
            NavHost(navController = navController, startDestination = MAIN_SCREEN) {
                composable(MAIN_SCREEN) {
                    MainScreen(
                        imageMeasureViewModel = imageMeasureViewModel
                    )

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
                    ImageUploadFragment(imageMeasureViewModel = imageMeasureViewModel)
                }
            }
        }
    }
}