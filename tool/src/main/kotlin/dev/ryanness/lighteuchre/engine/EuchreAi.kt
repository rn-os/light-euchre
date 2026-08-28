package dev.ryanness.lighteuchre.engine

/** Simple heuristic opponent - strong enough to be a fair teaching partner, not a card-counter. */
object EuchreAi {

    private fun trumpValue(card: Card, trump: Suit): Double = when {
        card.isRightBower(trump) -> 3.0
        card.isLeftBower(trump) -> 2.5
        card.suit == trump -> when (card.rank) {
            Rank.ACE -> 1.5
            Rank.KING -> 1.1
            Rank.QUEEN -> 0.8
            Rank.TEN -> 0.5
            Rank.NINE -> 0.3
            Rank.JACK -> 0.0 // unreachable: trump jack is always the right bower
        }
        card.rank == Rank.ACE -> 0.5
        else -> 0.0
    }

    private fun handStrength(hand: List<Card>, trump: Suit): Double = hand.sumOf { trumpValue(it, trump) }

    /** Decide what to do on this player's turn in bidding round 1. Returns Pair(orderUp, alone). */
    fun decideRound1(hand: List<Card>, turnUp: Card, isDealer: Boolean): Pair<Boolean, Boolean> {
        val strength = handStrength(hand, turnUp.suit) + if (isDealer) 0.6 else 0.0
        val orderUp = strength >= 2.4
        val alone = strength >= 4.3
        return orderUp to alone
    }

    /** Decide bidding round 2. Returns the suit to call, or null to pass. */
    fun decideRound2(hand: List<Card>, excludeSuit: Suit, mustCall: Boolean): Pair<Suit, Boolean>? {
        val candidates = Suit.entries.filter { it != excludeSuit }
        val best = candidates.maxByOrNull { handStrength(hand, it) } ?: return null
        val strength = handStrength(hand, best)
        if (!mustCall && strength < 2.2) return null
        return best to (strength >= 4.3)
    }

    fun chooseDiscard(hand: List<Card>, trump: Suit): Card =
        hand.minByOrNull { trumpValue(it, trump) * 10 + it.rank.order } ?: hand.first()

    fun chooseCardToPlay(
        hand: List<Card>,
        trump: Suit,
        currentTrick: List<TrickPlay>,
        mySeat: Int,
        partnerSeat: Int?,
    ): Card {
        val legal = run {
            val led = currentTrick.firstOrNull()?.card?.effectiveSuit(trump)
            if (led == null) hand
            else hand.filter { it.effectiveSuit(trump) == led }.ifEmpty { hand }
        }
        if (legal.size == 1) return legal.first()

        if (currentTrick.isEmpty()) {
            // Leading: prefer a strong off-suit ace, otherwise the lowest card.
            val offAce = legal.filter { !it.isTrump(trump) && it.rank == Rank.ACE }.maxByOrNull { it.rank.order }
            return offAce ?: legal.minByOrNull { effectiveOrder(it, trump) }!!
        }

        val led = currentTrick.first().card.effectiveSuit(trump)
        val currentBest = currentTrick.maxWithOrNull { a, b -> compareInTrick(a.card, b.card, trump, led) }!!
        val partnerWinning = partnerSeat != null && currentBest.seat == partnerSeat

        val winningPlays = legal.filter { compareInTrick(it, currentBest.card, trump, led) > 0 }

        if (partnerWinning) {
            // Don't waste a winner on your partner's trick; play the lowest safe card.
            val nonTrumpChoices = legal.filter { !it.isTrump(trump) }
            return (nonTrumpChoices.ifEmpty { legal }).minByOrNull { effectiveOrder(it, trump) }!!
        }

        if (winningPlays.isNotEmpty()) {
            return winningPlays.minByOrNull { effectiveOrder(it, trump) }!!
        }

        // Can't win: throw the lowest card, preferring to keep trump in hand.
        val nonTrumpChoices = legal.filter { !it.isTrump(trump) }
        return (nonTrumpChoices.ifEmpty { legal }).minByOrNull { effectiveOrder(it, trump) }!!
    }

    private fun effectiveOrder(card: Card, trump: Suit): Int =
        if (card.isTrump(trump)) 100 + card.trumpStrength(trump) else card.rank.order
}
