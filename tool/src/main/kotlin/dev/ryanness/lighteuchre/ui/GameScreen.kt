package dev.ryanness.lighteuchre.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.text.style.TextAlign
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
import dev.ryanness.lighteuchre.engine.EuchreState
import dev.ryanness.lighteuchre.engine.Phase
import dev.ryanness.lighteuchre.engine.Suit
import dev.ryanness.lighteuchre.engine.Team
import dev.ryanness.lighteuchre.engine.seatName

class GameScreen(sealedActivity: SealedLightActivity) : LightScreen<Unit, GameViewModel>(sealedActivity) {

    override val viewModelClass: Class<GameViewModel>
        get() = GameViewModel::class.java

    override fun createViewModel(): GameViewModel = GameViewModel(ProgressRepository(lightContext))

    @Composable
    override fun Content() {
        val state by viewModel.state.collectAsState()
        val themeColors by LightThemeController.colors.collectAsState()

        LightTheme(colors = themeColors) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(LightThemeTokens.colors.background),
            ) {
                LightTopBar(
                    leftButton = LightBarButton.LightIcon(icon = LightIcons.BACK, onClick = { goBack() }),
                    center = LightTopBarCenter.Text(
                        "US ${state.scores[Team.US] ?: 0} - THEM ${state.scores[Team.THEM] ?: 0}",
                    ),
                    rightButton = LightBarButton.Text("HELP", onClick = { navigateTo(::HelpScreen) }),
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                ) {
                    when (state.phase) {
                        Phase.BIDDING_ROUND_1 -> BiddingRound1Body(state, viewModel)
                        Phase.DEALER_DISCARD -> DealerDiscardBody(state, viewModel)
                        Phase.BIDDING_ROUND_2 -> BiddingRound2Body(state, viewModel)
                        Phase.PLAYING -> PlayingBody(state, viewModel)
                        Phase.HAND_COMPLETE -> HandCompleteBody(state)
                        Phase.GAME_COMPLETE -> GameCompleteBody(state)
                    }
                }

                GameBottomBar(state = state, viewModel = viewModel)
            }
        }
    }
}

@Composable
private fun GameBottomBar(state: EuchreState, viewModel: GameViewModel) {
    val isHumanTurn = state.currentActor() == 0
    when (state.phase) {
        Phase.BIDDING_ROUND_1 -> if (isHumanTurn) {
            LightBottomBar(
                items = listOf(
                    LightBarButton.Text("PASS", onClick = viewModel::passBid),
                    LightBarButton.Text("ORDER UP", onClick = { viewModel.orderUp(alone = false) }),
                    LightBarButton.Text("ALONE", onClick = { viewModel.orderUp(alone = true) }),
                ),
            )
        }

        Phase.BIDDING_ROUND_2 -> if (isHumanTurn && state.bidTurn != state.dealer) {
            LightBottomBar(items = listOf(LightBarButton.Text("PASS", onClick = viewModel::passBidRound2)))
        }

        Phase.HAND_COMPLETE -> LightBottomBar(
            items = listOf(LightBarButton.Text("CONTINUE", onClick = viewModel::continueToNextHand)),
        )

        Phase.GAME_COMPLETE -> LightBottomBar(
            items = listOf(LightBarButton.Text("NEW GAME", onClick = viewModel::newGame)),
        )

        else -> Unit
    }
}

@Composable
private fun BiddingRound1Body(state: EuchreState, viewModel: GameViewModel) {
    val isHumanTurn = state.currentActor() == 0
    Column {
        LightText(
            text = "Turned up: ${state.turnUpCard?.label ?: ""}",
            variant = LightTextVariant.Heading,
            modifier = Modifier.padding(top = 16.dp, bottom = 4.dp),
        )
        LightText(
            text = if (isHumanTurn) "Order it up, or pass?" else "${seatName(state.currentActor())} is deciding...",
            variant = LightTextVariant.Copy,
            lighten = true,
            modifier = Modifier.padding(bottom = 20.dp),
        )
        YourHand(state)
        RecentLog(state)
    }
}

@Composable
private fun DealerDiscardBody(state: EuchreState, viewModel: GameViewModel) {
    Column {
        LightText(
            text = "Trump is ${state.trump?.symbol}. Pick a card to bury.",
            variant = LightTextVariant.Heading,
            modifier = Modifier.padding(top = 16.dp, bottom = 20.dp),
        )
        PlayerHandRow(hand = state.hands[0], onPlay = viewModel::discard)
        RecentLog(state)
    }
}

@Composable
private fun BiddingRound2Body(state: EuchreState, viewModel: GameViewModel) {
    val isHumanTurn = state.currentActor() == 0
    val isStuck = state.bidTurn == state.dealer
    Column {
        LightText(
            text = "${state.turnedDownSuit?.symbol} was turned down.",
            variant = LightTextVariant.Heading,
            modifier = Modifier.padding(top = 16.dp, bottom = 4.dp),
        )
        LightText(
            text = when {
                !isHumanTurn -> "${seatName(state.currentActor())} is deciding..."
                isStuck -> "You're stuck - name a trump suit."
                else -> "Name a suit, or pass."
            },
            variant = LightTextVariant.Copy,
            lighten = true,
            modifier = Modifier.padding(bottom = 16.dp),
        )
        if (isHumanTurn) {
            Suit.entries.filter { it != state.turnedDownSuit }.forEach { suit ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .lightClickable(onClick = { viewModel.callTrump(suit, alone = false) }),
                    ) {
                        LightText(text = "Call ${suit.symbol} ${suit.label}", variant = LightTextVariant.Copy)
                    }
                    Row(
                        modifier = Modifier
                            .lightClickable(onClick = { viewModel.callTrump(suit, alone = true) })
                            .padding(start = 16.dp),
                    ) {
                        LightText(text = "ALONE", variant = LightTextVariant.Detail, lighten = true)
                    }
                }
            }
        }
        YourHand(state)
        RecentLog(state)
    }
}

@Composable
private fun PlayingBody(state: EuchreState, viewModel: GameViewModel) {
    Column {
        LightText(
            text = "Trump: ${state.trump?.symbol}" + if (state.alone) " - going alone" else "",
            variant = LightTextVariant.Subheading,
            modifier = Modifier.padding(top = 12.dp, bottom = 4.dp),
        )
        LightText(
            text = "Tricks: You+Partner ${state.teamTricks(Team.US)} - Opponents ${state.teamTricks(Team.THEM)}",
            variant = LightTextVariant.Detail,
            lighten = true,
            modifier = Modifier.padding(bottom = 16.dp),
        )

        TrickTable(state)

        Column(modifier = Modifier.weight(1f)) { RecentLog(state) }

        if (state.sittingOut == 0) {
            LightText(
                text = "Your partner is playing this hand alone - you're sitting out.",
                variant = LightTextVariant.Copy,
                lighten = true,
                modifier = Modifier.padding(bottom = 12.dp),
            )
        } else {
            LightText(text = "Your hand", variant = LightTextVariant.Detail, lighten = true, modifier = Modifier.padding(bottom = 6.dp))
            val legal = if (state.currentActor() == 0) state.legalPlaysFor(0).toSet() else emptySet()
            PlayerHandRow(
                hand = state.hands[0],
                legalPlays = if (state.currentActor() == 0) legal else state.hands[0].toSet(),
                onPlay = if (state.currentActor() == 0) viewModel::playCard else null,
                modifier = Modifier.padding(bottom = 12.dp),
            )
        }
    }
}

@Composable
private fun TrickTable(state: EuchreState) {
    Column(modifier = Modifier.padding(bottom = 12.dp)) {
        for (seat in 0..3) {
            if (seat == state.sittingOut) continue
            val play = state.currentTrick.firstOrNull { it.seat == seat }
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                LightText(text = seatName(seat), variant = LightTextVariant.Copy)
                LightText(text = play?.card?.label ?: "-", variant = LightTextVariant.Copy, lighten = play == null)
            }
        }
    }
}

@Composable
private fun HandCompleteBody(state: EuchreState) {
    val summary = state.lastHandSummary
    Column {
        LightText(text = "Hand complete", variant = LightTextVariant.Heading, modifier = Modifier.padding(top = 16.dp, bottom = 12.dp))
        if (summary != null) {
            val text = if (summary.euchred) {
                "${seatName(summary.makerSeat)}'s team was euchred! " +
                    "${if (summary.pointsTeam == Team.US) "You" else "Opponents"} score ${summary.pointsAwarded}."
            } else {
                "${seatName(summary.makerSeat)}'s team took ${summary.makerTricks} tricks. " +
                    "${if (summary.pointsTeam == Team.US) "You" else "Opponents"} score ${summary.pointsAwarded}."
            }
            LightText(text = text, variant = LightTextVariant.Copy, align = TextAlign.Start)
        }
    }
}

@Composable
private fun GameCompleteBody(state: EuchreState) {
    val youWon = (state.scores[Team.US] ?: 0) > (state.scores[Team.THEM] ?: 0)
    Column {
        LightText(
            text = if (youWon) "You win!" else "Opponents win",
            variant = LightTextVariant.Title,
            modifier = Modifier.padding(top = 24.dp, bottom = 12.dp),
        )
        LightText(
            text = "Final score: US ${state.scores[Team.US] ?: 0} - THEM ${state.scores[Team.THEM] ?: 0}",
            variant = LightTextVariant.Copy,
        )
    }
}

@Composable
private fun YourHand(state: EuchreState) {
    LightText(text = "Your hand", variant = LightTextVariant.Detail, lighten = true, modifier = Modifier.padding(bottom = 6.dp))
    PlayerHandRow(hand = state.hands[0], modifier = Modifier.padding(bottom = 12.dp))
}

@Composable
private fun RecentLog(state: EuchreState) {
    LightScrollView(modifier = Modifier.fillMaxWidth()) {
        state.log.takeLast(5).forEach { line ->
            LightText(
                text = line,
                variant = LightTextVariant.Fine,
                lighten = true,
                modifier = Modifier.padding(vertical = 2.dp),
            )
        }
    }
}
