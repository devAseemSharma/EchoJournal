package com.androidace.echojournal.ui.recording

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.androidace.echojournal.R

@Composable
fun RecordingBottomSheet(
    recordingViewModel: RecordingViewModel = hiltViewModel(),
    onCancel: () -> Unit = {},
    onDone: () -> Unit = {}
) {
    val isRecording by recordingViewModel.isRecording.collectAsStateWithLifecycle()
    val isPaused by recordingViewModel.isPaused.collectAsStateWithLifecycle()

    val haptic = LocalHapticFeedback.current

    // Collect elapsed time in ms
    val elapsedMs by recordingViewModel.elapsedTimeMs.collectAsStateWithLifecycle()

    // Format the elapsed time as HH:MM:SS
    val recordingDuration = formatMillis(elapsedMs)

    // Pulse animation while recording (for the halo effect)
    val infiniteTransition = rememberInfiniteTransition(label = "infiniteTransition")

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
        targetValue = 1f,
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

    // Outer Container
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp)  // extra bottom padding for styling
            .background(Color.White), // or a light gray if you'd like
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Title
        Text(
            text = "Recording your memories...",
            style = MaterialTheme.typography.bodyLarge.copy(color = Color.Black),
            modifier = Modifier.padding(top = 24.dp)
        )

        // Timer
        Text(
            text = recordingDuration,
            style = MaterialTheme.typography.titleMedium.copy(color = Color.Gray),
            modifier = Modifier
                .padding(top = 8.dp)
        )

        Spacer(modifier = Modifier.height(40.dp))

        // Row for buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Cancel Button (X)
            IconButton(
                onClick = {
                    if (isRecording || isPaused) {
                        recordingViewModel.cancelRecording()
                    }
                    onCancel()
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                }
            ) {
                // For an "X" icon, you can use Icons.Default.Close or your own vector asset
                Image(
                    painter = painterResource(R.drawable.ic_cancel_recording),
                    contentDescription = "Cancel",
                    modifier = Modifier.size(48.dp)
                )
            }

            // Center Circle with halo
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(140.dp)
            ) {
                // Halo effect while recording (only if not paused)
                if (isRecording && !isPaused) {
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .graphicsLayer {
                                scaleX = rippleInitialScale
                                scaleY = rippleInitialScale
                                alpha = rippleInitAlpha
                            }
                            .background(
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f),
                                shape = CircleShape
                            )
                    )
                    // Ripple #2 (lighter or darker shade, or alpha variation)
                    Box(
                        modifier = Modifier
                            .size(140.dp)
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
                }
                // Main circle (Done / Check)
                IconButton(
                    onClick = {
                        // If recording, we might handle "Done" or "Stop"
                        // If paused, also finalize

                        if (!isPaused && isRecording) {
                            recordingViewModel.stopRecording()
                            onDone()
                        } else if (isPaused && !isRecording) {
                            recordingViewModel.resumeRecording()
                        } else {
                            recordingViewModel.startRecording()
                        }
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    },
                    modifier = Modifier
                        .size(80.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = CircleShape
                        )
                ) {
                    // Check icon (you can swap with your own)
                    Image(
                        painter = if (isRecording) painterResource(R.drawable.check) else painterResource(
                            R.drawable.mic
                        ),
                        contentDescription = "Done",
                        modifier = Modifier.size(80.dp)
                    )
                }
            }

            // Pause Button (||)
            IconButton(
                onClick = {
                    if (!isPaused) {
                        // Pause
                        recordingViewModel.pauseRecording()
                    } else if (!isRecording) {
                        // Resume
                        recordingViewModel.stopRecording()
                        onDone()
                    }
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                }
            ) {
                val iconDesc = if (isPaused) "Resume" else "Pause"
                Image(
                    painter = if (isPaused) painterResource(R.drawable.ic_check_small) else painterResource(
                        R.drawable.ic_pause_small
                    ),
                    contentDescription = iconDesc,
                    modifier = Modifier.size(48.dp)
                )
            }
        }
    }
}

// Helper to format milliseconds to HH:MM:SS
fun formatMillis(millis: Long): String {
    val totalSeconds = millis / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60

    return if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        // If you prefer mm:ss when hours == 0
        String.format("%02d:%02d", minutes, seconds)
    }
}
