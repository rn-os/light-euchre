package dev.ryanness.lighteuchre.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import dev.ryanness.lighteuchre.AboutScreen
import dev.ryanness.lighteuchre.data.Progress
import dev.ryanness.lighteuchre.data.ProgressRepository
import dev.ryanness.lighteuchre.tutorial.lessons
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private const val RESET_CONFIRM_WINDOW_MS = 3000L

class SettingsScreenViewModel(private val progressRepository: ProgressRepository) : LightViewModel<Unit>() {
    val progress: StateFlow<Progress> =
        progressRepository.progress.stateIn(viewModelScope, SharingStarted.Eagerly, Progress.INITIAL)

    fun resetProgress() {
        viewModelScope.launch { progressRepository.resetProgress() }
    }
}

class SettingsScreen(sealedActivity: SealedLightActivity) : LightScreen<Unit, SettingsScreenViewModel>(sealedActivity) {

    override val viewModelClass: Class<SettingsScreenViewModel>
        get() = SettingsScreenViewModel::class.java

    override fun createViewModel(): SettingsScreenViewModel = SettingsScreenViewModel(ProgressRepository(lightContext))

    @Composable
    override fun Content() {
        val progress by viewModel.progress.collectAsState()
        val themeColors by LightThemeController.colors.collectAsState()
        var confirmingReset by remember { mutableStateOf(false) }

        LaunchedEffect(confirmingReset) {
            if (confirmingReset) {
                delay(RESET_CONFIRM_WINDOW_MS)
                confirmingReset = false
            }
        }

        LightTheme(colors = themeColors) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(LightThemeTokens.colors.background),
            ) {
                LightTopBar(
                    leftButton = LightBarButton.LightIcon(icon = LightIcons.BACK, onClick = { goBack() }),
                    center = LightTopBarCenter.Text("Settings"),
                )

                LightScrollView(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 28.dp),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .lightClickable(onClick = { navigateTo(::HelpScreen) })
                            .padding(vertical = 12.dp),
                    ) {
                        LightText(text = "Help & glossary", variant = LightTextVariant.Copy, modifier = Modifier.weight(1f))
                        LightIcon(icon = LightIcons.ARROW_RIGHT)
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .lightClickable(onClick = { navigateTo(::AboutScreen) })
                            .padding(vertical = 12.dp),
                    ) {
                        LightText(text = "About", variant = LightTextVariant.Copy, modifier = Modifier.weight(1f))
                        LightIcon(icon = LightIcons.ARROW_RIGHT)
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .lightClickable(
                                onClick = {
                                    if (confirmingReset) {
                                        confirmingReset = false
                                        viewModel.resetProgress()
                                    } else {
                                        confirmingReset = true
                                    }
                                },
                            )
                            .padding(vertical = 12.dp),
                    ) {
                        LightText(
                            text = if (confirmingReset) "Tap again to confirm reset" else "Reset learning progress",
                            variant = LightTextVariant.Copy,
                        )
                    }
                    if (progress.gamesPlayed > 0 || progress.completedLessons.isNotEmpty()) {
                        LightText(
                            text = "${progress.completedLessons.size} of ${lessons.size} lessons completed",
                            variant = LightTextVariant.Detail,
                            lighten = true,
                            modifier = Modifier.padding(top = 20.dp),
                        )
                    }
                }
            }
        }
    }
}
