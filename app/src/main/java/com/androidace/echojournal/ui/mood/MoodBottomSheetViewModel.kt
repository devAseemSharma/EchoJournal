package com.androidace.echojournal.ui.mood

import androidx.lifecycle.ViewModel
import com.androidace.echojournal.ui.mood.model.Mood
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.persistentListOf
import javax.inject.Inject

@HiltViewModel
class MoodBottomSheetViewModel @Inject constructor() : ViewModel() {
    val moodList =
        persistentListOf(Mood.STRESSED, Mood.SAD, Mood.NEUTRAL, Mood.PEACEFUL, Mood.EXCITED)
}