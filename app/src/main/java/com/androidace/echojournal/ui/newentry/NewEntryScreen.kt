package com.androidace.echojournal.ui.newentry

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import androidx.compose.ui.graphics.Color
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
                TopicsAutoCompleteField(
                    allTopics = newEntryScreenState.listTopics,
                    onCreateNewTopic = { newTopic ->
                        onCreateNewTopic.invoke(newTopic)

                    },
                    onTopicSelected = { selectedTopic ->
                        // Avoid duplicates
                        onTopicSelected.invoke(selectedTopic)
                    })
                Spacer(modifier = Modifier.height(16.dp))
                // 2) The row (or flow) of selected topics
                SelectedTopicsChipsFlow(
                    selectedTopics = selectedTopics,
                    onRemoveTopic = { topicToRemove ->
                        selectedTopics.remove(topicToRemove)
                    }
                )

            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopicsAutoCompleteField(
    allTopics: List<Topic>,
    onCreateNewTopic: (String) -> Unit,
    onTopicSelected: (Topic) -> Unit,
    modifier: Modifier = Modifier
) {

    var text by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }


    // Filter the topic list in memory, or make DB calls if you prefer.
    val filteredTopics = remember(text, allTopics) {
        if (text.isBlank()) {
            emptyList()
        } else {
            allTopics.filter { it.name.contains(text, ignoreCase = true) }
        }
    }

    // This specialized Box helps manage the dropdown's open/close behavior.
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        TextField(
            value = text,
            onValueChange = {
                text = it
                expanded = true  // open the dropdown whenever the text changes
            },
            label = { Text("Enter topic") },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(MenuAnchorType.PrimaryEditable)  // required for ExposedDropdownMenuBox anchor
        )

        ExposedDropdownMenu(
            expanded = expanded && text.isNotEmpty(),
            onDismissRequest = { expanded = false }
        ) {
            // 1) Show existing topics that match the query
            filteredTopics.forEach { topic ->
                DropdownMenuItem(
                    onClick = {
                        onTopicSelected(topic)
                        text = topic.name
                        expanded = false
                    },
                    text = {
                        Text(text = "# ${topic.name}")
                    }
                )
            }

            // 2) If user typed something that’s not in the list, offer to create it
            val isExactMatch = filteredTopics.any { it.name.equals(text, ignoreCase = true) }
            if (!isExactMatch && text.isNotEmpty()) {
                DropdownMenuItem(
                    onClick = {
                        onCreateNewTopic(text)
                        text = ""
                        expanded = false
                    },
                    text = {
                        Text(text = "+ Create ‘$text’")
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SelectedTopicsChipsFlow(
    selectedTopics: List<Topic>,
    onRemoveTopic: (Topic) -> Unit
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
    ) {
        selectedTopics.forEach { topic ->
            TopicChip(
                topic = topic,
                onRemove = { onRemoveTopic(topic) }
            )
        }
    }
}

@Composable
fun TopicChip(
    topic: Topic,
    onRemove: () -> Unit
) {
    Box(
        modifier = Modifier
            .background(
                color = Color(0xFFF2F2F7),  // a light gray background, or your choice
                shape = RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // "# Work"
            Text(
                text = "# ${topic.name}",
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = Color.Black
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            // The "X" icon (could also be an IconButton)
            Text(
                text = "x",
                color = Color.Gray,
                modifier = Modifier
                    .clickable { onRemove() }
            )
        }
    }
}