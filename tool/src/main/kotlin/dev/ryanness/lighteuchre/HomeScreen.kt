package dev.ryanness.lighteuchre

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
import com.thelightphone.sdk.InitialScreen
import com.thelightphone.sdk.LightScreen
import com.thelightphone.sdk.LightViewModel
import com.thelightphone.sdk.SealedLightActivity
import com.thelightphone.sdk.ui.LightBarButton
import com.thelightphone.sdk.ui.LightBottomBar
import com.thelightphone.sdk.ui.LightIcon
import com.thelightphone.sdk.ui.LightIcons
import com.thelightphone.sdk.ui.LightScrollView
import com.thelightphone.sdk.ui.LightText
import com.thelightphone.sdk.ui.LightTextVariant
import com.thelightphone.sdk.ui.LightTheme
import com.thelightphone.sdk.ui.LightThemeController
import com.thelightphone.sdk.ui.LightThemeTokens
import com.thelightphone.sdk.ui.lightClickable
import dev.ryanness.lighteuchre.data.Progress
import dev.ryanness.lighteuchre.data.ProgressRepository
import dev.ryanness.lighteuchre.ui.GameScreen
import dev.ryanness.lighteuchre.ui.LearnScreen
import dev.ryanness.lighteuchre.ui.SettingsScreen
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class HomeScreenViewModel(progressRepository: ProgressRepository) : LightViewModel<Unit>() {
    val progress: StateFlow<Progress> =
        progressRepository.progress.stateIn(viewModelScope, SharingStarted.Eagerly, Progress.INITIAL)
}

/**
 * The app's one persistent "home" - always just the Play view. Learn and Settings are real
 * pushed screens (see [LearnScreen], [SettingsScreen]) reached from the bottom bar, not in-place
 * tabs, so the platform's own back gesture and back-arrow correctly return here instead of ever
 * being able to exit the app from a sub-view.
 */
@InitialScreen
class HomeScreen(sealedActivity: SealedLightActivity) : LightScreen<Unit, HomeScreenViewModel>(sealedActivity) {

    override val viewModelClass: Class<HomeScreenViewModel>
        get() = HomeScreenViewModel::class.java

    override fun createViewModel(): HomeScreenViewModel = HomeScreenViewModel(ProgressRepository(lightContext))

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
                LightScrollView(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 28.dp),
                ) {
                    LightText(
                        text = "Light Euchre",
                        variant = LightTextVariant.Title,
                        modifier = Modifier.padding(top = 16.dp),
                    )
                    LightText(
                        text = "A trick-taking card game, taught one hand at a time.",
                        variant = LightTextVariant.Copy,
                        lighten = true,
                        modifier = Modifier.padding(top = 8.dp, bottom = 28.dp),
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .lightClickable(onClick = { navigateTo(::GameScreen) })
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        LightText(text = "Play a game", variant = LightTextVariant.Subheading, modifier = Modifier.weight(1f))
                        LightIcon(icon = LightIcons.ARROW_RIGHT)
                    }

                    if (progress.gamesPlayed > 0) {
                        LightText(
                            text = "${progress.gamesWon} won out of ${progress.gamesPlayed} played",
                            variant = LightTextVariant.Detail,
                            lighten = true,
                            modifier = Modifier.padding(top = 20.dp),
                        )
                    }
                }

                LightBottomBar(
                    items = listOf(
                        LightBarButton.LightIcon(
                            icon = LightIcons.PLAY,
                            contentDescription = "Play",
                            onClick = { navigateTo(::GameScreen) },
                        ),
                        LightBarButton.LightIcon(
                            icon = LightIcons.LIST,
                            contentDescription = "Learn",
                            onClick = { navigateTo(::LearnScreen) },
                        ),
                        LightBarButton.LightIcon(
                            icon = LightIcons.SETTINGS,
                            contentDescription = "Settings",
                            onClick = { navigateTo(::SettingsScreen) },
                        ),
                    ),
                )
            }
        }
    }
}
