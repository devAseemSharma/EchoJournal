package com.androidace.echojournal.ui.newentry

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.androidace.echojournal.R
import com.androidace.echojournal.ui.common.EJScreen
import com.androidace.echojournal.ui.common.EJUIState
import com.androidace.echojournal.ui.mood.MoodBottomSheet
import com.androidace.echojournal.ui.mood.model.Mood
import com.androidace.echojournal.util.UiText
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewEntryScreen(
    recordingFilePath: String?,
    viewModel: NewEntryViewModel = hiltViewModel()
) {
    val eJUiState by viewModel.eJUiStateFlow.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
    )
    val coroutineScope = rememberCoroutineScope()

    NewEntryScreenContent(
        ejUiState = eJUiState,
        bottomSheetState = sheetState,
        onAddMoodClick = {
            coroutineScope.launch {
                sheetState.show()
            }
        },
        onMoodCancelClick = {
            coroutineScope.launch {
                sheetState.hide()
            }
        }, onMoodConfirmClick = {
            coroutineScope.launch {
                sheetState.hide()
            }
        })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun NewEntryScreenContent(
    ejUiState: EJUIState,
    bottomSheetState: SheetState,
    onAddMoodClick: () -> Unit,
    onMoodCancelClick: () -> Unit,
    onMoodConfirmClick: (Mood) -> Unit,
    modifier: Modifier = Modifier
) {

    EJScreen(
        ejUiState = ejUiState,
        loadingScreen = {},
        bottomSheet = {
            ModalBottomSheet(
                onDismissRequest = { onMoodCancelClick() },
                sheetState = bottomSheetState,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                dragHandle = null
            ) {
                MoodBottomSheet(
                    onCancel = onMoodCancelClick,
                    onConfirm = onMoodConfirmClick
                )
            }
        }) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(title = {
                    Text(
                        text = stringResource(R.string.new_entry_title)
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
                    IconButton(onClick = onAddMoodClick) {
                        Image(
                            painter = painterResource(R.drawable.ic_add_mood),
                            contentDescription = "Add Mood"
                        )
                    }
                }
            }
        }
    }

}