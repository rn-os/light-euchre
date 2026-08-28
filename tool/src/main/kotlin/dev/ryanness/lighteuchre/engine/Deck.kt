package dev.ryanness.lighteuchre.engine

fun shuffledDeck(random: kotlin.random.Random = kotlin.random.Random): List<Card> =
    fullDeck().shuffled(random)
