package com.mukeshlearning.spadesgame.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import com.mukeshlearning.spadesgame.domain.model.*
import com.mukeshlearning.spadesgame.ui.theme.*

@Composable
fun PlayerLabel(
    player: Player,
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    val bg = when {
        isActive && player.isHuman -> HumanHighlight
        isActive                   -> ActiveHighlight
        else                       -> Color.Black.copy(alpha = 0.35f)
    }
    val textColor = when {
        player.isHuman -> NSGreen
        isActive       -> GoldAccent
        else           -> Color.White.copy(alpha = 0.65f)
    }
    val borderColor = when {
        isActive && player.isHuman -> NSGreen.copy(alpha = 0.6f)
        isActive                   -> GoldAccent.copy(alpha = 0.5f)
        else                       -> Color.Transparent
    }

    Column(
        modifier          = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .background(bg, RoundedCornerShape(12.dp))
                .border(1.dp, borderColor, RoundedCornerShape(12.dp))
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                text       = player.name.uppercase(),
                color      = textColor,
                fontSize   = 10.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 0.08.em
            )
        }
        Spacer(Modifier.height(2.dp))
        Text(
            text      = buildPlayerSubtitle(player),
            color     = Color.White.copy(alpha = 0.45f),
            fontSize  = 9.sp,
            lineHeight = 11.sp
        )
    }
}

private fun buildPlayerSubtitle(player: Player): String {
    val bidStr    = player.bid?.let { if (it == 0) "Nil" else "$it" } ?: "?"
    return "Bid: $bidStr  Tricks: ${player.tricksWon}"
}

// ─── Table Center (played cards) ─────────────────────────────────────────────
@Composable
fun TableCenter(
    state: GameState,
    modifier: Modifier = Modifier
) {
    Box(
        modifier          = modifier.size(220.dp, 180.dp),
        contentAlignment  = Alignment.Center
    ) {
        // Oval table border hint
        Box(
            modifier = Modifier
                .size(200.dp, 160.dp)
                .border(
                    width = 2.dp,
                    color = WoodBrown.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(50)
                )
        )

        val positionMap = mapOf(
            PlayerPosition.SOUTH to Alignment.BottomCenter,
            PlayerPosition.NORTH to Alignment.TopCenter,
            PlayerPosition.WEST  to Alignment.CenterStart,
            PlayerPosition.EAST  to Alignment.CenterEnd
        )
        val offsetMap = mapOf(
            PlayerPosition.SOUTH to Pair(0.dp, (-8).dp),
            PlayerPosition.NORTH to Pair(0.dp, 8.dp),
            PlayerPosition.WEST  to Pair(8.dp, 0.dp),
            PlayerPosition.EAST  to Pair((-8).dp, 0.dp)
        )

        state.tablePlayed.forEach { played ->
            val align  = positionMap[played.position] ?: Alignment.Center
            val offset = offsetMap[played.position] ?: Pair(0.dp, 0.dp)
            Box(
                modifier = Modifier
                    .align(align)
                    .offset(x = offset.first, y = offset.second)
            ) {
                PlayingCard(card = played.card, isPlayable = false)
            }
        }
    }
}

// ─── Scoreboard row ──────────────────────────────────────────────────────────
@Composable
fun ScoreboardRow(
    nsScore: TeamScore,
    ewScore: TeamScore,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.Black.copy(alpha = 0.55f))
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        TeamScoreItem(score = nsScore, color = NSGreen,  label = "NS (You+P2)")
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("VS", color = Color.White.copy(alpha = 0.25f), fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text("First to 500", color = Color.White.copy(alpha = 0.25f), fontSize = 8.sp)
        }
        TeamScoreItem(score = ewScore, color = EWBlue, label = "EW (P1+P3)", alignEnd = true)
    }
}

@Composable
private fun TeamScoreItem(
    score: TeamScore,
    color: Color,
    label: String,
    alignEnd: Boolean = false
) {
    Column(
        horizontalAlignment = if (alignEnd) Alignment.End else Alignment.Start
    ) {
        Text(label, color = Color.White.copy(alpha = 0.4f), fontSize = 9.sp, letterSpacing = 0.06.em)
        Text(
            text       = "${score.totalScore}",
            color      = color,
            fontSize   = 28.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text  = "Bid: ${score.currentBid ?: "—"} | Bags: ${score.bags}",
            color = Color.White.copy(alpha = 0.35f),
            fontSize = 9.sp
        )
    }
}

// ─── Spades broken badge ─────────────────────────────────────────────────────
@Composable
fun SpadesBadge(broken: Boolean, modifier: Modifier = Modifier) {
    val bg    = if (broken) Color(0x44FFFFFF) else Color(0x22000000)
    val color = if (broken) Color.White else Color.White.copy(alpha = 0.3f)
    Box(
        modifier = modifier
            .background(bg, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text      = if (broken) "♠ Spades broken" else "♠ Spades not broken",
            color     = color,
            fontSize  = 9.sp,
            fontWeight = if (broken) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

// ─── Toast ───────────────────────────────────────────────────────────────────
@Composable
fun GameToast(message: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(ToastBg, RoundedCornerShape(20.dp))
            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(20.dp))
            .padding(horizontal = 18.dp, vertical = 8.dp)
    ) {
        Text(text = message, color = Color.White, fontSize = 12.sp)
    }
}
