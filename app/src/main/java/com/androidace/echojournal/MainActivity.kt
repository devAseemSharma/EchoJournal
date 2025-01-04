package com.androidace.echojournal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.androidace.echojournal.ui.home.HomeScreen
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
    var showSheet by remember { mutableStateOf(false) }

    // The global bottom sheet, visible if showSheet == true
    if (showSheet) {
        ModalBottomSheet(
            sheetState = sheetState,
            onDismissRequest = {
                showSheet = false
            }
        ) {
            RecordingBottomSheet(
                onCancel = {
                    coroutineScope.launch { sheetState.hide() }
                    showSheet = false
                },
                onDone = {
                    coroutineScope.launch { sheetState.hide() }
                    showSheet = false
                }
            )
        }
    }

    // Your NavHost or main UI content
    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(
                // Pass in a callback so HomeScreen can show the sheet
                onShowRecordingSheet = {
                    showSheet = true
                    coroutineScope.launch { sheetState.show() }
                }
            )
        }


        // Additional screens...
    }
}