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
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.androidace.echojournal.R
import com.androidace.echojournal.audio.waveform.AudioWaveform
import com.androidace.echojournal.db.Topic
import com.androidace.echojournal.ui.common.timeline.LineStyle
import com.androidace.echojournal.ui.common.timeline.Timeline
import com.androidace.echojournal.ui.common.timeline.TimelineOrientation
import com.androidace.echojournal.ui.common.timeline.getLineType
import com.androidace.echojournal.ui.home.model.TimelineEntry
import com.androidace.echojournal.ui.mood.model.Mood
import com.androidace.echojournal.ui.newentry.TopicChip
import com.androidace.echojournal.ui.newentry.model.AudioWaveFormState
import com.androidace.echojournal.ui.newentry.model.NewEntryScreenState
import com.androidace.echojournal.ui.recording.RecordingViewModel
import com.androidace.echojournal.ui.theme.bodyStyle
import com.androidace.echojournal.ui.theme.moodColorPaletteMap
import com.androidace.echojournal.ui.theme.titleStyle
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.sqrt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onShowRecordingSheet: () -> Unit,
    homeViewModel: HomeScreenViewModel = hiltViewModel(),
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

    val entries by homeViewModel.entriesByDay.collectAsStateWithLifecycle()

    val listTopics by homeViewModel.listTopics.collectAsStateWithLifecycle()
    val listMood by homeViewModel.listMood.collectAsStateWithLifecycle()

    val selectedMoods by homeViewModel.selectedMoodFilter.collectAsStateWithLifecycle()
    val selectedTopics by homeViewModel.selectedTopicsFilter.collectAsStateWithLifecycle()

    var showMoodDropDown by remember { mutableStateOf(false) }
    var showTopicDropDown by remember { mutableStateOf(false) }


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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Your EchoJournal", style = titleStyle)
                },
                modifier = Modifier
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0XFFD9E2FF).copy(alpha = 0.4f),
                                Color(0XFFEEF0FF).copy(alpha = 0.4f)
                            )
                        )
                    )
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }) {
                        if (showMoodDropDown) showMoodDropDown = false
                        if (showTopicDropDown) showTopicDropDown = false
                    },
                colors = TopAppBarDefaults.topAppBarColors()
                    .copy(containerColor = Color.Transparent)
            )
        },
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
        },
        modifier = Modifier.background(
            Brush.verticalGradient(
                colors = listOf(
                    Color(0XFFD9E2FF).copy(alpha = 0.4f),
                    Color(0XFFEEF0FF).copy(alpha = 0.4f)
                )
            )
        )
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0XFFD9E2FF).copy(alpha = 0.4f),
                            Color(0XFFEEF0FF).copy(alpha = 0.4f)
                        )
                    )
                )
                .padding(paddingValues)
        ) {
            FilterRow(
                filterTopicList = selectedTopics,
                filterMoodList = selectedMoods,
                showMoodDropDown = showMoodDropDown,
                showTopicDropDown = showTopicDropDown,
                openMoodDropDown = {
                    showMoodDropDown = true
                    showTopicDropDown = false
                },
                openTopicDropDown = {
                    showTopicDropDown = true
                    showMoodDropDown = false
                },
                onClearTopicFilters = {
                    homeViewModel.clearTopicsFilters()
                    showTopicDropDown = false
                },
                onClearMoodFilters = {
                    homeViewModel.clearMoodFilters()
                    showMoodDropDown = false
                }
            )
            TimelineScreen(
                entries = entries,
                onProgressChange = { progress, entry ->
                    homeViewModel.onProgressChange(
                        progress,
                        entry
                    )
                },
                onPlayerMediaClick = homeViewModel::updatePlaybackState
            )
        }

        if (showMoodDropDown) {
            MoodDropDown(
                onClickOutside = {
                    showMoodDropDown = false
                },
                onMoodSelected = homeViewModel::addMoodFilter,
                filterMoods = listMood,
                modifier = Modifier.offset(y = paddingValues.calculateTopPadding() + 60.dp)
            )
        }

        if (showTopicDropDown) {
            TopicDropDown(
                onClickOutside = {
                    showTopicDropDown = false
                },
                onTopicSelected = homeViewModel::addTopicFilter,
                filteredTopics = listTopics,
                modifier = Modifier.offset(y = paddingValues.calculateTopPadding() + 60.dp)
            )
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
fun FilterRow(
    filterMoodList: List<Mood>,
    filterTopicList: List<Topic>,
    openMoodDropDown: () -> Unit,
    openTopicDropDown: () -> Unit,
    onClearMoodFilters: () -> Unit,
    onClearTopicFilters: () -> Unit,
    showMoodDropDown: Boolean,
    showTopicDropDown: Boolean,
    modifier: Modifier = Modifier
) {
    Row {
        Spacer(modifier = Modifier.width(16.dp))
        MoodOverlappingChip(
            filterMoodList,
            showMoodDropDown,
            onClearFilters = onClearMoodFilters,
            openChipDropdown = openMoodDropDown
        )
        Spacer(modifier = Modifier.width(4.dp))
        TopicOverlappingChip(
            filterTopicList,
            showTopicDropDown,
            onClearFilter = onClearTopicFilters,
            openChipDropdown = openTopicDropDown
        )
    }

}

@Composable
fun MoodOverlappingChip(
    listData: List<Mood>,
    showMoodDropDown: Boolean,
    openChipDropdown: () -> Unit,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    AssistChip(
        label = {
            Row(
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.heightIn(min = 32.dp)
            ) {
                if (listData.isEmpty()) {
                    Text(
                        text = "All Moods",
                        style = bodyStyle.copy(
                            color = MaterialTheme.colorScheme.secondary
                        )
                    )
                } else {
                    MoodGroupingText(listData)
                    Spacer(modifier = Modifier.width(3.dp))
                    Icon(
                        imageVector = Icons.Filled.Clear,
                        contentDescription = "Clear filters",
                        tint = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier
                            .size(18.dp)
                            .clickable {
                                onClearFilters.invoke()
                            }
                    )
                }
            }

        },
        onClick = openChipDropdown,
        modifier = modifier.then(Modifier.heightIn(min = 32.dp)),
        border = AssistChipDefaults.assistChipBorder(
            enabled = true,
            borderWidth = 1.dp,
            borderColor = if (!showMoodDropDown && listData.isEmpty())
                MaterialTheme.colorScheme.outlineVariant
            else if (showMoodDropDown)
                MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.outlineVariant
        ),
        shape = RoundedCornerShape(65.dp),
        colors = AssistChipDefaults.assistChipColors(
            if (!showMoodDropDown && listData.isEmpty()) Color.Transparent else MaterialTheme.colorScheme.onPrimary,
        )
    )
}

@Composable
fun TopicOverlappingChip(
    listData: List<Topic>,
    showTopicDropDown: Boolean,
    openChipDropdown: () -> Unit,
    onClearFilter: () -> Unit,
    modifier: Modifier = Modifier
) {
    AssistChip(
        label = {
            Row(
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.heightIn(min = 32.dp)
            ) {
                if (listData.isEmpty()) {
                    Text(
                        text = "All Topics",
                        style = bodyStyle.copy(
                            color = MaterialTheme.colorScheme.secondary
                        )
                    )
                } else {
                    TopicGroupingText(listData)
                    Spacer(modifier = Modifier.width(3.dp))
                    Icon(
                        imageVector = Icons.Filled.Clear,
                        contentDescription = "Clear filters",
                        tint = MaterialTheme.colorScheme.secondaryContainer,
                        modifier = Modifier
                            .size(18.dp)
                            .clickable {
                                onClearFilter.invoke()
                            }
                    )
                }
            }

        },
        onClick = openChipDropdown,
        border = AssistChipDefaults.assistChipBorder(
            enabled = true,
            borderWidth = 1.dp,
            borderColor = if (!showTopicDropDown && listData.isEmpty())
                MaterialTheme.colorScheme.outlineVariant
            else if (showTopicDropDown)
                MaterialTheme.colorScheme.primaryContainer
            else MaterialTheme.colorScheme.outlineVariant
        ),
        shape = RoundedCornerShape(65.dp),
        colors = AssistChipDefaults.assistChipColors(
            containerColor = if (!showTopicDropDown && listData.isEmpty()) Color.Transparent else MaterialTheme.colorScheme.onPrimary,
        )
    )
}

@Composable
fun MoodGroupingText(listMood: List<Mood>, modifier: Modifier = Modifier) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (listMood.size > 2) {
            Row(modifier = Modifier.padding(vertical = 5.dp)) {
                listMood.forEachIndexed { index, mood ->
                    if (index < 2) {
                        Image(
                            painter = painterResource(mood.activeResId),
                            contentDescription = "MoodFilters",
                            modifier = if (index == 0) Modifier.size(20.dp) else Modifier
                                .offset(x = (-4).dp)
                                .size(20.dp)
                        )
                    }
                }
                listMood.forEachIndexed { index, mood ->
                    if (index < 2) {
                        Text(text = if (index == 0) mood.displayName else ", ${mood.displayName}")
                    }
                }
                Text(text = " +1")
            }
        } else {
            Row {
                listMood.forEachIndexed { index, mood ->
                    Image(
                        painter = painterResource(mood.activeResId),
                        contentDescription = "MoodFilters",
                        modifier = if (index == 0) Modifier.size(20.dp) else Modifier
                            .offset(x = (-4).dp)
                            .size(20.dp)
                    )
                }
                listMood.forEachIndexed { index, mood ->
                    Text(text = if (index == 0) mood.displayName else ", ${mood.displayName}")
                }
            }
        }
    }
}

@Composable
fun TopicGroupingText(listTopic: List<Topic>, modifier: Modifier = Modifier) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (listTopic.size > 2) {
            Row(modifier = Modifier.padding(vertical = 5.dp)) {
                listTopic.forEachIndexed { index, topic ->
                    if (index < 2) {
                        Text(text = if (index == 0) topic.name else ", ${topic.name}")
                    }
                }
                Text(text = " +1")
            }
        } else {
            Row {
                listTopic.forEachIndexed { index, topic ->
                    Text(text = if (index == 0) topic.name else ", ${topic.name}")
                }
            }
        }
    }
}


@Composable
private fun TopicDropDown(
    onClickOutside: () -> Unit,
    filteredTopics: List<DropdownState<Topic>>,
    onTopicSelected: (Topic) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(Color.Transparent)
            .fillMaxSize()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                // Dismiss popup when user taps outside
                onClickOutside.invoke()
            },
        contentAlignment = Alignment.TopCenter
    ) {
        // The actual popup "container"
        Card(
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            colors = CardDefaults.cardColors()
                .copy(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.padding(horizontal = 16.dp)

        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 4.dp)
                    // Constrain the height to a max of 300.dp
                    .heightIn(max = 300.dp)
                    // Make it scrollable if content grows beyond 300.dp
                    .verticalScroll(rememberScrollState())
            ) {
                // 2A) Show matching topics
                filteredTopics.forEach { dropDownData ->
                    Column {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onTopicSelected(dropDownData.data)

                                }
                                .background(
                                    color = if (dropDownData.isSelected) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent,
                                    shape = if (dropDownData.isSelected) RoundedCornerShape(size = 8.dp) else RectangleShape
                                )
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Text(
                                text = "#",
                                style = bodyStyle.copy(
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.primary.copy(0.5f)
                                ),
                            )
                            Text(
                                text = dropDownData.data.name, style = bodyStyle.copy(
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.secondary
                                ),
                                modifier = Modifier
                                    .padding(4.dp)
                                    .weight(1.0f)
                            )
                            if (dropDownData.isSelected) {
                                Image(
                                    painter = painterResource(R.drawable.ic_trailing_tick),
                                    contentDescription = "Selected Item"
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                    }

                }
            }
        }
    }
}

@Composable
private fun MoodDropDown(
    onClickOutside: () -> Unit,
    filterMoods: List<DropdownState<Mood>>,
    onMoodSelected: (Mood) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Transparent) // or a semi-transparent scrim if preferred
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) {
                // Dismiss popup when user taps outside
                onClickOutside.invoke()
            },
        contentAlignment = Alignment.TopCenter
    ) {
        // The actual popup "container"
        Card(
            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
            colors = CardDefaults.cardColors()
                .copy(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.padding(horizontal = 16.dp)

        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 4.dp)
                    // Constrain the height to a max of 300.dp
                    .heightIn(max = 300.dp)
                    // Make it scrollable if content grows beyond 300.dp
                    .verticalScroll(rememberScrollState())
            ) {
                // 2A) Show matching topics
                filterMoods.forEach { dropDownData ->
                    Column {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    color = if (dropDownData.isSelected) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent,
                                    shape = if (dropDownData.isSelected) RoundedCornerShape(size = 8.dp) else RectangleShape
                                )
                                .clickable {
                                    onMoodSelected(dropDownData.data)
                                }
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Image(
                                painter = painterResource(dropDownData.data.activeResId),
                                modifier = Modifier.size(24.dp),
                                contentDescription = "Mood Item"
                            )
                            Text(
                                text = dropDownData.data.displayName, style = bodyStyle.copy(
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.secondary
                                ),
                                modifier = Modifier
                                    .padding(4.dp)
                                    .weight(1.0f)
                            )
                            if (dropDownData.isSelected) {
                                Image(
                                    painter = painterResource(R.drawable.ic_trailing_tick),
                                    contentDescription = "Selected Item"
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TimelineScreen(
    entries: Map<LocalDate, List<TimelineEntry>>,
    onProgressChange: (Float, TimelineEntry) -> Unit,
    onPlayerMediaClick: (TimelineEntry) -> Unit,
    modifier: Modifier = Modifier
) {
    val sortedDates = entries.keys.sortedDescending()

    LazyColumn(modifier = modifier) {
        sortedDates.forEach { date ->
            item {
                DayHeader(date = date)
            }
            itemsIndexed(entries[date] ?: emptyList()) { position, entry ->
                TimelineRow(
                    entry = entry,
                    position = position,
                    totalItems = entries[date]?.size ?: 0,
                    onProgressChange = { onProgressChange.invoke(it, entry) },
                    onPlayerMediaClick = { onPlayerMediaClick.invoke(entry) }
                )
            }
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
            .background(
                color = Color.Transparent
            )
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
fun TimelineRow(
    entry: TimelineEntry,
    position: Int,
    totalItems: Int,
    onProgressChange: (Float) -> Unit,
    onPlayerMediaClick: () -> Unit
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 210.dp)
            .padding(horizontal = 16.dp)
    ) {
        // Left side: icon + timeline line
        Timeline(
            modifier = Modifier.fillMaxHeight(),
            lineType = getLineType(position, totalItems),
            orientation = TimelineOrientation.Vertical,
            lineStyle = LineStyle.solid(
                color = MaterialTheme.colorScheme.outlineVariant,
                width = 1.dp
            ),
            marker = {
                Image(
                    painter = painterResource(id = entry.mood.activeResId),
                    contentDescription = "Mood Icon",
                    modifier = Modifier
                        .size(36.dp)
                )
            }
        )

        // Right side: the content card
        Column {
            Card(
                modifier = Modifier
                    .padding(start = 8.dp)
                    .fillMaxWidth(),
                colors = CardDefaults.cardColors().copy(
                    containerColor = MaterialTheme.colorScheme.onPrimary
                ),
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(
                                color = moodColorPaletteMap[entry.mood]?.lightBgColor
                                    ?: MaterialTheme.colorScheme.inverseOnSurface,
                                shape = RoundedCornerShape(45.dp)
                            )
                    ) {
                        Box(modifier = Modifier.weight(0.20f)) {
                            Box(contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .padding(start = 4.dp)
                                    .size(38.dp)
                                    .shadow(elevation = 1.dp, shape = CircleShape)
                                    .background(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                    .clickable {
                                        onPlayerMediaClick.invoke()
                                    }
                            ) {
                                Icon(
                                    painter = if (!entry.audioWaveFormState.isPlaying) painterResource(
                                        R.drawable.ic_play
                                    ) else painterResource(R.drawable.ic_pause),
                                    contentDescription = "Play Icon",
                                    modifier = Modifier.size(14.dp),
                                    tint = moodColorPaletteMap[entry.mood]?.darkColor
                                        ?: MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        AudioWaveform(
                            amplitudes = entry.audioWaveFormState.amplitudes,
                            onProgressChange = onProgressChange,
                            progress = entry.audioWaveFormState.progress,
                            spikePadding = 2.dp,
                            spikeWidth = 4.dp,
                            spikeRadius = 3.dp,
                            waveformBrush = SolidColor(
                                moodColorPaletteMap[entry.mood]?.lightColor
                                    ?: Color.White
                            ),
                            progressBrush = SolidColor(
                                moodColorPaletteMap[entry.mood]?.darkColor
                                    ?: MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier
                                .heightIn(min = 25.dp, max = 38.dp)
                                .padding(vertical = 5.dp)
                                .weight(1.1f)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.weight(0.38f)
                        ) {
                            TimeDuration(entry)
                        }
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
                                TopicChip(topic, isCancelable = false)
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

    }
}

@Composable
fun TimeDuration(state: TimelineEntry) {
    Text(
        "${state.audioWaveFormState.seekDuration}/${state.audioWaveFormState.totalDuration}",
        style = bodyStyle.copy(
            fontSize = 12.sp,
            fontWeight = FontWeight.W400,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        ),
    )
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
            audioDuration = "00:00",
            recordingId = 0,
            audioWaveFormState = AudioWaveFormState()
        ), 0, 3, onProgressChange = {}, onPlayerMediaClick = {}
    )
}

@Preview
@Composable
fun DropDownPreview() {
    MoodDropDown(
        onClickOutside = {},
        filterMoods = listOf(DropdownState(Mood.PEACEFUL, isSelected = false)),
        onMoodSelected = {})
}