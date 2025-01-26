package com.androidace.echojournal.ui.newentry

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.androidace.echojournal.R
import com.androidace.echojournal.audio.waveform.AudioWaveform
import com.androidace.echojournal.db.Topic
import com.androidace.echojournal.ui.common.EJScreen
import com.androidace.echojournal.ui.common.EJUIState
import com.androidace.echojournal.ui.mood.MoodBottomSheet
import com.androidace.echojournal.ui.mood.model.Mood
import com.androidace.echojournal.ui.newentry.model.NewEntryScreenState
import com.androidace.echojournal.ui.theme.bodyStyle
import com.androidace.echojournal.ui.theme.moodColorPaletteMap
import com.androidace.echojournal.ui.theme.titleStyle
import com.androidace.echojournal.ui.theme.transparentTextFieldColors
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewEntryScreen(
    newEntry: NewEntry?,
    viewModel: NewEntryViewModel = hiltViewModel()
) {
    val eJUiState by viewModel.eJUiStateFlow.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState()
    val newEntryScreenState by viewModel.newScreenState.collectAsStateWithLifecycle()
    val validateForm by viewModel.isFormValidated.collectAsStateWithLifecycle()
    // The list of selected topics
    val selectedTopics by viewModel.selectedTopic.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) {
        viewModel.loadAudio(newEntry?.id ?: 0)
    }

    NewEntryScreenContent(
        ejUiState = eJUiState,
        validateForm = validateForm,
        newEntryScreenState = newEntryScreenState,
        onValueChange = viewModel::onTitleValueChange,
        selectedTopics = selectedTopics,
        bottomSheetState = sheetState,
        onMoodCancelClick = {

        },
        onMoodConfirmClick = viewModel::onMoodSelected,
        onProgressChange = viewModel::updateProgress,
        onPlayRecordClick = viewModel::updatePlaybackState,
        onAiAssistantClick = {

        },
        onCreateNewTopic = {
            viewModel.createTopic(it) { newTopic ->
                // Avoid duplicates
                if (!selectedTopics.contains(newTopic)) {
                    selectedTopics.add(newTopic)
                }
            }
        },
        onTopicSelected = {
            // Avoid duplicates
            if (!selectedTopics.contains(it)) {
                selectedTopics.add(it)
            }
            viewModel.validate()
        },
        onCancel = {},
        onSave = viewModel::onSaveEntry
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun NewEntryScreenContent(
    ejUiState: EJUIState,
    newEntryScreenState: NewEntryScreenState,
    validateForm: Boolean,
    bottomSheetState: SheetState,
    selectedTopics: SnapshotStateList<Topic>,
    onValueChange: (String) -> Unit,
    onMoodCancelClick: () -> Unit,
    onMoodConfirmClick: (Mood) -> Unit,
    onPlayRecordClick: () -> Unit,
    onProgressChange: (Float) -> Unit,
    onAiAssistantClick: () -> Unit,
    onCreateNewTopic: (String) -> Unit,
    onTopicSelected: (Topic) -> Unit,
    onCancel: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()

    EJScreen(
        ejUiState = ejUiState,
        loadingScreen = {},
        bottomSheet = {
            if (bottomSheetState.isVisible) {
                ModalBottomSheet(
                    onDismissRequest = { onMoodCancelClick() },
                    sheetState = bottomSheetState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp),
                    dragHandle = null
                ) {
                    MoodBottomSheet(
                        selectedMood = newEntryScreenState.selectedMood,
                        onCancel = {
                            coroutineScope.launch {
                                bottomSheetState.hide()
                            }
                            onMoodCancelClick.invoke()
                        },
                        onConfirm = {
                            coroutineScope.launch {
                                bottomSheetState.hide()
                            }
                            onMoodConfirmClick.invoke(it)
                        }
                    )
                }
            }
        }) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(title = {
                    Text(
                        text = stringResource(R.string.new_entry_title),
                        style = titleStyle.copy(fontSize = 22.sp)
                    )
                },
                    navigationIcon = {
                        Icon(
                            painter = painterResource(R.drawable.ic_back),
                            contentDescription = "Back"
                        )
                    }
                )
            },
            bottomBar = {
                FooterLayout(
                    validateForm = validateForm,
                    onCancel = onCancel,
                    onSaveEntry = onSave
                )
            }
        ) {
            Column(
                modifier = Modifier
                    .padding(it)
                    .background(color = MaterialTheme.colorScheme.surface)
            ) {
                TextField(
                    placeholder = {
                        Text(
                            newEntryScreenState.newEntryTitleHint, style = titleStyle.copy(
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                        )
                    },
                    leadingIcon = {
                        IconButton(onClick = {
                            coroutineScope.launch {
                                bottomSheetState.show()
                            }
                        }) {
                            Image(
                                painter = painterResource(
                                    newEntryScreenState.selectedMood?.activeResId
                                        ?: R.drawable.ic_add_mood
                                ),
                                contentDescription = "Add Mood"
                            )
                        }
                    },
                    value = newEntryScreenState.newEntryTitle,
                    onValueChange = onValueChange,
                    textStyle = titleStyle.copy(
                        color = if (newEntryScreenState.newEntryTitleHint.isEmpty()) MaterialTheme.colorScheme.outlineVariant else MaterialTheme.colorScheme.onSurface
                    ),
                    colors = transparentTextFieldColors()
                )
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier
                        .padding(horizontal = 8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(
                                color = moodColorPaletteMap[newEntryScreenState.selectedMood]?.lightBgColor
                                    ?: MaterialTheme.colorScheme.inverseOnSurface,
                                shape = RoundedCornerShape(45.dp)
                            )
                            .weight(0.88f)
                    ) {
                        Box(modifier = Modifier.weight(0.18f)) {
                            Box(contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .padding(start = 4.dp)
                                    .size(32.dp)
                                    .shadow(elevation = 1.dp, shape = CircleShape)
                                    .background(
                                        shape = CircleShape,
                                        color = MaterialTheme.colorScheme.onPrimary
                                    )
                                    .clickable {
                                        onPlayRecordClick.invoke()
                                    }
                            ) {
                                Icon(
                                    painter = if (!newEntryScreenState.audioWaveFormState.isPlaying) painterResource(
                                        R.drawable.ic_play
                                    ) else painterResource(R.drawable.ic_pause),
                                    contentDescription = "Play Icon",
                                    modifier = Modifier.size(14.dp),
                                    tint = moodColorPaletteMap[newEntryScreenState.selectedMood]?.darkColor
                                        ?: MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        AudioWaveform(
                            amplitudes = newEntryScreenState.audioWaveFormState.amplitudes,
                            onProgressChange = onProgressChange,
                            progress = newEntryScreenState.audioWaveFormState.progress,
                            spikePadding = 2.dp,
                            spikeWidth = 4.dp,
                            spikeRadius = 3.dp,
                            waveformBrush = SolidColor(
                                moodColorPaletteMap[newEntryScreenState.selectedMood]?.lightColor
                                    ?: Color.White
                            ),
                            progressBrush = SolidColor(
                                moodColorPaletteMap[newEntryScreenState.selectedMood]?.darkColor
                                    ?: MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier
                                .heightIn(min = 25.dp, max = 48.dp)
                                .padding(vertical = 10.dp)
                                .weight(1.1f)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.weight(0.38f)
                        ) {
                            TimeDuration(newEntryScreenState)
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(modifier = Modifier.weight(0.12f), contentAlignment = Alignment.Center) {
                        Image(
                            painter = painterResource(R.drawable.ic_ai_assistant),
                            contentDescription = "AI Translate",
                            modifier = Modifier
                                .sizeIn(minWidth = 44.dp)
                                .shadow(elevation = 3.dp, shape = CircleShape)
                                .clickable {
                                    onAiAssistantClick.invoke()
                                }
                        )
                    }

                }
                Spacer(modifier = Modifier.height(16.dp))
                // 2) The row (or flow) of selected topics
                Row(verticalAlignment = Alignment.Top) {
                    Text(
                        text = "#",
                        style = bodyStyle.copy(
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier.padding(start = 8.dp, end = 0.dp, top = 10.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    SelectedTopicsChipsFlow(
                        selectedTopics = selectedTopics,
                        listTopics = newEntryScreenState.listTopics,
                        onTopicSelected = onTopicSelected,
                        onCreateNewTopic = onCreateNewTopic,
                        onRemoveTopic = { topicToRemove ->
                            selectedTopics.remove(topicToRemove)
                        }
                    )
                }

            }
        }
    }
}

@Composable
fun TimeDuration(state: NewEntryScreenState) {
    Text(
        "${state.audioWaveFormState.seekDuration}/${state.audioWaveFormState.totalDuration}",
        style = bodyStyle.copy(
            fontSize = 12.sp,
            fontWeight = FontWeight.W400,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        ),
    )
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SelectedTopicsChipsFlow(
    listTopics: List<Topic>,
    onCreateNewTopic: (String) -> Unit,
    onTopicSelected: (Topic) -> Unit,
    selectedTopics: List<Topic>,
    onRemoveTopic: (Topic) -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    var text by remember { mutableStateOf("") }
    var showPopup by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    Column {
        FlowRow(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    focusRequester.requestFocus()
                },
            verticalArrangement = Arrangement.Top
        ) {
            selectedTopics.forEach { topic ->
                TopicChip(
                    topic = topic,
                    onRemove = { onRemoveTopic(topic) }
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            BasicTextField(
                value = text,
                onValueChange = {
                    text = it
                    showPopup = true  // open the dropdown whenever the text changes
                },
                decorationBox = { innerTextField ->
                    if (selectedTopics.isEmpty() && text.isEmpty()) {
                        Text("Topic")
                    }
                    innerTextField()
                },
                textStyle = LocalTextStyle.current.copy(
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier
                    .wrapContentWidth()
                    .widthIn(min = 50.dp, max = 400.dp)
                    .defaultMinSize(
                        minHeight = 22.dp
                    )
                    .padding(vertical = 10.dp)
                    .focusRequester(focusRequester)
            )
        }
        TopicsAutoCompleteField(
            allTopics = listTopics,
            searchText = text,
            showPopup = showPopup,
            onCreateNewTopic = { newTopic ->
                onCreateNewTopic.invoke(newTopic)
                showPopup = false
                text = ""
                focusManager.clearFocus()
            },
            onTopicSelected = { selectedTopic ->
                // Avoid duplicates
                onTopicSelected.invoke(selectedTopic)
                showPopup = false
                text = ""
                focusManager.clearFocus()
            },
            onClickOutside = {
                showPopup = false
                focusManager.clearFocus()
            },
            modifier = Modifier.padding(vertical = 10.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopicsAutoCompleteField(
    allTopics: List<Topic>,
    searchText: String,
    showPopup: Boolean,
    onCreateNewTopic: (String) -> Unit,
    onTopicSelected: (Topic) -> Unit,
    onClickOutside: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Filter the topic list in memory, or make DB calls if you prefer.
    val filteredTopics = remember(searchText, allTopics) {
        if (searchText.isBlank()) {
            emptyList()
        } else {
            allTopics.filter { it.name.contains(searchText, ignoreCase = true) }
        }
    }

    // 2) Show the popup below the TextField if `showPopup` is true
    if (showPopup && searchText.isNotEmpty()) {
        // A Box that covers the entire screen to detect outside clicks (dismiss on tap outside).
        Box(
            modifier = Modifier
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
                modifier = Modifier.padding(end = 16.dp)

            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        // Constrain the height to a max of 300.dp
                        .heightIn(max = 300.dp)
                        // Make it scrollable if content grows beyond 300.dp
                        .verticalScroll(rememberScrollState())
                ) {
                    // 2A) Show matching topics
                    filteredTopics.forEachIndexed { index, topic ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onTopicSelected(topic)

                                }
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            Text(
                                text = "#",
                                style = bodyStyle.copy(
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.primary.copy(0.5f)
                                ),
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = topic.name, style = bodyStyle.copy(
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            )
                        }
                    }

                    // 2B) If there's no exact match, offer to create a new topic
                    val isExactMatch = filteredTopics.any {
                        it.name.equals(searchText, ignoreCase = true)
                    }
                    if (!isExactMatch && searchText.isNotEmpty()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onCreateNewTopic(searchText)
                                }
                                .padding(horizontal = 12.dp, vertical = 12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(end = 5.dp),
                                contentDescription = "Add topic"
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Create ‘$searchText’", style = bodyStyle.copy(
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TopicChip(
    topic: Topic,
    onRemove: () -> Unit = {}
) {
    AssistChip(
        label = {
            Row(
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "#",
                    style = bodyStyle.copy(
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.primary.copy(0.5f)
                    ),
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = topic.name,
                    style = bodyStyle.copy(
                        color = MaterialTheme.colorScheme.secondary
                    )
                )
            }

        },
        onClick = {},
        trailingIcon = {
            Icon(
                imageVector = Icons.Filled.Close,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                contentDescription = "Clear topic",
                modifier = Modifier
                    .size(16.dp)
                    .clickable {
                        onRemove()
                    })
        },
        border = AssistChipDefaults.assistChipBorder(enabled = false, borderWidth = 0.dp),
        shape = RoundedCornerShape(65.dp),
        colors = AssistChipDefaults.assistChipColors(containerColor = Color(0XFFF2F2F7)),
    )
}

@Composable
fun FooterLayout(
    validateForm: Boolean,
    onCancel: () -> Unit,
    onSaveEntry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Button(
            onClick = onCancel,
            colors = ButtonDefaults.buttonColors()
                .copy(containerColor = MaterialTheme.colorScheme.onPrimaryContainer),
            modifier = Modifier
                .weight(0.3f)
                .background(
                    shape = RoundedCornerShape(45.dp),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
        ) {
            Text(
                "Cancel",
                style = bodyStyle.copy(color = MaterialTheme.colorScheme.primary, fontSize = 16.sp)
            )
        }
        Spacer(
            modifier = Modifier.width(16.dp)
        )
        Box(
            modifier = Modifier
                .background(
                    shape = RoundedCornerShape(45.dp),
                    brush = if (validateForm)
                        Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF578CFF),
                                Color(0xFF1F70F5)
                            )
                        ) else SolidColor(MaterialTheme.colorScheme.surfaceVariant)
                )
                .weight(0.7f)
        ) {
            Button(
                onClick = onSaveEntry,
                enabled = validateForm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    disabledContentColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Save",
                    style = bodyStyle.copy(
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontSize = 16.sp
                    )
                )
            }
        }

    }
}