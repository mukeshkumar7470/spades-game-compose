package com.mukeshlearning.spadesgame.domain

import com.mukeshlearning.spadesgame.domain.model.*

object SpadesGameLogic {

    fun createInitialPlayers(): List<Player> = listOf(
        Player(id = 0, name = "You",    position = PlayerPosition.SOUTH, team = Team.NS, isHuman = true),
        Player(id = 1, name = "East",   position = PlayerPosition.EAST,  team = Team.EW),
        Player(id = 2, name = "North",  position = PlayerPosition.NORTH, team = Team.NS),
        Player(id = 3, name = "West",   position = PlayerPosition.WEST,  team = Team.EW),
    )

    fun dealCards(players: List<Player>): List<Player> {
        val deck = buildDeck().toMutableList()
        val hands = Array(4) { mutableListOf<Card>() }
        deck.forEachIndexed { i, card -> hands[i % 4].add(card) }
        return players.mapIndexed { i, p ->
            p.copy(
                hand = hands[i].sortedWith(
                    compareByDescending<Card> { it.suit.ordinal }.thenByDescending { it.rank.value }
                ).reversed().sortedWith(
                    compareBy<Card> { suitSortOrder(it.suit) }.thenByDescending { it.rank.value }
                ),
                bid = null,
                tricksWon = 0
            )
        }
    }

    private fun suitSortOrder(suit: Suit): Int = when (suit) {
        Suit.SPADES -> 0
        Suit.HEARTS -> 1
        Suit.DIAMONDS -> 2
        Suit.CLUBS -> 3
    }

    fun getPlayableCards(
        hand: List<Card>,
        tablePlayed: List<PlayedCard>,
        leadSuit: Suit?,
        spadesBroken: Boolean
    ): List<Card> {
        if (tablePlayed.isEmpty()) {
            // Leading the trick
            if (!spadesBroken) {
                val nonSpades = hand.filter { it.suit != Suit.SPADES }
                if (nonSpades.isNotEmpty()) return nonSpades
            }
            return hand
        }
        // Must follow suit
        val mustFollow = hand.filter { it.suit == leadSuit }
        if (mustFollow.isNotEmpty()) return mustFollow
        return hand
    }

    fun getWinningPlay(tablePlayed: List<PlayedCard>, leadSuit: Suit?): PlayedCard? {
        if (tablePlayed.isEmpty()) return null
        return tablePlayed.maxWithOrNull(Comparator { a, b ->
            compareCards(a.card, b.card, leadSuit)
        })
    }

    fun compareCards(a: Card, b: Card, leadSuit: Suit?): Int {
        val aSpade = a.suit == Suit.SPADES
        val bSpade = b.suit == Suit.SPADES
        return when {
            aSpade && !bSpade -> 1
            !aSpade && bSpade -> -1
            a.suit == b.suit -> a.rank.value.compareTo(b.rank.value)
            a.suit == leadSuit -> 1
            b.suit == leadSuit -> -1
            else -> 0
        }
    }

    fun calcAiBid(hand: List<Card>): Int {
        var bid = 0.0
        for (card in hand) {
            bid += when {
                card.suit == Suit.SPADES && card.rank.value >= Rank.ACE.value   -> 1.0
                card.suit == Suit.SPADES && card.rank.value >= Rank.QUEEN.value -> 0.9
                card.suit == Suit.SPADES && card.rank.value >= Rank.NINE.value  -> 0.5
                card.suit == Suit.SPADES && card.rank.value >= Rank.SIX.value   -> 0.3
                card.suit != Suit.SPADES && card.rank.value >= Rank.ACE.value   -> 0.85
                card.suit != Suit.SPADES && card.rank.value >= Rank.KING.value  -> 0.6
                card.suit != Suit.SPADES && card.rank.value >= Rank.QUEEN.value -> 0.3
                else -> 0.0
            }
        }
        return bid.toInt().coerceIn(1, 9)
    }

    fun chooseAiCard(
        playerId: Int,
        team: Team,
        playable: List<Card>,
        tablePlayed: List<PlayedCard>,
        leadSuit: Suit?,
        players: List<Player>
    ): Card {
        if (tablePlayed.isEmpty()) {
            // Lead: prefer lowest non-spade
            val nonSpades = playable.filter { it.suit != Suit.SPADES }
            val pool = if (nonSpades.isNotEmpty()) nonSpades else playable
            return pool.minByOrNull { it.rank.value }!!
        }

        val winning = getWinningPlay(tablePlayed, leadSuit) ?: return playable.minByOrNull { it.rank.value }!!
        val winnerTeam = players.firstOrNull { it.id == winning.playerId }?.team

        // Partner is winning - dump lowest
        if (winnerTeam == team) {
            return playable.minByOrNull { it.rank.value }!!
        }

        // Try to win cheaply
        val canWin = playable.filter { card ->
            compareCards(card, winning.card, leadSuit) > 0
        }
        if (canWin.isNotEmpty()) {
            return canWin.minByOrNull { if (it.suit == Suit.SPADES) it.rank.value + 100 else it.rank.value }!!
        }

        // Can't win - dump lowest value
        return playable.minByOrNull { it.rank.value }!!
    }

    fun scoreRound(
        players: List<Player>,
        nsScore: TeamScore,
        ewScore: TeamScore
    ): Pair<TeamScore, TeamScore> {
        val nsTricks = players.filter { it.team == Team.NS }.sumOf { it.tricksWon }
        val ewTricks = players.filter { it.team == Team.EW }.sumOf { it.tricksWon }
        val nsBid    = players.filter { it.team == Team.NS }.sumOf { it.bid ?: 0 }
        val ewBid    = players.filter { it.team == Team.EW }.sumOf { it.bid ?: 0 }

        fun calcTeam(tricks: Int, bid: Int, score: TeamScore): TeamScore {
            val earned: Int
            var newBags = score.bags
            if (tricks >= bid) {
                val overtricks = tricks - bid
                newBags += overtricks
                val bagPenalty = if (newBags >= 10) { newBags -= 10; 100 } else 0
                earned = bid * 10 + overtricks - bagPenalty
            } else {
                earned = -(bid * 10)
            }
            return score.copy(totalScore = score.totalScore + earned, bags = newBags)
        }

        return calcTeam(nsTricks, nsBid, nsScore) to calcTeam(ewTricks, ewBid, ewScore)
    }
}
