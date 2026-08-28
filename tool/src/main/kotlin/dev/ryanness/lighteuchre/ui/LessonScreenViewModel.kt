package dev.ryanness.lighteuchre.ui

import androidx.lifecycle.viewModelScope
import com.thelightphone.sdk.LightViewModel
import dev.ryanness.lighteuchre.data.ProgressRepository
import dev.ryanness.lighteuchre.tutorial.Lesson
import dev.ryanness.lighteuchre.tutorial.lessons
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class LessonUiState(
    val lesson: Lesson,
    val stepIndex: Int = 0,
    val selectedOption: Int? = null,
    val finished: Boolean = false,
)

class LessonScreenViewModel(
    lessonId: Int,
    private val progressRepository: ProgressRepository,
) : LightViewModel<Unit>() {

    private val lesson = lessons.first { it.id == lessonId }
    private val _state = MutableStateFlow(LessonUiState(lesson = lesson))
    val state: StateFlow<LessonUiState> = _state

    fun selectOption(index: Int) {
        _state.value = _state.value.copy(selectedOption = index)
    }

    fun next() {
        val current = _state.value
        if (current.stepIndex >= lesson.steps.lastIndex) {
            viewModelScope.launch { progressRepository.markLessonComplete(lesson.id, lessons.size) }
            _state.value = current.copy(finished = true)
        } else {
            _state.value = current.copy(stepIndex = current.stepIndex + 1, selectedOption = null)
        }
    }
}
