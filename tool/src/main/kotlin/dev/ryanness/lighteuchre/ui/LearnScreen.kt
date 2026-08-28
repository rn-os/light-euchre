package dev.ryanness.lighteuchre.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import com.thelightphone.sdk.LightScreen
import com.thelightphone.sdk.LightViewModel
import com.thelightphone.sdk.SealedLightActivity
import com.thelightphone.sdk.ui.LightBarButton
import com.thelightphone.sdk.ui.LightIcon
import com.thelightphone.sdk.ui.LightIcons
import com.thelightphone.sdk.ui.LightScrollView
import com.thelightphone.sdk.ui.LightText
import com.thelightphone.sdk.ui.LightTextVariant
import com.thelightphone.sdk.ui.LightTheme
import com.thelightphone.sdk.ui.LightThemeController
import com.thelightphone.sdk.ui.LightThemeTokens
import com.thelightphone.sdk.ui.LightTopBar
import com.thelightphone.sdk.ui.LightTopBarCenter
import com.thelightphone.sdk.ui.lightClickable
import dev.ryanness.lighteuchre.data.Progress
import dev.ryanness.lighteuchre.data.ProgressRepository
import dev.ryanness.lighteuchre.tutorial.lessons
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class LearnScreenViewModel(progressRepository: ProgressRepository) : LightViewModel<Unit>() {
    val progress: StateFlow<Progress> =
        progressRepository.progress.stateIn(viewModelScope, SharingStarted.Eagerly, Progress.INITIAL)
}

class LearnScreen(sealedActivity: SealedLightActivity) : LightScreen<Unit, LearnScreenViewModel>(sealedActivity) {

    override val viewModelClass: Class<LearnScreenViewModel>
        get() = LearnScreenViewModel::class.java

    override fun createViewModel(): LearnScreenViewModel = LearnScreenViewModel(ProgressRepository(lightContext))

    @Composable
    override fun Content() {
        val progress by viewModel.progress.collectAsState()
        val themeColors by LightThemeController.colors.collectAsState()

        LightTheme(colors = themeColors) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(LightThemeTokens.colors.background),
            ) {
                LightTopBar(
                    leftButton = LightBarButton.LightIcon(icon = LightIcons.BACK, onClick = { goBack() }),
                    center = LightTopBarCenter.Text("Learn"),
                )

                LightScrollView(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 28.dp),
                ) {
                    LightText(
                        text = "Work through these in order to learn Euchre from scratch.",
                        variant = LightTextVariant.Detail,
                        lighten = true,
                        modifier = Modifier.padding(top = 16.dp, bottom = 16.dp),
                    )
                    lessons.forEachIndexed { index, lesson ->
                        val unlocked = index <= progress.highestUnlockedLesson
                        val completed = index in progress.completedLessons
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .let { m ->
                                    if (unlocked) {
                                        m.lightClickable(onClick = { navigateTo({ sa -> LessonScreen(sa, lesson.id) }) })
                                    } else {
                                        m
                                    }
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                LightText(
                                    text = "${index + 1}. ${lesson.title}",
                                    variant = LightTextVariant.Copy,
                                    lighten = !unlocked,
                                )
                                LightText(
                                    text = lesson.summary,
                                    variant = LightTextVariant.Fine,
                                    lighten = true,
                                    modifier = Modifier.padding(top = 2.dp),
                                )
                            }
                            when {
                                completed -> LightIcon(icon = LightIcons.STAR)
                                unlocked -> LightIcon(icon = LightIcons.ARROW_RIGHT)
                                else -> Unit
                            }
                        }
                    }
                }
            }
        }
    }
}
