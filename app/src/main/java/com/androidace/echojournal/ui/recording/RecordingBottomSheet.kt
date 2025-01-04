package com.androidace.echojournal.ui.recording

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
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

    // Example: track recording duration in ViewModel, or pass it in
    // For now, we'll hardcode "01:30:45"
    val recordingDuration = "01:30:45"

    // Pulse animation while recording (for the halo effect)
    val infiniteTransition = rememberInfiniteTransition()
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
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
                        recordingViewModel.stopRecording()
                    }
                    onCancel()
                }
            ) {
                // For an "X" icon, you can use Icons.Default.Close or your own vector asset
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Cancel",
                    tint = Color.Red.copy(alpha = 0.7f),
                    modifier = Modifier.size(48.dp)
                )
            }

            // Center Circle with halo
            Box(
                contentAlignment = Alignment.Center
            ) {
                // Halo effect while recording (only if not paused)
                if (isRecording && !isPaused) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .scale(pulseScale)
                            .background(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                shape = CircleShape
                            )
                    )
                }
                // Main circle (Done / Check)
                IconButton(
                    onClick = {
                        // If recording, we might handle "Done" or "Stop"
                        // If paused, also finalize
                        if (isPaused) {
                            recordingViewModel.stopRecording()
                            onDone()
                        } else {
                            recordingViewModel.startRecording()
                        }
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
                        painter = if (isPaused) painterResource(R.drawable.check) else painterResource(
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
                    if (isRecording && !isPaused) {
                        // Pause
                        recordingViewModel.pauseRecording()
                    } else if (isPaused) {
                        // Resume
                        recordingViewModel.stopRecording()
                        onDone()
                    }
                }
            ) {
                val iconDesc = if (isPaused) "Resume" else "Pause"
                Image(
                    painter = if (isPaused) painterResource(R.drawable.ic_pause_small) else painterResource(R.drawable.ic_check_small),
                    contentDescription = iconDesc,
                    modifier = Modifier.size(48.dp)
                )
            }
        }
    }
}
