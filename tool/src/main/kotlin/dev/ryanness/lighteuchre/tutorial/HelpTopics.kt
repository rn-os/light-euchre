package dev.ryanness.lighteuchre.tutorial

data class HelpTopic(
    val id: Int,
    val title: String,
    val keywords: List<String>,
    val body: String,
)

val helpTopics: List<HelpTopic> = listOf(
    HelpTopic(
        id = 0,
        title = "Bowers",
        keywords = listOf("bower", "bowers", "jack", "jacks", "left bower", "right bower"),
        body = "The Jack of the trump suit is the Right Bower - the strongest card in the " +
            "game. The Jack of the OTHER suit that shares trump's color is the Left Bower - " +
            "it switches sides and counts as trump too, ranking just below the Right Bower. " +
            "Spades/Clubs share a color, and so do Hearts/Diamonds.",
    ),
    HelpTopic(
        id = 1,
        title = "What is trump?",
        keywords = listOf("trump", "what is trump"),
        body = "Trump is the suit chosen each hand that beats every other suit. Every trump " +
            "card - even the lowly 9 - beats every card of a non-trump suit, no matter how " +
            "high. From strongest to weakest: right bower, left bower, then A, K, Q, 10, 9 " +
            "of the trump suit.",
    ),
    HelpTopic(
        id = 2,
        title = "Following suit",
        keywords = listOf("follow suit", "following", "must follow", "led suit", "led"),
        body = "If you have a card matching the suit that was led, you must play one. The " +
            "left bower counts as trump for this - if it was led, you need a trump card to " +
            "follow, not a card of its printed suit.",
    ),
    HelpTopic(
        id = 3,
        title = "Trumping in",
        keywords = listOf("trump in", "trumping", "void", "can't follow"),
        body = "If you don't have any card of the suit that was led, you may play anything - " +
            "including a trump card. This is called trumping in, and any trump beats every " +
            "card of the led suit.",
    ),
    HelpTopic(
        id = 4,
        title = "Ordering it up",
        keywords = listOf("order up", "order it up", "pick up", "round one"),
        body = "After the deal, one card is turned face-up. Starting left of the dealer, each " +
            "player may order it up - accept that card's suit as trump - or pass. If someone " +
            "orders it up, the dealer picks the card into their hand and discards one back " +
            "down to five.",
    ),
    HelpTopic(
        id = 5,
        title = "Calling trump (round two)",
        keywords = listOf("call", "calling", "round two", "name a suit", "turned down"),
        body = "If everyone passes on the turned-up card, it's turned face down and never " +
            "seen again. In round two, starting left of the dealer, each player may name any " +
            "OTHER suit as trump, or pass.",
    ),
    HelpTopic(
        id = 6,
        title = "Sticking the dealer",
        keywords = listOf("stuck", "stick the dealer", "dealer stuck", "redeal"),
        body = "There's no redeal in Euchre. If everyone passes twice, the dealer is stuck - " +
            "forced to name a trump suit themselves, any suit except the one that was turned " +
            "down.",
    ),
    HelpTopic(
        id = 7,
        title = "Going alone",
        keywords = listOf("alone", "go alone", "sitting out", "sit out", "solo"),
        body = "Whoever calls trump can go alone: their partner sits out the hand entirely, " +
            "even if they're the dealer. Taking all 5 tricks alone scores 4 points instead of " +
            "the usual 2, but you're playing 1 against 2.",
    ),
    HelpTopic(
        id = 8,
        title = "Getting euchred",
        keywords = listOf("euchre", "euchred", "set", "defended"),
        body = "If the team that called trump takes fewer than 3 of the 5 tricks, they've " +
            "been euchred - the other team scores 2 points instead, even though they never " +
            "called trump.",
    ),
    HelpTopic(
        id = 9,
        title = "Scoring & the march",
        keywords = listOf("score", "scoring", "march", "points", "point", "win the game"),
        body = "Taking 3 or 4 tricks scores 1 point. Taking all 5 - a march - scores 2 " +
            "points, or 4 if you did it alone. Falling short of 3 tricks after calling trump " +
            "hands the defense 2 points. First team to 10 wins.",
    ),
    HelpTopic(
        id = 10,
        title = "The dealer's discard",
        keywords = listOf("discard", "bury", "dealer discard", "pick up card"),
        body = "Whenever a suit is ordered up, the dealer picks the turned-up card into their " +
            "hand - now holding six - and discards one card face down to get back to five. " +
            "This happens even if the dealer's side is about to sit out the hand.",
    ),
    HelpTopic(
        id = 11,
        title = "Winning a trick",
        keywords = listOf("trick", "win trick", "highest card", "leads"),
        body = "If nobody trumps, the highest card of the suit that was led wins the trick. " +
            "If anyone trumps in, the highest trump played wins instead. Whoever wins leads " +
            "the next trick.",
    ),
)

fun searchHelpTopics(query: String): List<HelpTopic> {
    val terms = query.trim().lowercase().split(Regex("\\s+")).filter { it.isNotBlank() }
    if (terms.isEmpty()) return helpTopics

    return helpTopics
        .map { topic ->
            val haystack = (listOf(topic.title) + topic.keywords + listOf(topic.body)).joinToString(" ").lowercase()
            topic to terms.count { term -> haystack.contains(term) }
        }
        .filter { it.second > 0 }
        .sortedByDescending { it.second }
        .map { it.first }
}
