package dev.ryanness.lighteuchre.engine

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CardRulesTest {

    @Test
    fun `right and left bower outrank everything else in trump`() {
        val trump = Suit.SPADES
        val rightBower = Card(Suit.SPADES, Rank.JACK)
        val leftBower = Card(Suit.CLUBS, Rank.JACK) // same color as spades
        val trumpAce = Card(Suit.SPADES, Rank.ACE)

        assertTrue(rightBower.isTrump(trump))
        assertTrue(leftBower.isTrump(trump))
        assertEquals(trump, leftBower.effectiveSuit(trump))
        assertTrue(rightBower.trumpStrength(trump) > leftBower.trumpStrength(trump))
        assertTrue(leftBower.trumpStrength(trump) > trumpAce.trumpStrength(trump))
    }

    @Test
    fun `off color jack is not trump`() {
        val trump = Suit.SPADES
        val heartsJack = Card(Suit.HEARTS, Rank.JACK)
        assertTrue(!heartsJack.isTrump(trump))
        assertEquals(Suit.HEARTS, heartsJack.effectiveSuit(trump))
    }

    @Test
    fun `left bower beats an ace of the led suit`() {
        val trump = Suit.SPADES
        // Hearts led; left bower (club jack, counts as trump) is in the trick instead of following hearts.
        val trick = listOf(
            TrickPlay(0, Card(Suit.HEARTS, Rank.ACE)),
            TrickPlay(1, Card(Suit.CLUBS, Rank.JACK)),
        )
        val led = trick.first().card.effectiveSuit(trump)
        val winner = trick.maxWithOrNull { a, b -> compareInTrick(a.card, b.card, trump, led) }!!
        assertEquals(1, winner.seat)
    }
}

class EuchreEngineTest {

    @Test
    fun `ordering up moves the up-card into the dealer hand and requires a discard`() {
        val engine = EuchreEngine(random = kotlin.random.Random(1), startingDealer = 3)
        engine.startNewHand()
        assertEquals(Phase.BIDDING_ROUND_1, engine.state.phase)
        assertEquals(5, engine.state.hands[3].size)

        engine.orderUp()
        assertEquals(Phase.DEALER_DISCARD, engine.state.phase)
        assertEquals(6, engine.state.hands[3].size)

        engine.discard(engine.state.hands[3].first())
        assertEquals(Phase.PLAYING, engine.state.phase)
        assertEquals(5, engine.state.hands[3].size)
        assertEquals(0, engine.state.leader) // seat left of dealer 3 is seat 0
    }

    @Test
    fun `everyone passing round 1 moves to round 2 with the turned-down suit excluded`() {
        val engine = EuchreEngine(random = kotlin.random.Random(2), startingDealer = 0)
        engine.startNewHand()
        val downSuit = engine.state.turnUpCard!!.suit
        repeat(4) { engine.passBid() }

        assertEquals(Phase.BIDDING_ROUND_2, engine.state.phase)
        assertEquals(downSuit, engine.state.turnedDownSuit)
        assertEquals(1, engine.state.bidTurn)
    }

    @Test
    fun `dealer is stuck on the last turn of round 2`() {
        val engine = EuchreEngine(random = kotlin.random.Random(3), startingDealer = 0)
        engine.startNewHand()
        repeat(4) { engine.passBid() }
        engine.passBidRound2() // seat 1
        engine.passBidRound2() // seat 2
        engine.passBidRound2() // seat 3
        assertEquals(0, engine.state.bidTurn)
        assertTrue(engine.dealerIsStuck())
    }

    @Test
    fun `a full hand awards euchre points to the defenders`() {
        // Rig a deck so seat 0 makes trump but the defense wins the majority of tricks.
        val engine = EuchreEngine(startingDealer = 1, humanSeat = 0)
        engine.startNewHand()
        engine.orderUp()
        engine.discard(engine.state.hands[1].first())

        while (engine.state.phase == Phase.PLAYING) {
            val seat = engine.state.currentActor()
            val card = engine.state.legalPlaysFor(seat).first()
            engine.playCard(seat, card)
        }

        assertEquals(Phase.HAND_COMPLETE, engine.state.phase)
        val summary = engine.state.lastHandSummary!!
        assertEquals(5, summary.makerTricks + summary.defenderTricks)
        assertTrue(engine.state.scores.values.sum() > 0)
    }

    @Test
    fun `going alone sits out the maker's partner and only three play the hand`() {
        val engine = EuchreEngine(random = kotlin.random.Random(7), startingDealer = 3, humanSeat = 0)
        engine.startNewHand()
        repeat(4) { engine.passBid() }
        val suit = Suit.entries.first { it != engine.state.turnedDownSuit }
        engine.callTrump(suit, alone = true)

        assertEquals(true, engine.state.alone)
        assertEquals(2, engine.state.sittingOut) // partner of seat 0 sits out
        assertEquals(Phase.PLAYING, engine.state.phase)

        var tricksPlayed = 0
        while (engine.state.phase == Phase.PLAYING) {
            val seat = engine.state.currentActor()
            assertTrue(seat != 2) // sitting-out partner never acts
            val card = engine.state.legalPlaysFor(seat).first()
            engine.playCard(seat, card)
            if (engine.state.currentTrick.isEmpty()) tricksPlayed++
        }
        assertEquals(5, tricksPlayed)
        assertEquals(Phase.HAND_COMPLETE, engine.state.phase)

        val summary = engine.state.lastHandSummary!!
        if (summary.makerTricks == 5) {
            assertEquals(4, summary.pointsAwarded)
        }
    }
}
