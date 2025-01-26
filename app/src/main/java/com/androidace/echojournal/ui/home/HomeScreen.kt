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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.focus.FocusRequester.Companion.createRefs
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.androidace.echojournal.R
import com.androidace.echojournal.db.Topic
import com.androidace.echojournal.ui.common.timeline.LineStyle
import com.androidace.echojournal.ui.common.timeline.Timeline
import com.androidace.echojournal.ui.common.timeline.TimelineOrientation
import com.androidace.echojournal.ui.common.timeline.getLineType
import com.androidace.echojournal.ui.home.model.TimelineEntry
import com.androidace.echojournal.ui.mood.model.Mood
import com.androidace.echojournal.ui.newentry.TopicChip
import com.androidace.echojournal.ui.recording.RecordingViewModel
import com.androidace.echojournal.ui.theme.moodColorPaletteMap
import com.androidace.echojournal.util.formatMillis
import java.time.LocalDate
import java.time.format.DateTimeFormatter
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
        LazyColumn(modifier = Modifier.background(color = Color.LightGray)) {
            itemsIndexed(items = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)) { position, item ->
                TimelineRow(
                    TimelineEntry(
                        id = 1,
                        mood = Mood.PEACEFUL,
                        title = "New Entry",
                        description = "Book Reading",
                        createdAt = LocalDate.now().toEpochDay(),
                        topics = listOf(
                            Topic(topicId = 1, name = "Android")
                        ),
                        audioPath = "",
                        audioDuration = "00:00"
                    ), position, 10
                )
            }
        }
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
                recordingViewModel.stopRecording {
                    onDone.invoke()
                }
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

@Composable
fun DayHeader(date: LocalDate) {
    val today = LocalDate.now()
    val label = when (date) {
        today -> "TODAY"
        today.minusDays(1) -> "YESTERDAY"
        else -> date.format(DateTimeFormatter.ofPattern("EEEE, MMM d"))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TimelineRow(entry: TimelineEntry, position: Int, totalItems: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .padding(horizontal = 16.dp)
    ) {
        // Left side: icon + timeline line
        Timeline(
            modifier = Modifier.fillMaxHeight(),
            lineType = getLineType(position, totalItems),
            orientation = TimelineOrientation.Vertical,
            lineStyle = LineStyle.solid(
                color = Color(0xFF4CAF50),
                width = 2.dp
            ),
            marker = {
                Image(
                    painter = painterResource(id = entry.mood.activeResId),
                    contentDescription = "Mood Icon",
                    modifier = Modifier
                        .size(24.dp)
                )
            }
        )

        // Right side: the content card
        Card(
            modifier = Modifier
                .padding(start = 8.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                // Title
                Text(
                    text = entry.title,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                // Audio player row (placeholder or custom UI)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(R.drawable.ic_play),
                        contentDescription = null,
                        tint = moodColorPaletteMap[entry.mood]?.darkColor
                            ?: MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = entry.audioDuration)
                }

                // Optional: show partial or full description
                if (!entry.description.isNullOrBlank()) {
                    Text(
                        text = entry.description,
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Topics row
                if (entry.topics.isNotEmpty()) {
                    FlowRow(
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        entry.topics.forEach { topic ->
                            TopicChip(topic)
                        }
                    }
                }

                // Time text (e.g., 17:30)
                val timeText = formatMillis(entry.createdAt)
                Text(
                    text = timeText,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 4.dp)
                )
            }
        }
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

@Preview
@Composable
private fun PreviewTimelineRow() {
    TimelineRow(
        TimelineEntry(
            id = 1,
            mood = Mood.PEACEFUL,
            title = "New Entry",
            description = "Book Reading",
            createdAt = LocalDate.now().toEpochDay(),
            topics = listOf(
                Topic(topicId = 1, name = "Android")
            ),
            audioPath = "",
            audioDuration = "00:00"
        ), 0, 3
    )
}