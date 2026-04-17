package com.mukeshlearning.spadesgame.presentation

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.unit.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.mukeshlearning.spadesgame.domain.model.*
import com.mukeshlearning.spadesgame.domain.SpadesGameLogic
import com.mukeshlearning.spadesgame.presentation.components.*
import com.mukeshlearning.spadesgame.ui.theme.*

@Composable
fun GameScreen(viewModel: GameViewModel = viewModel()) {

    val state by viewModel.gameState.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(TableGreenLight, TableGreenDark, Color(0xFF071F10)),
                    radius = 900f
                )
            )
    ) {
        // ── Main game layout ───────────────────────────────────────────────
        Column(modifier = Modifier.fillMaxSize()) {

            // Info bar
            InfoBar(state = state, modifier = Modifier.fillMaxWidth())

            // Game table (fills remaining space)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                TableLayout(state = state, viewModel = viewModel)
            }

            // Scoreboard
            ScoreboardRow(
                nsScore = state.nsScore.copy(
                    currentBid    = state.teamBid(Team.NS),
                    currentTricks = state.teamTricks(Team.NS)
                ),
                ewScore = state.ewScore.copy(
                    currentBid    = state.teamBid(Team.EW),
                    currentTricks = state.teamTricks(Team.EW)
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        // ── Overlays ──────────────────────────────────────────────────────

        // Bidding
        if (state.phase == GamePhase.BIDDING && state.isHumanTurn) {
            BiddingPanel(
                cardCount = state.humanPlayer?.hand?.size ?: 13,
                onBid     = { viewModel.placeBid(it) }
            )
        }

        // Round result
        if (state.phase == GamePhase.ROUND_OVER && state.roundResult != null) {
            RoundResultOverlay(
                result   = state.roundResult!!,
                nsTotal  = state.nsScore.totalScore,
                ewTotal  = state.ewScore.totalScore,
                onNext   = { viewModel.proceedToNextRound() }
            )
        }

        // Game over
        if (state.phase == GamePhase.GAME_OVER && state.winnerTeam != null) {
            GameOverOverlay(
                winnerTeam = state.winnerTeam!!,
                nsTotal    = state.nsScore.totalScore,
                ewTotal    = state.ewScore.totalScore,
                onRestart  = { viewModel.startGame() }
            )
        }

        // Toast notification
        AnimatedVisibility(
            visible = state.toastMessage != null,
            enter   = fadeIn() + slideInVertically(),
            exit    = fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter).padding(top = 56.dp)
        ) {
            state.toastMessage?.let { msg ->
                GameToast(message = msg)
                LaunchedEffect(msg) {
                    kotlinx.coroutines.delay(2500)
                    viewModel.dismissToast()
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GameScreenPreview() {
    GameScreen()
}

// ─── Info bar (round, phase, lead suit) ──────────────────────────────────────
@Composable
private fun InfoBar(state: GameState, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .background(Color.Black.copy(alpha = 0.5f))
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        InfoChip("Round ${state.round}")
        InfoChip("Phase: ${state.phase.name.lowercase().replaceFirstChar { it.uppercase() }}")
        val lead = state.leadSuit?.let { "${it.symbol} Lead" } ?: "No lead"
        InfoChip(lead)
        SpadesBadge(broken = state.spadesBroken)
    }
}

@Preview
@Composable
fun InfoBarPreview() {
    InfoBar(state = GameState(round = 1, phase = GamePhase.PLAYING, spadesBroken = true))
}

@Composable
private fun InfoChip(text: String) {
    Text(
        text = text,
        color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.6f),
        fontSize = 10.sp,
        modifier = Modifier
            .background(
                androidx.compose.ui.graphics.Color.White.copy(alpha = 0.07f),
                RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 8.dp, vertical = 3.dp)
    )
}

@Preview
@Composable
fun InfoChipPreview() {
    InfoChip(text = "Round 1")
}

// ─── Table layout — four player zones + center ───────────────────────────────
@Composable
private fun TableLayout(state: GameState, viewModel: GameViewModel) {
    val humanPlayer  = state.humanPlayer
    val humanIndex   = state.humanPlayerIndex
    val northPlayer  = state.players.firstOrNull { it.position == PlayerPosition.NORTH }
    val eastPlayer   = state.players.firstOrNull { it.position == PlayerPosition.EAST }
    val westPlayer   = state.players.firstOrNull { it.position == PlayerPosition.WEST }

    val playableCards = if (state.phase == GamePhase.PLAYING && state.isHumanTurn) {
        SpadesGameLogic.getPlayableCards(
            hand         = humanPlayer?.hand ?: emptyList(),
            tablePlayed  = state.tablePlayed,
            leadSuit     = state.leadSuit,
            spadesBroken = state.spadesBroken
        )
    } else emptyList()

    Box(modifier = Modifier.fillMaxSize()) {

        // North zone
        northPlayer?.let { player ->
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                PlayerLabel(player = player, isActive = state.currentPlayerIndex == state.players.indexOf(player))
                Spacer(Modifier.height(4.dp))
                NorthHand(player = player)
            }
        }

        // West zone
        westPlayer?.let { player ->
            Row(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PlayerLabel(
                    player   = player,
                    isActive = state.currentPlayerIndex == state.players.indexOf(player),
                    modifier = Modifier.width(52.dp)
                )
                Spacer(Modifier.width(4.dp))
                WestHand(player = player)
            }
        }

        // East zone
        eastPlayer?.let { player ->
            Row(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                EastHand(player = player)
                Spacer(Modifier.width(4.dp))
                PlayerLabel(
                    player   = player,
                    isActive = state.currentPlayerIndex == state.players.indexOf(player),
                    modifier = Modifier.width(52.dp)
                )
            }
        }

        // Table center (played cards)
        TableCenter(
            state    = state,
            modifier = Modifier.align(Alignment.Center)
        )

        // South (human) zone
        humanPlayer?.let { player ->
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SouthHand(
                    player          = player,
                    playableCards   = playableCards,
                    selectedCard    = state.selectedCard,
                    isCurrentPlayer = state.phase == GamePhase.PLAYING && state.isHumanTurn,
                    onCardClick     = { viewModel.onCardSelected(it) }
                )
                Spacer(Modifier.height(4.dp))
                PlayerLabel(
                    player   = player,
                    isActive = state.phase == GamePhase.PLAYING && state.isHumanTurn
                )
            }
        }

        // "Tap again to play" hint
        if (state.selectedCard != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 120.dp)
                    .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text("Tap the card again to play it", color = GoldAccent, fontSize = 11.sp)
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF071F10)
@Composable
fun TableLayoutPreview() {
    val viewModel = GameViewModel()
    val state = viewModel.gameState.collectAsState().value
    TableLayout(state = state, viewModel = viewModel)
}
