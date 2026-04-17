package com.mukeshlearning.spadesgame.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.mukeshlearning.spadesgame.domain.model.Card
import com.mukeshlearning.spadesgame.domain.model.Player

// ─── South (Human) hand ──────────────────────────────────────────────────────

@Composable
fun SouthHand(
    player: Player,
    playableCards: List<Card>,
    selectedCard: Card?,
    isCurrentPlayer: Boolean,
    onCardClick: (Card) -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clipToBounds()
    ) {

        val totalCards = player.hand.size
        val cardWidth = 60.dp
        val availableWidth = maxWidth

        // 👉 Calculate spacing dynamically
        val rawSpacing = if (totalCards > 1) {
            (availableWidth - cardWidth) / (totalCards - 1)
        } else 0.dp

        // 👉 Limit spacing (for overlap feel)
        val spacing = rawSpacing.coerceIn(12.dp, 28.dp)


        player.hand.forEachIndexed { index, card ->
            val isPlayable = isCurrentPlayer && playableCards.any { it == card }
            val isSelected = selectedCard == card
            // Fan overlap
            val zIndex = if (isSelected) 50f else index.toFloat()
            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clipToBounds()
            ) {

                val totalCards = player.hand.size
                val cardWidth = 60.dp
                val availableWidth = maxWidth

                val rawSpacing = if (totalCards > 1) {
                    (availableWidth - cardWidth) / (totalCards - 1)
                } else 0.dp

                val spacing = rawSpacing.coerceIn(12.dp, 28.dp)

                // 👉 TOTAL width occupied by cards
                val totalWidth = if (totalCards > 0) {
                    cardWidth + spacing * (totalCards - 1)
                } else 0.dp

                // 👉 START OFFSET to center
                val startX = (availableWidth - totalWidth) / 2

                player.hand.forEachIndexed { index, card ->

                    val isPlayable = isCurrentPlayer && playableCards.contains(card)
                    val isSelected = selectedCard == card

                    val zIndex = if (isSelected) 100f else index.toFloat()

                    PlayingCard(
                        card = card,
                        isPlayable = isPlayable,
                        isSelected = isSelected,
                        modifier = Modifier
                            .offset(
                                x = startX + spacing * index,
                                y = if (isSelected) (-18).dp else 0.dp
                            )
                            .zIndex(zIndex)
                            .graphicsLayer {
                                rotationZ = (index - totalCards / 2f) * 2.5f
                            },
                        onClick = if (isPlayable) ({ onCardClick(card) }) else null
                    )
                }
            }        }
    }
}

// ─── North (AI) hand ─────────────────────────────────────────────────────────
@Composable
fun NorthHand(player: Player) {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.Top
        ) {
            player.hand.forEachIndexed { index, _ ->
                Box(modifier = Modifier.offset(x = (index * (-6)).dp)) {
                    SmallCardBack()
                }
            }
        }
    }
}

// ─── West (AI) hand ──────────────────────────────────────────────────────────
@Composable
fun WestHand(player: Player) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.End
    ) {
        player.hand.forEachIndexed { index, _ ->
            Box(modifier = Modifier.offset(y = (index * (-14)).dp)) {
                SmallCardBack()
            }
        }
    }
}

// ─── East (AI) hand ──────────────────────────────────────────────────────────
@Composable
fun EastHand(player: Player) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.Start
    ) {
        player.hand.forEachIndexed { index, _ ->
            Box(modifier = Modifier.offset(y = (index * (-14)).dp)) {
                SmallCardBack()
            }
        }
    }
}
