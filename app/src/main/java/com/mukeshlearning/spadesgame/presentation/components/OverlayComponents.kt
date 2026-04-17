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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.mukeshlearning.spadesgame.domain.model.*
import com.mukeshlearning.spadesgame.ui.theme.*

@Composable
fun RoundResultOverlay(
    result: RoundResult,
    nsTotal: Int,
    ewTotal: Int,
    onNext: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.75f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .width(320.dp)
                .background(Color(0xFF0E2016), RoundedCornerShape(16.dp))
                .border(2.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(16.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Round Complete", color = GoldAccent, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(18.dp))

            ResultTeamRow(
                teamName   = "NS (You + North)",
                bid        = result.nsBid,
                tricks     = result.nsTricks,
                earned     = result.nsEarned,
                totalScore = nsTotal,
                color      = NSGreen
            )
            Spacer(Modifier.height(12.dp))
            ResultTeamRow(
                teamName   = "EW (East + West)",
                bid        = result.ewBid,
                tricks     = result.ewTricks,
                earned     = result.ewEarned,
                totalScore = ewTotal,
                color      = EWBlue
            )

            Spacer(Modifier.height(20.dp))
            Button(
                onClick = onNext,
                colors  = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                shape   = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Deal Next Round  ▶", color = Color.White, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun ResultTeamRow(
    teamName: String,
    bid: Int,
    tricks: Int,
    earned: Int,
    totalScore: Int,
    color: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.05f), RoundedCornerShape(10.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Column {
            Text(teamName, color = color, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Text("Bid $bid, made $tricks tricks", color = Color.White.copy(alpha = 0.5f), fontSize = 11.sp)
        }
        Column(horizontalAlignment = Alignment.End) {
            val earnedStr = if (earned >= 0) "+$earned" else "$earned"
            Text(earnedStr, color = if (earned >= 0) NSGreen else Color(0xFFFF7070), fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text("Total: $totalScore", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
        }
    }
}

// ─── Game Over overlay ───────────────────────────────────────────────────────
@Composable
fun GameOverOverlay(
    winnerTeam: Team,
    nsTotal: Int,
    ewTotal: Int,
    onRestart: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.92f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Text("🏆", fontSize = 56.sp)
            Spacer(Modifier.height(8.dp))
            val winName = if (winnerTeam == Team.NS) "NS Team (You + North)" else "EW Team"
            Text(
                text       = "$winName Wins!",
                color      = GoldAccent,
                fontSize   = 28.sp,
                fontWeight = FontWeight.Bold,
                textAlign  = TextAlign.Center
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text  = "Final Score\nNS: $nsTotal   EW: $ewTotal",
                color = Color.White.copy(alpha = 0.65f),
                fontSize = 15.sp,
                textAlign = TextAlign.Center,
                lineHeight = 22.sp
            )
            Spacer(Modifier.height(28.dp))
            Button(
                onClick = onRestart,
                colors  = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                shape   = RoundedCornerShape(12.dp),
                modifier = Modifier.height(52.dp).width(200.dp)
            ) {
                Text("Play Again", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}
