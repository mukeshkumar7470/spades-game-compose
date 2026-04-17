package com.mukeshlearning.spadesgame.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mukeshlearning.spadesgame.domain.SpadesGameLogic
import com.mukeshlearning.spadesgame.domain.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GameViewModel : ViewModel() {

    private val _gameState = MutableStateFlow(GameState())
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    init {
        startGame()
    }

    fun startGame() {
        val players = SpadesGameLogic.createInitialPlayers()
        val dealt   = SpadesGameLogic.dealCards(players)
        _gameState.value = GameState(
            phase            = GamePhase.BIDDING,
            round            = 1,
            players          = dealt,
            currentPlayerIndex = 0,
            nsScore          = TeamScore(Team.NS),
            ewScore          = TeamScore(Team.EW)
        )
        processAiBids()
    }

    private fun nextRound() {
        val state   = _gameState.value
        val players = SpadesGameLogic.dealCards(state.players)
        _gameState.value = state.copy(
            phase              = GamePhase.BIDDING,
            round              = state.round + 1,
            players            = players,
            currentPlayerIndex = 0,
            tablePlayed        = emptyList(),
            leadSuit           = null,
            spadesBroken       = false,
            trickLeaderIndex   = 0,
            toastMessage       = null,
            roundResult        = null,
            selectedCard       = null
        )
        processAiBids()
    }

    // ─── BIDDING ────────────────────────────────────────────────────────────

    private fun processAiBids() {
        viewModelScope.launch {
            var state = _gameState.value
            while (state.phase == GamePhase.BIDDING && !state.isHumanTurn) {
                delay(300)
                state = _gameState.value
                if (state.phase != GamePhase.BIDDING) break
                val p   = state.currentPlayerIndex
                val bid = SpadesGameLogic.calcAiBid(state.players[p].hand)
                applyBid(p, bid)
                state = _gameState.value
            }
        }
    }

    fun placeBid(bid: Int) {
        val state = _gameState.value
        if (state.phase != GamePhase.BIDDING || !state.isHumanTurn) return
        applyBid(state.humanPlayerIndex, bid)
        processAiBids()
    }

    private fun applyBid(playerIndex: Int, bid: Int) {
        val state   = _gameState.value
        val players = state.players.mapIndexed { i, p ->
            if (i == playerIndex) p.copy(bid = bid) else p
        }
        val nextIndex = (playerIndex + 1) % 4
        val allBid    = players.all { it.bid != null }

        _gameState.value = state.copy(
            players            = players,
            currentPlayerIndex = if (allBid) state.trickLeaderIndex else nextIndex,
            phase              = if (allBid) GamePhase.PLAYING else GamePhase.BIDDING
        )

        if (allBid) triggerAiPlayIfNeeded()
    }

    // ─── PLAYING ────────────────────────────────────────────────────────────

    fun onCardSelected(card: Card) {
        val state = _gameState.value
        if (state.phase != GamePhase.PLAYING || !state.isHumanTurn) return
        val playable = SpadesGameLogic.getPlayableCards(
            hand         = state.humanPlayer?.hand ?: return,
            tablePlayed  = state.tablePlayed,
            leadSuit     = state.leadSuit,
            spadesBroken = state.spadesBroken
        )
        if (playable.none { it == card }) {
            _gameState.value = state.copy(toastMessage = "You must follow suit!")
            return
        }
        // Single tap = select, second tap on selected = play
        if (state.selectedCard == card) {
            playCardForHuman(card)
        } else {
            _gameState.value = state.copy(selectedCard = card, toastMessage = null)
        }
    }

    private fun playCardForHuman(card: Card) {
        val state = _gameState.value
        executePlay(state, state.humanPlayerIndex, card)
    }

    private fun triggerAiPlayIfNeeded() {
        val state = _gameState.value
        if (state.phase == GamePhase.PLAYING && !state.isHumanTurn) {
            viewModelScope.launch {
                delay(550)
                aiPlayStep()
            }
        }
    }

    private fun aiPlayStep() {
        val state = _gameState.value
        if (state.phase != GamePhase.PLAYING || state.isHumanTurn) return
        val p       = state.currentPlayerIndex
        val player  = state.players[p]
        val playable = SpadesGameLogic.getPlayableCards(
            hand         = player.hand,
            tablePlayed  = state.tablePlayed,
            leadSuit     = state.leadSuit,
            spadesBroken = state.spadesBroken
        )
        if (playable.isEmpty()) return
        val card = SpadesGameLogic.chooseAiCard(
            playerId    = p,
            team        = player.team,
            playable    = playable,
            tablePlayed = state.tablePlayed,
            leadSuit    = state.leadSuit,
            players     = state.players
        )
        executePlay(state, p, card)
    }

    private fun executePlay(state: GameState, playerIndex: Int, card: Card) {
        val player      = state.players[playerIndex]
        val newHand     = player.hand.filter { it != card }
        val newPlayed   = state.tablePlayed + PlayedCard(player.id, player.position, card)
        val newLeadSuit = if (state.tablePlayed.isEmpty()) card.suit else state.leadSuit
        val newSpadesBroken = state.spadesBroken || card.suit == Suit.SPADES
        val updatedPlayers  = state.players.mapIndexed { i, p ->
            if (i == playerIndex) p.copy(hand = newHand) else p
        }

        if (newPlayed.size == 4) {
            // Trick complete - update state then resolve after delay
            _gameState.value = state.copy(
                players         = updatedPlayers,
                tablePlayed     = newPlayed,
                leadSuit        = newLeadSuit,
                spadesBroken    = newSpadesBroken,
                selectedCard    = null,
                toastMessage    = null
            )
            viewModelScope.launch {
                delay(1000)
                resolveTrick()
            }
        } else {
            val next = (playerIndex + 1) % 4
            _gameState.value = state.copy(
                players          = updatedPlayers,
                tablePlayed      = newPlayed,
                leadSuit         = newLeadSuit,
                spadesBroken     = newSpadesBroken,
                currentPlayerIndex = next,
                selectedCard     = null,
                toastMessage     = null
            )
            triggerAiPlayIfNeeded()
        }
    }

    private fun resolveTrick() {
        val state   = _gameState.value
        val winning = SpadesGameLogic.getWinningPlay(state.tablePlayed, state.leadSuit) ?: return
        val winnerPlayerIndex = state.players.indexOfFirst { it.id == winning.playerId }
        val winner  = state.players[winnerPlayerIndex]

        val updatedPlayers = state.players.mapIndexed { i, p ->
            if (i == winnerPlayerIndex) p.copy(tricksWon = p.tricksWon + 1) else p
        }

        val toast = "${winner.name} wins the trick with ${winning.card.rank.displayName}${winning.card.suit.symbol}"

        val isRoundOver = updatedPlayers.all { it.hand.isEmpty() }

        if (isRoundOver) {
            val (newNs, newEw) = SpadesGameLogic.scoreRound(updatedPlayers, state.nsScore, state.ewScore)
            val nsTricks = updatedPlayers.filter { it.team == Team.NS }.sumOf { it.tricksWon }
            val ewTricks = updatedPlayers.filter { it.team == Team.EW }.sumOf { it.tricksWon }
            val nsBid    = updatedPlayers.filter { it.team == Team.NS }.sumOf { it.bid ?: 0 }
            val ewBid    = updatedPlayers.filter { it.team == Team.EW }.sumOf { it.bid ?: 0 }
            val nsEarned = newNs.totalScore - state.nsScore.totalScore
            val ewEarned = newEw.totalScore - state.ewScore.totalScore

            val winner500 = when {
                newNs.totalScore >= 100 -> Team.NS
                newEw.totalScore >= 100 -> Team.EW
                else                   -> null
            }
            _gameState.value = state.copy(
                players        = updatedPlayers,
                tablePlayed    = emptyList(),
                leadSuit       = null,
                nsScore        = newNs,
                ewScore        = newEw,
                phase          = if (winner500 != null) GamePhase.GAME_OVER else GamePhase.ROUND_OVER,
                toastMessage   = toast,
                winnerTeam     = winner500,
                roundResult    = RoundResult(nsBid, nsTricks, nsEarned, ewBid, ewTricks, ewEarned)
            )
        } else {
            _gameState.value = state.copy(
                players            = updatedPlayers,
                tablePlayed        = emptyList(),
                leadSuit           = null,
                trickLeaderIndex   = winnerPlayerIndex,
                currentPlayerIndex = winnerPlayerIndex,
                toastMessage       = toast
            )
            triggerAiPlayIfNeeded()
        }
    }

    // ─── PUBLIC HELPERS ─────────────────────────────────────────────────────

    fun dismissToast() {
        _gameState.value = _gameState.value.copy(toastMessage = null)
    }

    fun proceedToNextRound() {
        nextRound()
    }
}
