package dev.ryanness.lighteuchre.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.thelightphone.sdk.LightScreen
import com.thelightphone.sdk.SealedLightActivity
import com.thelightphone.sdk.ui.LightBarButton
import com.thelightphone.sdk.ui.LightBottomBar
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
import dev.ryanness.lighteuchre.data.ProgressRepository
import dev.ryanness.lighteuchre.tutorial.LessonStep

class LessonScreen(
    sealedActivity: SealedLightActivity,
    private val lessonId: Int,
) : LightScreen<Unit, LessonScreenViewModel>(sealedActivity) {

    override val viewModelClass: Class<LessonScreenViewModel>
        get() = LessonScreenViewModel::class.java

    override fun createViewModel(): LessonScreenViewModel =
        LessonScreenViewModel(lessonId, ProgressRepository(lightContext))

    @Composable
    override fun Content() {
        val uiState by viewModel.state.collectAsState()
        val themeColors by LightThemeController.colors.collectAsState()

        LightTheme(colors = themeColors) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(LightThemeTokens.colors.background),
            ) {
                LightTopBar(
                    leftButton = LightBarButton.LightIcon(icon = LightIcons.BACK, onClick = { goBack() }),
                    center = LightTopBarCenter.Text(uiState.lesson.title),
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                ) {
                    if (uiState.finished) {
                        LessonFinishedBody(uiState.lesson.title)
                    } else {
                        LightScrollView(modifier = Modifier.fillMaxSize()) {
                            LightText(
                                text = "Step ${uiState.stepIndex + 1} of ${uiState.lesson.steps.size}",
                                variant = LightTextVariant.Detail,
                                lighten = true,
                                modifier = Modifier.padding(top = 12.dp, bottom = 12.dp),
                            )
                            when (val step = uiState.lesson.steps[uiState.stepIndex]) {
                                is LessonStep.Explain -> ExplainStepBody(step)
                                is LessonStep.Quiz -> QuizStepBody(step, uiState.selectedOption, viewModel::selectOption)
                            }
                            Spacer(modifier = Modifier.padding(bottom = 24.dp))
                        }
                    }
                }

                LightBottomBar(
                    items = listOf(
                        LightBarButton.Text(
                            text = if (uiState.finished) "DONE" else if (uiState.stepIndex >= uiState.lesson.steps.lastIndex) "FINISH" else "NEXT",
                            onClick = { if (uiState.finished) goBack() else viewModel.next() },
                        ),
                    ),
                )
            }
        }
    }
}

@Composable
private fun ExplainStepBody(step: LessonStep.Explain) {
    Column {
        step.title?.let {
            LightText(text = it, variant = LightTextVariant.Heading, modifier = Modifier.padding(bottom = 12.dp))
        }
        LightText(text = step.body, variant = LightTextVariant.Paragraph)
    }
}

@Composable
private fun QuizStepBody(step: LessonStep.Quiz, selected: Int?, onSelect: (Int) -> Unit) {
    Column {
        LightText(text = step.question, variant = LightTextVariant.Copy, modifier = Modifier.padding(bottom = 16.dp))
        step.options.forEachIndexed { index, option ->
            val isSelected = selected == index
            val isCorrect = index == step.correctIndex
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .lightClickable(onClick = { onSelect(index) })
                    .padding(vertical = 10.dp),
            ) {
                val marker = when {
                    selected == null -> "○"
                    isSelected && isCorrect -> "✓"
                    isSelected && !isCorrect -> "✗"
                    isCorrect -> "✓"
                    else -> "○"
                }
                LightText(
                    text = "$marker  $option",
                    variant = LightTextVariant.Copy,
                    lighten = selected != null && !isCorrect && !isSelected,
                )
            }
        }
        if (selected != null) {
            LightText(
                text = step.explanation,
                variant = LightTextVariant.Paragraph,
                lighten = true,
                modifier = Modifier.padding(top = 12.dp),
            )
        }
    }
}

@Composable
private fun LessonFinishedBody(lessonTitle: String) {
    Column {
        LightText(
            text = "Lesson complete",
            variant = LightTextVariant.Heading,
            modifier = Modifier.padding(top = 24.dp, bottom = 12.dp),
        )
        LightText(text = "You've finished \"$lessonTitle\".", variant = LightTextVariant.Copy)
    }
}
