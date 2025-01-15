package com.androidace.echojournal.ui.newentry

import androidx.lifecycle.ViewModel
import com.androidace.echojournal.ui.common.UIStateHandlerImpl
import com.androidace.echojournal.ui.common.UiStateHandler
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class NewEntryViewModel @Inject constructor(
    val uiStateHandlerImpl: UIStateHandlerImpl,
): ViewModel(), UiStateHandler by uiStateHandlerImpl {

}