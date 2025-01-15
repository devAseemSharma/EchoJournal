package com.androidace.echojournal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.androidace.echojournal.ui.home.Home
import com.androidace.echojournal.ui.home.HomeScreen
import com.androidace.echojournal.ui.mood.MoodBottomSheet
import com.androidace.echojournal.ui.newentry.NewEntry
import com.androidace.echojournal.ui.newentry.NewEntryScreen
import com.androidace.echojournal.ui.recording.RecordingBottomSheet
import com.androidace.echojournal.ui.theme.EchoJournalTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EchoJournalTheme {
                MainRoot()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainRoot() {
    val navController = rememberNavController()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()

    // A shared boolean that any screen can toggle
    var showRecordingSheet by remember { mutableStateOf(false) }
    var showMoodSheet by remember { mutableStateOf(true) }

    // The global bottom sheet, visible if showSheet == true
    if (showRecordingSheet) {
        ModalBottomSheet(
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.background,
            onDismissRequest = {
                showRecordingSheet = false
            }
        ) {
            RecordingBottomSheet(
                onCancel = {
                    coroutineScope.launch { sheetState.hide() }
                    showRecordingSheet = false
                },
                onDone = {
                    coroutineScope.launch { sheetState.hide() }
                    showRecordingSheet = false
                }
            )
        }
    }

    if (showMoodSheet) {
        ModalBottomSheet(
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.background,
            onDismissRequest = {
                showMoodSheet = false
            }
        ) {
            MoodBottomSheet(onCancel = {

            }, onConfirm = {

            })
        }
    }

    // Your NavHost or main UI content
    NavHost(
        navController = navController,
        startDestination = Home
    ) {
        composable<Home> {
            HomeScreen(
                // Pass in a callback so HomeScreen can show the sheet
                onShowRecordingSheet = {
                    navController.navigate(NewEntry(""))
                    /*showRecordingSheet = true
                    coroutineScope.launch { sheetState.show() }*/
                }
            )
        }

        composable<NewEntry> {
            val args = it.toRoute<NewEntry>()
            NewEntryScreen(args.recordingPath)
        }

        // Additional screens...
    }
}