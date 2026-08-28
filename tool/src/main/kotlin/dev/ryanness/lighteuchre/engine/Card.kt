package dev.ryanness.lighteuchre.engine

enum class CardColor { RED, BLACK }

enum class Suit(val symbol: String, val label: String, val color: CardColor) {
    SPADES("♠", "Spades", CardColor.BLACK),
    HEARTS("♥", "Hearts", CardColor.RED),
    DIAMONDS("♦", "Diamonds", CardColor.RED),
    CLUBS("♣", "Clubs", CardColor.BLACK),
}

/** The other suit of the same color, i.e. the suit whose jack becomes the left bower. */
fun Suit.sameColorSuit(): Suit = when (this) {
    Suit.SPADES -> Suit.CLUBS
    Suit.CLUBS -> Suit.SPADES
    Suit.HEARTS -> Suit.DIAMONDS
    Suit.DIAMONDS -> Suit.HEARTS
}

enum class Rank(val label: String, val order: Int) {
    NINE("9", 0),
    TEN("10", 1),
    JACK("J", 2),
    QUEEN("Q", 3),
    KING("K", 4),
    ACE("A", 5),
}

data class Card(val suit: Suit, val rank: Rank) {
    val label: String get() = "${rank.label}${suit.symbol}"

    /** True if this card is the jack that would become a bower under [trump]. */
    fun isJackOf(trump: Suit): Boolean =
        rank == Rank.JACK && (suit == trump || suit == trump.sameColorSuit())

    fun isRightBower(trump: Suit): Boolean = rank == Rank.JACK && suit == trump

    fun isLeftBower(trump: Suit): Boolean = rank == Rank.JACK && suit == trump.sameColorSuit()

    /**
     * The suit this card "counts as" once [trump] is set. The left bower (the same-color jack)
     * counts as trump even though it's printed in the other suit - this is the trick that
     * trips up every new Euchre player.
     */
    fun effectiveSuit(trump: Suit): Suit = if (isLeftBower(trump)) trump else suit

    fun isTrump(trump: Suit): Boolean = effectiveSuit(trump) == trump

    /**
     * Rank used to compare two trump cards. Bowers sit above the rest of the trump suit;
     * the trump jack itself never appears here since it's always the right bower.
     */
    fun trumpStrength(trump: Suit): Int = when {
        isRightBower(trump) -> 6
        isLeftBower(trump) -> 5
        else -> when (rank) {
            Rank.NINE -> 0
            Rank.TEN -> 1
            Rank.QUEEN -> 2
            Rank.KING -> 3
            Rank.ACE -> 4
            Rank.JACK -> error("non-trump jack has no trump strength")
        }
    }
}

/** Compares two cards as played in the same trick. Returns >0 if [a] beats [b]. */
fun compareInTrick(a: Card, b: Card, trump: Suit, ledSuit: Suit): Int {
    val aTrump = a.isTrump(trump)
    val bTrump = b.isTrump(trump)
    if (aTrump && bTrump) return a.trumpStrength(trump) - b.trumpStrength(trump)
    if (aTrump) return 1
    if (bTrump) return -1

    val aFollows = a.effectiveSuit(trump) == ledSuit
    val bFollows = b.effectiveSuit(trump) == ledSuit
    if (aFollows && bFollows) return a.rank.order - b.rank.order
    if (aFollows) return 1
    if (bFollows) return -1
    return 0 // neither followed suit nor trumped; whichever was already winning stays winning
}

fun fullDeck(): List<Card> =
    Suit.entries.flatMap { suit -> Rank.entries.map { rank -> Card(suit, rank) } }
