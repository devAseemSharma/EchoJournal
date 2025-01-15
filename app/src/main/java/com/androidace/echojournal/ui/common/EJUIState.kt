package com.androidace.echojournal.ui.common

import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

data class EJUIState(
    val isLoading: Boolean = false,
)

@ViewModelScoped
class UIStateHandlerImpl @Inject constructor(): UiStateHandler{
    private val _eJUiStateFlow = MutableStateFlow(
        EJUIState()
    )

    override val eJUiStateFlow: StateFlow<EJUIState>
        get() = _eJUiStateFlow

    override fun hideLoader() {
        _eJUiStateFlow.value = eJUiStateFlow.value.copy(isLoading = false)
    }

    override fun showLoader() {
        _eJUiStateFlow.value = eJUiStateFlow.value.copy(isLoading = true)
    }

    override fun updateState(eJUiState: EJUIState) {
       _eJUiStateFlow.value = eJUiState
    }

}

interface UiStateHandler {
    val eJUiStateFlow: StateFlow<EJUIState>
    fun hideLoader()
    fun showLoader()
    fun updateState(eJUiState: EJUIState)
}