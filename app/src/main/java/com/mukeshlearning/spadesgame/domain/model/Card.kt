package com.mukeshlearning.spadesgame.domain.model

enum class Suit(val symbol: String, val displayName: String) {
    SPADES("♠", "Spades"),
    HEARTS("♥", "Hearts"),
    DIAMONDS("♦", "Diamonds"),
    CLUBS("♣", "Clubs")
}

enum class Rank(val displayName: String, val value: Int) {
    TWO("2", 2), THREE("3", 3), FOUR("4", 4), FIVE("5", 5),
    SIX("6", 6), SEVEN("7", 7), EIGHT("8", 8), NINE("9", 9),
    TEN("10", 10), JACK("J", 11), QUEEN("Q", 12), KING("K", 13), ACE("A", 14)
}

data class Card(val suit: Suit, val rank: Rank) {
    val isRed: Boolean get() = suit == Suit.HEARTS || suit == Suit.DIAMONDS
    val displayRank: String get() = rank.displayName
    val suitSymbol: String get() = suit.symbol
}

fun buildDeck(): List<Card> =
    Suit.entries.flatMap { suit -> Rank.entries.map { rank -> Card(suit, rank) } }.shuffled()
