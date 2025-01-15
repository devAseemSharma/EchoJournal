package com.androidace.echojournal.ui.newentry

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.androidace.echojournal.R
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
    NewEntryScreenContent(
        ejUiState = eJUiState,
        newEntryScreenState = newEntryScreenState,
        onValueChange = viewModel::onTitleValueChange,
        bottomSheetState = sheetState,
        onMoodCancelClick = {

        },
        onMoodConfirmClick = {

        },
        onProgressChange = viewModel::updateProgress,
        onPlayRecordClick = viewModel::updatePlaybackState,
        onAiAssistantClick = {

        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun NewEntryScreenContent(
    ejUiState: EJUIState,
    newEntryScreenState: NewEntryScreenState,
    bottomSheetState: SheetState,
    onValueChange: (String) -> Unit,
    onMoodCancelClick: () -> Unit,
    onMoodConfirmClick: (Mood) -> Unit,
    onPlayRecordClick: () -> Unit,
    onProgressChange: (Float) -> Unit,
    onAiAssistantClick: () -> Unit,
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
                        .weight(1.0f)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.inverseOnSurface,
                                shape = RoundedCornerShape(45.dp)
                            )
                            .weight(0.9f)
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
                    Box(modifier = Modifier.weight(0.1f), contentAlignment = Alignment.Center) {
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
            }
        }
    }

}