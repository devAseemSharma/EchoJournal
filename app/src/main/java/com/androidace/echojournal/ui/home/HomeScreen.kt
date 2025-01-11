package com.androidace.echojournal.ui.home

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.androidace.echojournal.R
import com.androidace.echojournal.ui.recording.RecordingViewModel
import kotlin.math.sqrt

@Composable
fun HomeScreen(
    onShowRecordingSheet: () -> Unit,
    viewModel: RecordingViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    // Check if RECORD_AUDIO is already granted
    val hasAudioPermission = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.RECORD_AUDIO
    ) == PackageManager.PERMISSION_GRANTED

    // Whether to show the recording UI
    var showRecording by remember { mutableStateOf(false) }

    var onTapClicked by remember { mutableStateOf(false) }
    var onLongPressClicked by remember { mutableStateOf(false) }

    // Launcher to request the RECORD_AUDIO permission
    val audioPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        // Callback when user grants/denies the permission
        if (isGranted) {
            // If granted, display the bottom sheet
            if (onTapClicked) {
                onShowRecordingSheet.invoke()
                onTapClicked = false
            }
            if (onLongPressClicked) {
                showRecording = true
                viewModel.startRecording()
                onLongPressClicked = false
            }
        } else {
            // Permission denied; handle accordingly (e.g., show a message)
        }
    }


    // Example UI
    Scaffold(
        floatingActionButton = {
            AnimatedVisibility(!showRecording) {
                Box(contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(70.dp)
                        .pointerInput(Unit) {
                            detectTapGestures(
                                // onLongPress triggers the "recording UI"
                                onLongPress = {
                                    onLongPressClicked = true
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    if (hasAudioPermission) {
                                        showRecording = true
                                    } else {
                                        // Request permission if not granted
                                        audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                },
                                onTap = {
                                    onTapClicked = true
                                    if (hasAudioPermission) {
                                        onShowRecordingSheet.invoke()
                                    } else {
                                        // Request permission if not granted
                                        audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                    }
                                }
                                // You can also provide onTap if you want a short press behavior
                            )
                        }
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_add_fab),
                        contentDescription = "Add Icon",
                    )
                }
            }
        }
    ) { paddingValues ->
        Text(
            text = "Home Screen",
            modifier = Modifier.padding(paddingValues)
        )
        // Overlay the "recording UI" if showRecording == true
        if (showRecording) {
            RecordingUI(
                onCancel = {
                    // Handle the user canceling
                    showRecording = false
                },
                hasAudioPermission = hasAudioPermission,
                recordingViewModel = viewModel,
                onDone = {
                    // If you had a "Done" button, handle it
                    showRecording = false
                }
            )
        }
    }
}

@Composable
fun RecordingUI(
    onCancel: () -> Unit,
    onDone: () -> Unit = {},
    hasAudioPermission: Boolean,
    recordingViewModel: RecordingViewModel
) {
    val haptic = LocalHapticFeedback.current
    LaunchedEffect(Unit) {
        if (hasAudioPermission) {
            recordingViewModel.startRecording()
        }
    }

    // A simple full-screen box overlay
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.BottomEnd
    ) {
        // The "recording" area (a row or column with record and cancel)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            modifier = Modifier.size(width = 190.dp, height = 152.dp),
        ) {
            // Cancel button
            CancelButton(
                onCancel = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    recordingViewModel.cancelRecording()
                    onCancel.invoke()
                }
            )
            // Recording "button" or text
            PulseRecordingButton {
                recordingViewModel.stopRecording()
                onDone.invoke()
            }
        }
    }
}

@Composable
fun PulseRecordingButton(
    onClick: () -> Unit
) {
    // Infinite transition for pulse
    val infiniteTransition = rememberInfiniteTransition(label = "")
    val waveDuration = 1200  // total time for one ripple expansion
    val ripple2Delay = 400

    // First ripple expands from scale=0 to scale=1
    val rippleInitialScale by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = waveDuration, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ), label = ""
    )
    val rippleInitAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = waveDuration, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ), label = ""
    )
    // Second ripple also expands 0 -> 1,
    // but we offset its start by half the duration (600ms)
    val rippleSecondScale by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = waveDuration, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart,
            // Start after 600ms
            initialStartOffset = StartOffset(ripple2Delay)
        ), label = ""
    )
    val rippleSecondAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = waveDuration, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Restart,
            initialStartOffset = StartOffset(ripple2Delay)
        ), label = ""
    )

    // Wrap everything in a Box so we can put the pulse behind the button
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(100.dp)
    ) {
        // Pulse circle behind
        Box(
            modifier = Modifier
                .size(100.dp),
            contentAlignment = Alignment.Center
        ) {
            // Ripple #1
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .graphicsLayer {
                        // scale from 0 to 1
                        scaleX = rippleInitialScale
                        scaleY = rippleInitialScale
                        // fade out (1f to 0f)
                        alpha = rippleInitAlpha
                    }
                    .background(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
                        shape = CircleShape
                    )
            )
            // Ripple #2 (lighter or darker shade, or alpha variation)
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .graphicsLayer {
                        scaleX = rippleSecondScale
                        scaleY = rippleSecondScale
                        alpha = rippleSecondAlpha
                    }
                    .background(
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                        shape = CircleShape
                    )
            )
            // Center content if you wish, e.g., an icon or text
        }

        // Actual button (or circle)
        // For demonstration, a standard Button with text
        IconButton(
            onClick = onClick,
            modifier = Modifier.size(64.dp)
        ) {
            // Check icon (you can swap with your own)
            Image(
                painter = painterResource(R.drawable.mic),
                contentDescription = "Done",
                modifier = Modifier.size(64.dp)
            )
        }
    }
}

@Composable
fun CancelButton(
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Interaction source to detect press
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    // Whether we've triggered the click (and thus the animation)
    var hasClicked by remember { mutableStateOf(false) }

    // Haptic feedback
    val haptic = LocalHapticFeedback.current

    // Animate scale from 1f -> 1.5f
    val animatedScale by animateFloatAsState(
        targetValue = if (isPressed) 1.5f else 1f,
        animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing),
        finishedListener = {
            // Once we've reached the end of the animation
            if (hasClicked) {
                onCancel()
                // Optionally reset hasClicked so the button can be reused
                hasClicked = false
            }
        },
        label = "scaleButtonAnim"
    )

    // When user first presses down, do haptic feedback (one-time)
    // We'll watch `isPressed` transitions from false -> true
    LaunchedEffect(isPressed) {
        if (isPressed) {
            // Perform a "LongPress" or "HeavyClick" haptic as you prefer
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    // The actual button
    IconButton(
        onClick = {
            // When the user releases finger (click), finalize the cancel
            onCancel()
        },
        interactionSource = interactionSource,
        modifier = modifier
            .scale(animatedScale)
            .padding(8.dp),

        ) {
        Image(
            painter = painterResource(R.drawable.ic_cancel_recording),
            contentDescription = "Cancel"
        )
    }
}

private fun Offset.distanceTo(other: Offset): Float {
    val dx = x - other.x
    val dy = y - other.y
    return sqrt(dx * dx + dy * dy)
}


fun Dp.toPx(density: Density): Float {
    return with(density) {
        this@toPx.toPx()
    }
}
