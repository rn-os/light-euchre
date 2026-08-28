package dev.ryanness.lighteuchre.ui

import androidx.lifecycle.viewModelScope
import com.thelightphone.sdk.LightViewModel
import com.thelightphone.sdk.SimpleLightScreen
import dev.ryanness.lighteuchre.data.ProgressRepository
import dev.ryanness.lighteuchre.engine.Card
import dev.ryanness.lighteuchre.engine.EuchreAi
import dev.ryanness.lighteuchre.engine.EuchreEngine
import dev.ryanness.lighteuchre.engine.EuchreState
import dev.ryanness.lighteuchre.engine.Phase
import dev.ryanness.lighteuchre.engine.Suit
import dev.ryanness.lighteuchre.engine.Team
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

private const val HUMAN_SEAT = 0
private const val AI_THINK_DELAY_MS = 650L

class GameViewModel(private val progressRepository: ProgressRepository) : LightViewModel<Unit>() {
    private val engine = EuchreEngine(humanSeat = HUMAN_SEAT)
    private var aiJob: Job? = null
    private var recordedThisGame = false

    private val _state = MutableStateFlow(engine.state)
    val state: StateFlow<EuchreState> = _state

    init {
        engine.newGame()
        publish()
    }

    // Pauses the AI while another screen (e.g. Help) is on top, so nothing plays out unseen.
    override fun onScreenHide(screen: SimpleLightScreen<Unit>) {
        aiJob?.cancel()
    }

    override fun onScreenShow(screen: SimpleLightScreen<Unit>) {
        maybeRunAiTurn()
    }

    fun newGame() {
        recordedThisGame = false
        engine.newGame()
        publish()
    }

    fun orderUp(alone: Boolean = false) {
        if (engine.state.currentActor() != HUMAN_SEAT) return
        engine.orderUp(alone)
        publish()
    }

    fun passBid() {
        if (engine.state.currentActor() != HUMAN_SEAT) return
        engine.passBid()
        publish()
    }

    fun callTrump(suit: Suit, alone: Boolean = false) {
        if (engine.state.currentActor() != HUMAN_SEAT) return
        engine.callTrump(suit, alone)
        publish()
    }

    fun passBidRound2() {
        if (engine.state.currentActor() != HUMAN_SEAT || engine.dealerIsStuck()) return
        engine.passBidRound2()
        publish()
    }

    fun discard(card: Card) {
        if (engine.state.dealer != HUMAN_SEAT) return
        engine.discard(card)
        publish()
    }

    fun playCard(card: Card) {
        if (engine.state.currentActor() != HUMAN_SEAT) return
        engine.playCard(HUMAN_SEAT, card)
        publish()
    }

    fun continueToNextHand() {
        engine.continueToNextHand()
        publish()
    }

    private fun publish() {
        _state.value = engine.state
        maybeRecordGameResult()
        maybeRunAiTurn()
    }

    private fun maybeRecordGameResult() {
        val s = engine.state
        if (s.phase == Phase.GAME_COMPLETE && !recordedThisGame) {
            recordedThisGame = true
            val won = (s.scores[Team.US] ?: 0) > (s.scores[Team.THEM] ?: 0)
            viewModelScope.launch { progressRepository.recordGameResult(won) }
        }
    }

    private fun maybeRunAiTurn() {
        val s = engine.state
        if (s.phase == Phase.HAND_COMPLETE || s.phase == Phase.GAME_COMPLETE) return
        val actor = s.currentActor()
        if (actor == HUMAN_SEAT) return

        aiJob?.cancel()
        aiJob = viewModelScope.launch {
            delay(AI_THINK_DELAY_MS)
            performAiTurn(actor)
            _state.value = engine.state
            maybeRunAiTurn()
        }
    }

    private fun performAiTurn(seat: Int) {
        val s = engine.state
        when (s.phase) {
            Phase.BIDDING_ROUND_1 -> {
                val (order, alone) = EuchreAi.decideRound1(s.hands[seat], s.turnUpCard!!, seat == s.dealer)
                if (order) engine.orderUp(alone) else engine.passBid()
            }
            Phase.DEALER_DISCARD -> {
                engine.discard(EuchreAi.chooseDiscard(s.hands[seat], s.trump!!))
            }
            Phase.BIDDING_ROUND_2 -> {
                val mustCall = engine.dealerIsStuck()
                val choice = EuchreAi.decideRound2(s.hands[seat], s.turnedDownSuit!!, mustCall)
                if (choice != null) engine.callTrump(choice.first, choice.second) else engine.passBidRound2()
            }
            Phase.PLAYING -> {
                val partnerSeat = (seat + 2) % 4
                val card = EuchreAi.chooseCardToPlay(s.hands[seat], s.trump!!, s.currentTrick, seat, partnerSeat)
                engine.playCard(seat, card)
            }
            Phase.HAND_COMPLETE, Phase.GAME_COMPLETE -> Unit
        }
    }
}
