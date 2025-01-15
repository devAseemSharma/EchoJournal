package com.androidace.echojournal.ui.newentry

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.Modifier
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

        }, onMoodConfirmClick = {

        })
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
                Row {
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
                    Spacer(modifier = Modifier.width(12.dp))
                    TextField(
                        placeholder = {
                            Text(
                                newEntryScreenState.newEntryTitleHint, style = titleStyle.copy(
                                    color = MaterialTheme.colorScheme.outlineVariant
                                )
                            )
                        },
                        value = newEntryScreenState.newEntryTitle,
                        onValueChange = onValueChange,
                        textStyle = titleStyle.copy(
                            color = if (newEntryScreenState.newEntryTitleHint.isEmpty()) MaterialTheme.colorScheme.outlineVariant else MaterialTheme.colorScheme.onSurface
                        ),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            errorContainerColor = Color.Transparent
                        )
                    )
                }
            }
        }
    }

}