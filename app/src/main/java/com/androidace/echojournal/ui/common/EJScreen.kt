package com.androidace.echojournal.ui.common

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable

@Composable
fun EJScreen(
    ejUiState: EJUIState,
    loadingScreen: @Composable () -> Unit,
    bottomSheet: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
    when {
        ejUiState.isLoading -> {
            Content(content, bottomSheet)
            loadingScreen()
        }

        else -> {
            Content(content, bottomSheet)
        }
    }
}

@Composable
private fun Content(
    content: @Composable () -> Unit,
    bottomSheet: @Composable() (() -> Unit)?
) {
    Box {
        content()
        bottomSheet?.invoke()
    }
}