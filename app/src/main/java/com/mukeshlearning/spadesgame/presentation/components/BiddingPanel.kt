package com.mukeshlearning.spadesgame.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.mukeshlearning.spadesgame.ui.theme.*

@Composable
fun BiddingPanel(
    cardCount: Int,
    onBid: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        // Scrim
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
        )

        // Panel
        Column(
            modifier = Modifier
                .width(320.dp)
                .background(Color(0xFF0E2016), RoundedCornerShape(16.dp))
                .border(2.dp, GoldAccent.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text       = "♠ Place Your Bid",
                color      = GoldAccent,
                fontSize   = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text      = "You have $cardCount cards. How many tricks will you win?",
                color     = Color.White.copy(alpha = 0.6f),
                fontSize  = 12.sp,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(20.dp))

            // Bid buttons 1–9
            val bids = (1..9).toList()
            for (row in bids.chunked(3)) {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    row.forEach { bid ->
                        BidButton(label = "$bid", onClick = { onBid(bid) })
                    }
                }
                Spacer(Modifier.height(10.dp))
            }

            // Nil bid (risky)
            BidButton(
                label       = "Nil  (risky)",
                onClick     = { onBid(0) },
                isNil       = true,
                modifier    = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun BidButton(
    label: String,
    onClick: () -> Unit,
    isNil: Boolean = false,
    modifier: Modifier = Modifier
) {
    val bg     = if (isNil) Color(0x44880000) else Color.White.copy(alpha = 0.07f)
    val border = if (isNil) Color(0x88FF4444) else Color.White.copy(alpha = 0.18f)
    val color  = if (isNil) Color(0xFFFF8080) else Color.White

    Box(
        modifier = modifier
            .then(if (!isNil) Modifier.size(84.dp, 52.dp) else Modifier.height(52.dp))
            .background(bg, RoundedCornerShape(10.dp))
            .border(1.dp, border, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(text = label, color = color, fontSize = if (isNil) 13.sp else 18.sp, fontWeight = FontWeight.SemiBold)
    }
}
