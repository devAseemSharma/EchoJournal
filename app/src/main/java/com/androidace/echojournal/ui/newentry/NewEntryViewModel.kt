package com.androidace.echojournal.ui.newentry

import androidx.lifecycle.ViewModel
import com.androidace.echojournal.ui.common.UIStateHandlerImpl
import com.androidace.echojournal.ui.common.UiStateHandler
import com.androidace.echojournal.ui.newentry.model.NewEntryScreenState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class NewEntryViewModel @Inject constructor(
    val uiStateHandlerImpl: UIStateHandlerImpl,
): ViewModel(), UiStateHandler by uiStateHandlerImpl {

    private var _newScreenState = MutableStateFlow(NewEntryScreenState())
    val newScreenState = _newScreenState.asStateFlow()

    fun onTitleValueChange(text:String){
        _newScreenState.value = _newScreenState.value.copy(
            newEntryTitle = text
        )
    }

}