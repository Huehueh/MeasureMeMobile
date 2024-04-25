package com.example.measureme

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
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
import androidx.compose.ui.platform.LocalContext
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
    private val sharingTargetViewModel: SharingTargetViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        intent.parseSharedContent()?.let {
            sharingTargetViewModel.setImage(it)
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
                        sharingTargetViewModel = sharingTargetViewModel
                    )
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun Navigation(
        navController: NavHostController,
        sharingTargetViewModel: SharingTargetViewModel
    ) {
        Column() {
            TextField(
                value = sharingTargetViewModel.serverAddress.value,
                onValueChange = { sharingTargetViewModel.serverAddress.value = it },
                label = { Text(text = "IP serwera") }
            )
            NavHost(navController = navController, startDestination = MAIN_SCREEN) {
                composable(MAIN_SCREEN) {
                    MainScreen(
                        sharingTargetViewModel = sharingTargetViewModel
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
                    ImageUploadFragment(sharingTargetViewModel = sharingTargetViewModel)
                }
            }
        }
    }
}