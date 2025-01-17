package com.androidace.echojournal.ui.newentry

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.androidace.echojournal.R
import com.androidace.echojournal.db.Topic
import com.androidace.echojournal.ui.common.EJScreen
import com.androidace.echojournal.ui.common.EJUIState
import com.androidace.echojournal.ui.mood.MoodBottomSheet
import com.androidace.echojournal.ui.mood.model.Mood
import com.androidace.echojournal.ui.newentry.model.NewEntryScreenState
import com.androidace.echojournal.ui.theme.titleStyle
import com.androidace.echojournal.ui.theme.transparentTextFieldColors
import com.linc.audiowaveform.AudioWaveform
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewEntryScreen(
    recordingFilePath: String?,
    viewModel: NewEntryViewModel = hiltViewModel()
) {
    val eJUiState by viewModel.eJUiStateFlow.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState()
    val newEntryScreenState by viewModel.newScreenState.collectAsStateWithLifecycle()
// The list of selected topics
    val selectedTopics = remember { mutableStateListOf<Topic>() }

    NewEntryScreenContent(
        ejUiState = eJUiState,
        newEntryScreenState = newEntryScreenState,
        onValueChange = viewModel::onTitleValueChange,
        selectedTopics = selectedTopics,
        bottomSheetState = sheetState,
        onMoodCancelClick = {

        },
        onMoodConfirmClick = {

        },
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
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun NewEntryScreenContent(
    ejUiState: EJUIState,
    newEntryScreenState: NewEntryScreenState,
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
        ) {
            Column(modifier = Modifier.padding(it)) {
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
                                painter = painterResource(R.drawable.ic_add_mood),
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
                                color = MaterialTheme.colorScheme.inverseOnSurface,
                                shape = RoundedCornerShape(45.dp)
                            )
                            .weight(0.85f)
                    ) {
                        Image(
                            painter = painterResource(R.drawable.ic_play_record),
                            contentDescription = "Play Record",
                            modifier = Modifier
                                .padding(start = 4.dp)
                                .shadow(elevation = 3.dp, shape = CircleShape)
                                .clickable {
                                    onPlayRecordClick.invoke()
                                }
                        )
                        AudioWaveform(
                            amplitudes = newEntryScreenState.audioWaveFormState.amplitudes,
                            onProgressChange = onProgressChange,
                            modifier = Modifier.sizeIn(minWidth = 200.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Box(modifier = Modifier.weight(0.15f), contentAlignment = Alignment.Center) {
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
                textStyle = LocalTextStyle.current.copy(fontSize = 14.sp),
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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp)
                    )
                    // Constrain the height to a max of 300.dp
                    .heightIn(max = 300.dp)
                    // Make it scrollable if content grows beyond 300.dp
                    .verticalScroll(rememberScrollState())
            ) {
                // 2A) Show matching topics
                filteredTopics.forEachIndexed { index, topic ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onTopicSelected(topic)

                            }
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Text(text = "# ${topic.name}")
                    }

                    // Optional: add a divider between items
                    if (index < filteredTopics.lastIndex) {
                        Divider()
                    }
                }

                // 2B) If there's no exact match, offer to create a new topic
                val isExactMatch = filteredTopics.any {
                    it.name.equals(searchText, ignoreCase = true)
                }
                if (!isExactMatch && searchText.isNotEmpty()) {
                    if (filteredTopics.isNotEmpty()) {
                        HorizontalDivider()
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onCreateNewTopic(searchText)
                            }
                            .padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Text(text = "+ Create ‘$searchText’")
                    }
                }
            }
        }
    }
}

@Composable
fun TopicChip(
    topic: Topic,
    onRemove: () -> Unit
) {
    AssistChip(
        label = {
            Text(
                text = "# ${topic.name}",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.Black
                )
            )
        },
        onClick = {},
        trailingIcon = {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "Clear topic",
                modifier = Modifier.clickable {
                    onRemove()
                })
        },
        shape = RoundedCornerShape(65.dp),
        colors = AssistChipDefaults.assistChipColors(containerColor = Color(0XFFF2F2F7)),
    )
}