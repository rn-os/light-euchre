package dev.ryanness.lighteuchre.engine

import kotlin.random.Random

/** Seats go 0=You, 1=Left opponent, 2=Partner, 3=Right opponent, turn order increasing. */
enum class Team { US, THEM }

fun seatTeam(seat: Int): Team = if (seat % 2 == 0) Team.US else Team.THEM

fun seatName(seat: Int): String = when (seat) {
    0 -> "You"
    1 -> "Left opponent"
    2 -> "Partner"
    else -> "Right opponent"
}

data class TrickPlay(val seat: Int, val card: Card)

enum class Phase { BIDDING_ROUND_1, DEALER_DISCARD, BIDDING_ROUND_2, PLAYING, HAND_COMPLETE, GAME_COMPLETE }

data class HandSummary(
    val makerSeat: Int,
    val alone: Boolean,
    val makerTricks: Int,
    val defenderTricks: Int,
    val euchred: Boolean,
    val pointsAwarded: Int,
    val pointsTeam: Team,
)

data class EuchreState(
    val hands: List<List<Card>> = List(4) { emptyList() },
    val dealer: Int = 0,
    val turnUpCard: Card? = null,
    val turnedDownSuit: Suit? = null,
    val trump: Suit? = null,
    val maker: Int? = null,
    val alone: Boolean = false,
    val sittingOut: Int? = null,
    val phase: Phase = Phase.BIDDING_ROUND_1,
    val bidTurn: Int = 0,
    val leader: Int = 0,
    val currentTrick: List<TrickPlay> = emptyList(),
    val tricksWon: List<Int> = List(4) { 0 },
    val scores: Map<Team, Int> = mapOf(Team.US to 0, Team.THEM to 0),
    val lastHandSummary: HandSummary? = null,
    val handNumber: Int = 0,
    val log: List<String> = emptyList(),
) {
    val ledSuit: Suit? get() = currentTrick.firstOrNull()?.let { it.card.effectiveSuit(trump!!) }

    fun teamTricks(team: Team): Int = (0..3).filter { seatTeam(it) == team }.sumOf { tricksWon[it] }

    /** Whose turn it is to act, valid during bidding/discard/play phases. */
    fun currentActor(): Int = when (phase) {
        Phase.BIDDING_ROUND_1, Phase.BIDDING_ROUND_2 -> bidTurn
        Phase.DEALER_DISCARD -> dealer
        Phase.PLAYING -> {
            if (currentTrick.isEmpty()) leader
            else nextActiveSeat(currentTrick.last().seat)
        }
        Phase.HAND_COMPLETE, Phase.GAME_COMPLETE -> -1
    }

    fun nextActiveSeat(from: Int): Int {
        var s = (from + 1) % 4
        while (s == sittingOut) s = (s + 1) % 4
        return s
    }

    fun legalPlaysFor(seat: Int): List<Card> {
        val hand = hands[seat]
        val trump = trump ?: return hand
        val led = ledSuit ?: return hand
        val following = hand.filter { it.effectiveSuit(trump) == led }
        return following.ifEmpty { hand }
    }
}

class EuchreEngine(
    private val random: Random = Random,
    val humanSeat: Int = 0,
    startingDealer: Int = 0,
) {
    var state: EuchreState = EuchreState(dealer = startingDealer)
        private set

    fun newGame() {
        state = EuchreState(dealer = state.dealer)
        startNewHand()
    }

    fun startNewHand() {
        val deck = shuffledDeck(random)
        val hands = listOf(
            deck.subList(0, 5).sortedForDisplay(),
            deck.subList(5, 10).sortedForDisplay(),
            deck.subList(10, 15).sortedForDisplay(),
            deck.subList(15, 20).sortedForDisplay(),
        )
        val turnUp = deck[20]
        val firstBidder = (state.dealer + 1) % 4
        state = state.copy(
            hands = hands,
            turnUpCard = turnUp,
            turnedDownSuit = null,
            trump = null,
            maker = null,
            alone = false,
            sittingOut = null,
            phase = Phase.BIDDING_ROUND_1,
            bidTurn = firstBidder,
            leader = firstBidder,
            currentTrick = emptyList(),
            tricksWon = List(4) { 0 },
            lastHandSummary = null,
            handNumber = state.handNumber + 1,
            log = listOf("Hand ${state.handNumber + 1} - ${seatName(state.dealer)} deals. Up card: ${turnUp.label}"),
        )
    }

    // ---- Bidding round 1 ----

    fun orderUp(alone: Boolean = false) {
        require(state.phase == Phase.BIDDING_ROUND_1)
        val turnUp = state.turnUpCard ?: return
        val caller = state.bidTurn
        val newHands = state.hands.toMutableList()
        newHands[state.dealer] = newHands[state.dealer] + turnUp
        val sittingOut = if (alone) (caller + 2) % 4 else null
        state = state.copy(
            hands = newHands,
            trump = turnUp.suit,
            maker = caller,
            alone = alone,
            sittingOut = sittingOut,
            phase = Phase.DEALER_DISCARD,
            log = state.log + buildString {
                append("${seatName(caller)} order${if (caller == humanSeat) "" else "s"} up ${turnUp.suit.symbol}.")
                if (alone) append(" Going alone!")
            },
        )
    }

    fun passBid() {
        require(state.phase == Phase.BIDDING_ROUND_1)
        val passer = state.bidTurn
        if (passer == state.dealer) {
            // everyone passed round 1: turn the card down
            state = state.copy(
                turnedDownSuit = state.turnUpCard?.suit,
                turnUpCard = null,
                phase = Phase.BIDDING_ROUND_2,
                bidTurn = (state.dealer + 1) % 4,
                log = state.log + "${seatName(passer)} passes. Turned down.",
            )
        } else {
            state = state.copy(
                bidTurn = (state.bidTurn + 1) % 4,
                log = state.log + "${seatName(passer)} passes.",
            )
        }
    }

    // ---- Dealer discard ----

    fun discard(card: Card) {
        require(state.phase == Phase.DEALER_DISCARD)
        val newHands = state.hands.toMutableList()
        val dealerHand = newHands[state.dealer]
        require(card in dealerHand)
        newHands[state.dealer] = dealerHand - card
        state = state.copy(hands = newHands, log = state.log + "${seatName(state.dealer)} discards.")
        beginPlay()
    }

    // ---- Bidding round 2 ----

    /** True once the dealer is "stuck": everyone else passed, dealer must name a suit. */
    fun dealerIsStuck(): Boolean = state.phase == Phase.BIDDING_ROUND_2 && state.bidTurn == state.dealer

    fun passBidRound2() {
        require(state.phase == Phase.BIDDING_ROUND_2)
        check(!dealerIsStuck()) { "dealer is stuck and must call a suit" }
        state = state.copy(
            bidTurn = (state.bidTurn + 1) % 4,
            log = state.log + "${seatName(state.bidTurn)} passes.",
        )
    }

    fun callTrump(suit: Suit, alone: Boolean = false) {
        require(state.phase == Phase.BIDDING_ROUND_2)
        require(suit != state.turnedDownSuit) { "cannot call the turned-down suit" }
        val caller = state.bidTurn
        val sittingOut = if (alone) (caller + 2) % 4 else null
        state = state.copy(
            trump = suit,
            maker = caller,
            alone = alone,
            sittingOut = sittingOut,
            log = state.log + buildString {
                append("${seatName(caller)} call${if (caller == humanSeat) "" else "s"} ${suit.symbol}.")
                if (alone) append(" Going alone!")
            },
        )
        beginPlay()
    }

    private fun beginPlay() {
        val firstLeader = (state.dealer + 1) % 4
        val leader = if (firstLeader == state.sittingOut) state.nextActiveSeat(firstLeader) else firstLeader
        state = state.copy(
            phase = Phase.PLAYING,
            leader = leader,
            currentTrick = emptyList(),
        )
    }

    // ---- Playing ----

    fun playCard(seat: Int, card: Card) {
        require(state.phase == Phase.PLAYING)
        require(seat == state.currentActor())
        require(card in state.legalPlaysFor(seat)) { "illegal play: not following suit" }

        val newHands = state.hands.toMutableList()
        newHands[seat] = newHands[seat] - card
        val newTrick = state.currentTrick + TrickPlay(seat, card)
        state = state.copy(
            hands = newHands,
            currentTrick = newTrick,
            log = state.log + "${seatName(seat)} plays ${card.label}.",
        )

        val activeCount = if (state.sittingOut != null) 3 else 4
        if (newTrick.size == activeCount) {
            resolveTrick()
        }
    }

    private fun resolveTrick() {
        val trump = state.trump!!
        val led = state.currentTrick.first().card.effectiveSuit(trump)
        val winner = state.currentTrick.maxWithOrNull { a, b ->
            compareInTrick(a.card, b.card, trump, led)
        }!!.seat

        val newTricksWon = state.tricksWon.toMutableList()
        newTricksWon[winner] = newTricksWon[winner] + 1

        state = state.copy(
            tricksWon = newTricksWon,
            leader = winner,
            currentTrick = emptyList(),
            log = state.log + "${seatName(winner)} takes the trick.",
        )

        if (state.tricksWon.sum() == 5) {
            finishHand()
        }
    }

    private fun finishHand() {
        val maker = state.maker!!
        val makerTeam = seatTeam(maker)
        val defenderTeam = if (makerTeam == Team.US) Team.THEM else Team.US
        val makerTricks = state.teamTricks(makerTeam)
        val defenderTricks = 5 - makerTricks
        val euchred = makerTricks < 3
        val points: Int
        val pointsTeam: Team
        if (euchred) {
            points = 2
            pointsTeam = defenderTeam
        } else {
            pointsTeam = makerTeam
            points = when {
                makerTricks == 5 && state.alone -> 4
                makerTricks == 5 -> 2
                else -> 1
            }
        }
        val newScores = state.scores.toMutableMap()
        newScores[pointsTeam] = (newScores[pointsTeam] ?: 0) + points

        val summary = HandSummary(
            makerSeat = maker,
            alone = state.alone,
            makerTricks = makerTricks,
            defenderTricks = defenderTricks,
            euchred = euchred,
            pointsAwarded = points,
            pointsTeam = pointsTeam,
        )

        val gameOver = newScores.values.any { it >= 10 }
        state = state.copy(
            scores = newScores,
            lastHandSummary = summary,
            phase = if (gameOver) Phase.GAME_COMPLETE else Phase.HAND_COMPLETE,
            log = state.log + buildString {
                if (euchred) append("${seatName(maker)}'s team was euchred! ")
                append("${if (pointsTeam == Team.US) "Your team" else "Opponents"} score $points point${if (points == 1) "" else "s"}.")
            },
        )
    }

    fun continueToNextHand() {
        require(state.phase == Phase.HAND_COMPLETE)
        state = state.copy(dealer = (state.dealer + 1) % 4)
        startNewHand()
    }
}

private fun List<Card>.sortedForDisplay(): List<Card> =
    sortedWith(compareBy({ it.suit.ordinal }, { it.rank.order }))
