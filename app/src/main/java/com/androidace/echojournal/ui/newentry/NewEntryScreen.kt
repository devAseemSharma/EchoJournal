package com.androidace.echojournal.ui.newentry

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.navigation.compose.hiltViewModel
import com.androidace.echojournal.R
import com.androidace.echojournal.util.UiText

@Composable
fun NewEntryScreen(
    recordingFilePath: String?,
    viewModel: NewEntryViewModel = hiltViewModel()
) {


    NewEntryScreenContent()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun NewEntryScreenContent(modifier: Modifier = Modifier) {

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = {
                Text(
                    text = stringResource(R.string.new_entry_title)
                )
            },

                navigationIcon = {
                    Icon(painter = painterResource(R.drawable.ic_back), contentDescription = "Back")
                }
            )
        }
    ) {
        Column(modifier = Modifier.padding(it)) {

        }
    }
}