package com.androidace.echojournal.ui.home

import android.Manifest
import android.content.pm.PackageManager
import android.provider.CalendarContract.Colors
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.core.content.ContextCompat
import com.androidace.echojournal.R

@Composable
fun HomeScreen(
    onShowRecordingSheet: () -> Unit
) {
    val context = LocalContext.current
    // Check if RECORD_AUDIO is already granted
    val hasAudioPermission = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.RECORD_AUDIO
    ) == PackageManager.PERMISSION_GRANTED

    // Launcher to request the RECORD_AUDIO permission
    val audioPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        // Callback when user grants/denies the permission
        if (isGranted) {
            // If granted, display the bottom sheet
            onShowRecordingSheet.invoke()
        } else {
            // Permission denied; handle accordingly (e.g., show a message)
        }
    }
    // Example UI
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                shape = CircleShape,
                onClick = {
                    if (hasAudioPermission) {
                        onShowRecordingSheet.invoke()
                    } else {
                        // Request permission if not granted
                        audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    }
                },
                modifier = Modifier.background(MaterialTheme.colorScheme.onPrimary)) {
                Icon(Icons.Filled.Add, contentDescription = "Add Recording", tint = Color.White)
            }
        }
    ) { paddingValues ->
        Text(
            text = "Home Screen",
            modifier = Modifier.padding(paddingValues)
        )
    }
}