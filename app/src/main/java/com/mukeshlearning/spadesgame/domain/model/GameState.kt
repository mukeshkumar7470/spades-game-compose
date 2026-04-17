package com.mukeshlearning.spadesgame.domain.model

enum class PlayerPosition { SOUTH, EAST, NORTH, WEST }
enum class Team { NS, EW }
enum class GamePhase { IDLE, BIDDING, PLAYING, ROUND_OVER, GAME_OVER }

data class Player(
    val id: Int,
    val name: String,
    val position: PlayerPosition,
    val team: Team,
    val isHuman: Boolean = false,
    val hand: List<Card> = emptyList(),
    val bid: Int? = null,
    val tricksWon: Int = 0
)

data class PlayedCard(
    val playerId: Int,
    val position: PlayerPosition,
    val card: Card
)

data class TeamScore(
    val team: Team,
    val totalScore: Int = 0,
    val bags: Int = 0,
    val currentBid: Int? = null,
    val currentTricks: Int = 0
)

data class RoundResult(
    val nsBid: Int,
    val nsTricks: Int,
    val nsEarned: Int,
    val ewBid: Int,
    val ewTricks: Int,
    val ewEarned: Int
)

data class GameState(
    val phase: GamePhase = GamePhase.IDLE,
    val round: Int = 0,
    val players: List<Player> = emptyList(),
    val currentPlayerIndex: Int = 0,
    val tablePlayed: List<PlayedCard> = emptyList(),
    val leadSuit: Suit? = null,
    val spadesBroken: Boolean = false,
    val trickLeaderIndex: Int = 0,
    val nsScore: TeamScore = TeamScore(Team.NS),
    val ewScore: TeamScore = TeamScore(Team.EW),
    val toastMessage: String? = null,
    val roundResult: RoundResult? = null,
    val winnerTeam: Team? = null,
    val selectedCard: Card? = null,
    val biddingOrder: List<Int> = listOf(0, 1, 2, 3)
) {
    val currentPlayer: Player? get() = players.getOrNull(currentPlayerIndex)
    val humanPlayer: Player? get() = players.firstOrNull { it.isHuman }
    val humanPlayerIndex: Int get() = players.indexOfFirst { it.isHuman }
    val isHumanTurn: Boolean get() = currentPlayerIndex == humanPlayerIndex

    fun teamScore(team: Team): TeamScore = if (team == Team.NS) nsScore else ewScore

    fun playersInTeam(team: Team): List<Player> = players.filter { it.team == team }

    fun teamTricks(team: Team): Int = players.filter { it.team == team }.sumOf { it.tricksWon }

    fun teamBid(team: Team): Int? {
        val bids = players.filter { it.team == team }.map { it.bid }
        return if (bids.all { it != null }) bids.sumOf { it!! } else null
    }
}
