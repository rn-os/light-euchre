package dev.ryanness.lighteuchre.tutorial

import dev.ryanness.lighteuchre.tutorial.LessonStep.Explain
import dev.ryanness.lighteuchre.tutorial.LessonStep.Quiz

val lessons: List<Lesson> = listOf(
    Lesson(
        id = 0,
        title = "The Deck & Trump",
        summary = "24 cards, and the twist that trips up everyone at first.",
        steps = listOf(
            Explain(
                title = "Just 24 cards",
                body = "Euchre uses only the 9, 10, J, Q, K, and A of each suit - 24 cards " +
                    "total. Four players sit in two partnerships, with partners across from " +
                    "each other.",
            ),
            Explain(
                title = "Trump beats everything",
                body = "Each hand, one suit becomes trump. Every card in the trump suit beats " +
                    "every card of every other suit - even the 9 of trump beats the Ace of " +
                    "another suit.",
            ),
            Explain(
                title = "The bowers",
                body = "Here's the twist: the Jack of the trump suit is the Right Bower, the " +
                    "single strongest card in the game. The Jack of the OTHER suit that shares " +
                    "trump's color becomes the Left Bower - it switches sides and counts as " +
                    "trump too, ranking just under the Right Bower.",
            ),
            Explain(
                title = "Full trump order",
                body = "From strongest to weakest when a suit is trump:\n" +
                    "J (trump) - Right Bower\n" +
                    "J (same color) - Left Bower\n" +
                    "A, K, Q, 10, 9 of trump\n\n" +
                    "Spades and Clubs share a color, and so do Hearts and Diamonds - that's " +
                    "which suits swap bowers.",
            ),
            Quiz(
                question = "Spades is trump. Which of these is the strongest card?",
                options = listOf("9 of Spades", "Jack of Clubs", "Ace of Spades", "Queen of Spades"),
                correctIndex = 1,
                explanation = "Clubs shares its color with Spades, so the Jack of Clubs becomes " +
                    "the Left Bower - trump, and second only to the Jack of Spades itself.",
            ),
            Quiz(
                question = "Hearts is trump. Is the Jack of Diamonds a trump card?",
                options = listOf("Yes - it's the left bower", "No - Diamonds isn't trump"),
                correctIndex = 0,
                explanation = "Hearts and Diamonds are both red suits, so the Jack of Diamonds " +
                    "switches sides to become the Left Bower whenever Hearts is trump.",
            ),
            Quiz(
                question = "Clubs is trump. Which of these is NOT a trump card?",
                options = listOf("Jack of Clubs", "Jack of Spades", "Jack of Hearts", "9 of Clubs"),
                correctIndex = 2,
                explanation = "Spades shares its color with Clubs, so the Jack of Spades is the " +
                    "left bower. The Jack of Hearts belongs to a different color entirely, so " +
                    "it stays an ordinary Heart.",
            ),
        ),
    ),
    Lesson(
        id = 1,
        title = "Bidding: Calling Trump",
        summary = "Ordering it up, calling it, and sticking the dealer.",
        steps = listOf(
            Explain(
                title = "The turn-up card",
                body = "After the deal, the next card is turned face-up. Starting with the " +
                    "player left of the dealer, everyone gets one chance to 'order it up' - " +
                    "accept that card's suit as trump - or pass.",
            ),
            Explain(
                title = "The dealer picks it up",
                body = "If anyone orders it up, the dealer picks that card into their hand and " +
                    "discards one card to get back to five. That card's suit is trump for the " +
                    "whole hand, no matter who ordered it up.",
            ),
            Explain(
                title = "Round two: naming it",
                body = "If all four players pass, the card is turned face down and it's never " +
                    "seen again. Now, starting left of the dealer once more, each player may " +
                    "name any OTHER suit as trump, or pass.",
            ),
            Explain(
                title = "Sticking the dealer",
                body = "There's no redeal in Euchre. If everyone passes a second time, the " +
                    "dealer is 'stuck' - they're forced to name a trump suit themselves.",
            ),
            Explain(
                title = "Going alone",
                body = "Whoever calls trump can choose to go alone: their partner sits out the " +
                    "hand entirely. It's a gamble - taking all 5 tricks alone is worth double " +
                    "points, but you're playing 1 against 2.",
            ),
            Quiz(
                question = "You hold both bowers and the Ace of trump, plus two weak off-suit " +
                    "cards. The turned-up card matches. Should you order it up?",
                options = listOf("Yes", "No"),
                correctIndex = 0,
                explanation = "Three guaranteed trump winners including both bowers is one of " +
                    "the strongest hands possible - always take trump this strong.",
            ),
            Quiz(
                question = "Everyone has passed twice, and it's the dealer's turn in round two. " +
                    "What happens?",
                options = listOf(
                    "The hand is thrown in and redealt",
                    "The dealer must name a trump suit",
                    "The turned-down suit becomes trump automatically",
                ),
                correctIndex = 1,
                explanation = "The dealer is stuck - Euchre never redeals. They have to call " +
                    "some suit other than the one turned down.",
            ),
        ),
    ),
    Lesson(
        id = 2,
        title = "Playing Tricks",
        summary = "Following suit, trumping in, and winning the hand.",
        steps = listOf(
            Explain(
                title = "Leading the trick",
                body = "The player to the dealer's left leads the very first trick, playing " +
                    "any card. After that, whoever wins a trick leads the next one.",
            ),
            Explain(
                title = "Follow suit if you can",
                body = "Everyone else must follow suit - play a card matching the suit that was " +
                    "led - if they're able to. Remember the left bower: it counts as trump for " +
                    "this too, not its printed suit.",
            ),
            Explain(
                title = "Trumping in",
                body = "Can't follow suit? Play anything you like, including trump - this is " +
                    "called 'trumping in'. Any trump card beats every card of the suit that was " +
                    "led, no matter how high.",
            ),
            Explain(
                title = "Winning the trick",
                body = "If nobody trumps, the highest card of the led suit wins. If somebody " +
                    "trumps, the highest trump played wins instead. Highest trump always wins, " +
                    "no exceptions.",
            ),
            Quiz(
                question = "Hearts was led. You have no hearts at all, but you do hold the 9 of " +
                    "trump. Can you play it?",
                options = listOf("Yes - you can trump in when void of the led suit", "No - the 9 is too weak to play here"),
                correctIndex = 0,
                explanation = "You may always play trump when you can't follow suit, and even " +
                    "the lowest trump beats every card of the suit that was led.",
            ),
            Quiz(
                question = "Spades is trump. Diamonds was led. You hold the Jack of Hearts and " +
                    "the 9 of Diamonds. What must you play?",
                options = listOf("The 9 of Diamonds", "The Jack of Hearts", "Either card - your choice"),
                correctIndex = 0,
                explanation = "The Jack of Hearts is just an ordinary Heart here (Hearts isn't " +
                    "trump-colored with Spades), so it can't follow suit. You hold a Diamond, " +
                    "so you must play it.",
            ),
        ),
    ),
    Lesson(
        id = 3,
        title = "Scoring & Going Alone",
        summary = "How hands turn into points, and the fastest way to win.",
        steps = listOf(
            Explain(
                title = "Winning the hand",
                body = "Whichever team called trump needs at least 3 of the 5 tricks to " +
                    "'make it'. Taking 3 or 4 tricks scores 1 point.",
            ),
            Explain(
                title = "The march",
                body = "Take all 5 tricks - a 'march' - and it's worth 2 points instead of 1. " +
                    "Go alone and march all 5 by yourself, and it's worth 4.",
            ),
            Explain(
                title = "Getting euchred",
                body = "If the team that called trump takes fewer than 3 tricks, they've been " +
                    "'euchred' - the other team scores 2 points instead, even though they never " +
                    "called trump at all.",
            ),
            Explain(
                title = "Game point",
                body = "First team to 10 points wins the game. Because a euchre swings 2 points " +
                    "to the defense, calling trump on a shaky hand is genuinely risky.",
            ),
            Quiz(
                question = "Your team called trump and took all 5 tricks, with your partner " +
                    "playing normally (not alone). How many points?",
                options = listOf("1 point", "2 points", "4 points"),
                correctIndex = 1,
                explanation = "A march - all 5 tricks - is worth 2 points. It only becomes 4 " +
                    "when the maker went alone.",
            ),
            Quiz(
                question = "Your team called trump but only won 2 tricks. What happens?",
                options = listOf(
                    "You still score 1 point",
                    "The other team scores 2 points - you got euchred",
                    "Nothing - the hand is replayed",
                ),
                correctIndex = 1,
                explanation = "Falling short of 3 tricks after calling trump hands the other " +
                    "team 2 points, regardless of who they are or what they held.",
            ),
        ),
    ),
)
