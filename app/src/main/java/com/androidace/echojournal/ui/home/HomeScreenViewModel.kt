package com.androidace.echojournal.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.androidace.echojournal.audio.AudioPlaybackManager
import com.androidace.echojournal.data.AudioRepository
import com.androidace.echojournal.db.RecordedAudio
import com.androidace.echojournal.db.Topic
import com.androidace.echojournal.repository.NewEntryRepository
import com.androidace.echojournal.repository.TopicRepository
import com.androidace.echojournal.ui.home.model.TimelineEntry
import com.androidace.echojournal.ui.mood.model.Mood
import com.androidace.echojournal.ui.theme.moodColorPaletteMap
import com.androidace.echojournal.util.formatMillis
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class HomeScreenViewModel @Inject constructor(
    private val topicRepository: TopicRepository,
    private val newEntryRepository: NewEntryRepository,
    private val audioRepository: AudioRepository,
    private val playbackManager: AudioPlaybackManager,
) : ViewModel() {

    private var _timelineEntries: List<TimelineEntry> = emptyList()

    private var _entriesByDay = MutableStateFlow<Map<LocalDate, List<TimelineEntry>>>(emptyMap())

    val entriesByDay = _entriesByDay.asStateFlow()

    private var _listTopics: MutableStateFlow<List<DropdownState<Topic>>> =
        MutableStateFlow(emptyList())
    val listTopics = _listTopics.asStateFlow()

    private var _listMood: MutableStateFlow<List<DropdownState<Mood>>> =
        MutableStateFlow(emptyList())
    val listMood = _listMood.asStateFlow()


    private var _selectedMoodFilter =
        MutableStateFlow(mutableListOf<Mood>())
    val selectedMoodFilter = _selectedMoodFilter.asStateFlow()

    private var _selectedTopicsFilter =
        MutableStateFlow(mutableListOf<Topic>())
    val selectedTopicsFilter = _selectedTopicsFilter.asStateFlow()

    private var currentLocalAudio: RecordedAudio? = null

    init {
        fetchEntriesByTimeLine()
        fetchSavedTopics()
        createMoodDropdownList()
    }

    private fun createMoodDropdownList() {
        val moodList =
            persistentListOf(Mood.STRESSED, Mood.SAD, Mood.NEUTRAL, Mood.PEACEFUL, Mood.EXCITED)
        _listMood.value = moodList.map {
            DropdownState(it, isSelected = false)
        }
    }

    private fun fetchEntriesByTimeLine() {
        viewModelScope.launch {
            _timelineEntries = newEntryRepository.getTimelineEntries()
            _entriesByDay.value = _timelineEntries.groupBy {
                Instant.ofEpochMilli(it.createdAt)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
            }
        }
    }

    private fun filterEntriesByTopicsAndMood() {
        viewModelScope.launch {
            val filteredTimeLines =
                if (_selectedMoodFilter.value.isNotEmpty() && _selectedTopicsFilter.value.isNotEmpty()) {
                    newEntryRepository.getTimelineEntriesByTopicList(_selectedTopicsFilter.value)
                        .filter { _selectedMoodFilter.value.contains(it.mood) }
                } else if (_selectedMoodFilter.value.isNotEmpty()) {
                    newEntryRepository.getTimelineEntries()
                        .filter { _selectedMoodFilter.value.contains(it.mood) }
                } else if (_selectedTopicsFilter.value.isNotEmpty()) {
                    newEntryRepository.getTimelineEntriesByTopicList(_selectedTopicsFilter.value)
                } else {
                    newEntryRepository.getTimelineEntries()
                }
            _timelineEntries = filteredTimeLines
            _entriesByDay.value = _timelineEntries.groupBy {
                Instant.ofEpochMilli(it.createdAt)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
            }
        }
    }

    private fun fetchSavedTopics() {
        viewModelScope.launch {
            val topics = topicRepository.getAllTopics()
            _listTopics.value = topics.map {
                DropdownState(it, isSelected = false)
            }
        }
    }

    fun addMoodFilter(mood: Mood) {
        if (selectedMoodFilter.value.contains(mood)) {
            _selectedMoodFilter.value = _selectedMoodFilter.value.filterNot {
                it == mood
            }.toMutableList()
        } else {
            val list = mutableListOf<Mood>()
            list.addAll(_selectedMoodFilter.value)
            list.add(mood)
            _selectedMoodFilter.value = list
        }

        _listMood.value = listMood.value.map { dropdownState ->
            return@map if (dropdownState.data == mood) {
                dropdownState.copy(isSelected = dropdownState.isSelected.not())
            } else {
                dropdownState
            }
        }
        filterEntriesByTopicsAndMood()
    }

    fun addTopicFilter(topic: Topic) {
        if (selectedTopicsFilter.value.contains(topic)) {
            _selectedTopicsFilter.value = _selectedTopicsFilter.value.filterNot {
                it == topic
            }.toMutableList()
        } else {
            val list = mutableListOf<Topic>()
            list.addAll(_selectedTopicsFilter.value)
            list.add(topic)
            _selectedTopicsFilter.value = list
        }
        _listTopics.value = listTopics.value.map { dropdownState ->
            return@map if (dropdownState.data == topic) {
                dropdownState.copy(isSelected = dropdownState.isSelected.not())
            } else {
                dropdownState
            }
        }
        filterEntriesByTopicsAndMood()
    }

    fun clearMoodFilters() {
        _selectedMoodFilter.value = mutableListOf()
        _listMood.value = listMood.value.map { dropdownState ->
            dropdownState.copy(isSelected = false)
        }
        filterEntriesByTopicsAndMood()
    }

    fun clearTopicsFilters() {
        _selectedTopicsFilter.value = mutableListOf()
        _listTopics.value = listTopics.value.map { dropdownState ->
            dropdownState.copy(isSelected = false)
        }
        filterEntriesByTopicsAndMood()
    }

    private fun loadAudio(newEntry: TimelineEntry) {
        viewModelScope.launch {
            try {
                currentLocalAudio =
                    audioRepository.loadAudioByContentId(newEntry.recordingId) ?: return@launch
                currentLocalAudio?.let {
                    playbackManager.initializePlayer(it)
                }
                launch { observePlaybackEvents(newEntry) }
                updatePlaybackState(newEntry)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updatePlaybackState(newEntry: TimelineEntry) {
        if (currentLocalAudio?.fileUri != newEntry.audioPath) {
            loadAudio(newEntry)
        } else {
            if (!newEntry.audioWaveFormState.isPlaying) {
                _timelineEntries = _timelineEntries.map {
                    if (it == newEntry) {
                        it.copy(
                            audioWaveFormState = it.audioWaveFormState.copy(
                                isPlaying = true,
                            )
                        )
                    } else {
                        it
                    }
                }
                _entriesByDay.value = _timelineEntries.groupBy {
                    Instant.ofEpochMilli(it.createdAt)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                }
                playbackManager.play()
            } else {
                _timelineEntries = _timelineEntries.map {
                    if (it == newEntry) {
                        it.copy(
                            audioWaveFormState = it.audioWaveFormState.copy(
                                isPlaying = false,
                            )
                        )
                    } else {
                        it
                    }
                }
                _entriesByDay.value = _timelineEntries.groupBy {
                    Instant.ofEpochMilli(it.createdAt)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                }
                playbackManager.pause()
            }
        }
    }


    private fun observePlaybackEvents(newEntry: TimelineEntry) {
        viewModelScope.launch {
            playbackManager.progressFlow.collectLatest {
                when (it) {
                    is AudioPlaybackManager.Event.PositionChanged -> updatePlaybackProgress(
                        it.position,
                        newEntry
                    )

                    is AudioPlaybackManager.Event.PlayingChanged -> updatePlayingState(
                        it.isPlaying,
                        newEntry
                    )

                    AudioPlaybackManager.Event.OnPlayingComplete -> resetPlayer(newEntry)
                }
            }
        }

    }

    private fun updatePlaybackProgress(position: Long, newEntry: TimelineEntry) {

        val audio = currentLocalAudio ?: return
        val progress = position.toFloat() / audio.timestamp
        _entriesByDay.value = _timelineEntries.map {
            return@map if (it == newEntry) {
                it.copy(
                    audioWaveFormState = it.audioWaveFormState.copy(
                        progress = progress,
                        seekDuration = formatMillis(position)
                    )
                )
            } else {
                it
            }
        }.groupBy {
            Instant.ofEpochMilli(it.createdAt)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
        }
    }

    private fun updatePlayingState(isPlaying: Boolean, newEntry: TimelineEntry) {
        _timelineEntries = _timelineEntries.map {
            if (it == newEntry) {
                it.copy(audioWaveFormState = it.audioWaveFormState.copy(isPlaying = isPlaying))
            } else {
                it
            }
        }
        _entriesByDay.value = _timelineEntries.groupBy {
            Instant.ofEpochMilli(it.createdAt)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
        }
    }

    private fun resetPlayer(newEntry: TimelineEntry) {
        _timelineEntries = _timelineEntries.map {
            if (it == newEntry) {
                it.copy(
                    audioWaveFormState = it.audioWaveFormState.copy(
                        isPlaying = false, progress = 0f,
                        seekDuration = "00:00",
                    )
                )
            } else {
                it
            }
        }
        _entriesByDay.value = _timelineEntries.groupBy {
            Instant.ofEpochMilli(it.createdAt)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
        }
    }


    fun onProgressChange(progress: Float, entry: TimelineEntry) {
        val position = currentLocalAudio?.timestamp?.times(progress)?.toLong() ?: 0L
        playbackManager.seekTo(position)

        _entriesByDay.value = _timelineEntries.map {
            return@map if (it == entry) {
                it.copy(
                    audioWaveFormState = it.audioWaveFormState.copy(
                        progress = progress,
                    )
                )
            } else {
                it.copy(
                    audioWaveFormState = it.audioWaveFormState.copy(
                        isPlaying = false, progress = 0f,
                        seekDuration = "00:00",
                    )
                )
            }
        }.groupBy {
            Instant.ofEpochMilli(it.createdAt)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
        }
    }


}

data class DropdownState<T>(
    val data: T,
    val isSelected: Boolean = false
)