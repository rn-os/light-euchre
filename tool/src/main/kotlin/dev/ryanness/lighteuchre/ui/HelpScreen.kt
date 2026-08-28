package dev.ryanness.lighteuchre.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.thelightphone.sdk.LightScreen
import com.thelightphone.sdk.LightViewModel
import com.thelightphone.sdk.SealedLightActivity
import com.thelightphone.sdk.ui.LightBarButton
import com.thelightphone.sdk.ui.LightIcon
import com.thelightphone.sdk.ui.LightIcons
import com.thelightphone.sdk.ui.LightScrollView
import com.thelightphone.sdk.ui.LightText
import com.thelightphone.sdk.ui.LightTextField
import com.thelightphone.sdk.ui.LightTextInputEditor
import com.thelightphone.sdk.ui.LightTextVariant
import com.thelightphone.sdk.ui.LightTheme
import com.thelightphone.sdk.ui.LightThemeController
import com.thelightphone.sdk.ui.LightThemeTokens
import com.thelightphone.sdk.ui.LightTopBar
import com.thelightphone.sdk.ui.LightTopBarCenter
import com.thelightphone.sdk.ui.defaultKeyboardOptions
import com.thelightphone.sdk.ui.lightClickable
import dev.ryanness.lighteuchre.tutorial.HelpTopic
import dev.ryanness.lighteuchre.tutorial.helpTopics
import dev.ryanness.lighteuchre.tutorial.searchHelpTopics
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

data class HelpUiState(
    val query: String = "",
    val isEditingQuery: Boolean = false,
    val editSessionId: Int = 0,
    val selectedTopic: HelpTopic? = null,
)

class HelpScreenViewModel : LightViewModel<Unit>() {
    private val _state = MutableStateFlow(HelpUiState())
    val state: StateFlow<HelpUiState> = _state

    fun startEditingQuery() {
        _state.update { it.copy(isEditingQuery = true, editSessionId = it.editSessionId + 1) }
    }

    fun cancelEditingQuery() {
        _state.update { it.copy(isEditingQuery = false) }
    }

    fun submitQuery(text: String) {
        _state.update { it.copy(query = text, isEditingQuery = false, selectedTopic = null) }
    }

    fun selectTopic(topic: HelpTopic) {
        _state.update { it.copy(selectedTopic = topic) }
    }

    fun clearSelectedTopic() {
        _state.update { it.copy(selectedTopic = null) }
    }
}

/**
 * A real pushed screen (not a same-screen overlay) so the platform's own back handling - the
 * edge-swipe gesture and its top-edge arrow - correctly steps back to whatever screen opened
 * Help, the same way it already does for every other screen. The Light SDK doesn't give tools a
 * way to intercept that gesture themselves (androidx BackHandler is blocked at build time), so an
 * in-place overlay would have made that gesture jump all the way out of the surrounding screen.
 */
class HelpScreen(sealedActivity: SealedLightActivity) : LightScreen<Unit, HelpScreenViewModel>(sealedActivity) {

    override val viewModelClass: Class<HelpScreenViewModel>
        get() = HelpScreenViewModel::class.java

    override fun createViewModel(): HelpScreenViewModel = HelpScreenViewModel()

    @Composable
    override fun Content() {
        val state by viewModel.state.collectAsState()
        val themeColors by LightThemeController.colors.collectAsState()

        LightTheme(colors = themeColors) {
            if (state.isEditingQuery) {
                val queryFieldState = rememberTextFieldState(state.query)
                val keyboardOptionsFlow = remember { MutableStateFlow(defaultKeyboardOptions()) }
                LightTextInputEditor(
                    title = "Ask about...",
                    state = queryFieldState,
                    onSubmit = { viewModel.submitQuery(it.toString()) },
                    onBack = { viewModel.cancelEditingQuery() },
                    keyboardOptionsFlow = keyboardOptionsFlow,
                    singleLine = true,
                    editorKey = state.editSessionId,
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(LightThemeTokens.colors.background),
                ) {
                    LightTopBar(
                        leftButton = LightBarButton.LightIcon(
                            icon = LightIcons.BACK,
                            onClick = {
                                if (state.selectedTopic != null) viewModel.clearSelectedTopic() else goBack()
                            },
                        ),
                        center = LightTopBarCenter.Text("Help"),
                    )

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                    ) {
                        val topic = state.selectedTopic
                        if (topic != null) {
                            LightScrollView(modifier = Modifier.fillMaxSize()) {
                                LightText(
                                    text = topic.title,
                                    variant = LightTextVariant.Heading,
                                    modifier = Modifier.padding(top = 16.dp, bottom = 12.dp),
                                )
                                LightText(text = topic.body, variant = LightTextVariant.Paragraph)
                            }
                        } else {
                            LightTextField(
                                label = "Search",
                                value = state.query,
                                placeholder = "e.g. \"left bower\", \"going alone\"",
                                onClick = { viewModel.startEditingQuery() },
                                modifier = Modifier.padding(top = 16.dp, bottom = 16.dp),
                            )

                            val results = remember(state.query) { searchHelpTopics(state.query) }
                            val noMatches = state.query.isNotBlank() && results.isEmpty()
                            LightText(
                                text = when {
                                    state.query.isBlank() -> "All topics"
                                    noMatches -> "No matches for \"${state.query}\" - browse all topics below"
                                    else -> "Results for \"${state.query}\""
                                },
                                variant = LightTextVariant.Detail,
                                lighten = true,
                                modifier = Modifier.padding(bottom = 8.dp),
                            )

                            LightScrollView(modifier = Modifier.fillMaxSize()) {
                                val list = if (noMatches) helpTopics else results
                                list.forEach { item ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .lightClickable(onClick = { viewModel.selectTopic(item) })
                                            .padding(vertical = 10.dp),
                                    ) {
                                        LightText(text = item.title, variant = LightTextVariant.Copy, modifier = Modifier.weight(1f))
                                        LightIcon(icon = LightIcons.ARROW_RIGHT)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
