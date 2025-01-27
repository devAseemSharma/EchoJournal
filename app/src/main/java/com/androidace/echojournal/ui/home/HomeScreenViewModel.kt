package com.androidace.echojournal.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.androidace.echojournal.repository.NewEntryRepository
import com.androidace.echojournal.repository.TopicRepository
import com.androidace.echojournal.ui.home.model.TimelineEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    private val topicRepository: TopicRepository,
    private val newEntryRepository: NewEntryRepository,
) : ViewModel() {


    private var _timelineEntries: List<TimelineEntry> = emptyList()

    private var _entriesByDay = MutableStateFlow<Map<LocalDate, List<TimelineEntry>>>(emptyMap())

    val entriesByDay = _entriesByDay.asStateFlow()


    init {
        fetchEntriesByTimeLine()
    }

    fun fetchEntriesByTimeLine() {
        viewModelScope.launch {
            _timelineEntries = newEntryRepository.getTimelineEntries()
            _entriesByDay.value = _timelineEntries.groupBy {
                Instant.ofEpochMilli(it.createdAt)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
            }
        }
    }

}