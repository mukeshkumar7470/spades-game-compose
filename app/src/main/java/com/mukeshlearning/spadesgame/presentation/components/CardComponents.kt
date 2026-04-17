package com.mukeshlearning.spadesgame.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.unit.*
import com.mukeshlearning.spadesgame.domain.model.Card
import com.mukeshlearning.spadesgame.ui.theme.*

@Composable
fun PlayingCard(
    card: Card,
    modifier: Modifier = Modifier,
    isPlayable: Boolean = false,
    isSelected: Boolean = false,
    onClick: (() -> Unit)? = null,
    faceDown: Boolean = false
) {
    val hoverOffset by animateDpAsState(
        targetValue  = if (isSelected) (-18).dp else if (isPlayable) 0.dp else 0.dp,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 400f),
        label        = "cardLift"
    )
    val scale by animateFloatAsState(
        targetValue  = if (isSelected) 1.06f else 1f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 500f),
        label        = "cardScale"
    )

    val borderColor = when {
        isSelected -> CardBorderSelected
        isPlayable -> CardBorderPlayable
        else       -> CardBorderNormal
    }
    val borderWidth = if (isSelected || isPlayable) 2.dp else 0.5.dp
    val elevation   = if (isSelected) 12.dp else if (isPlayable) 6.dp else 3.dp

    Box(
        modifier = modifier
            .offset(y = hoverOffset)
            .scale(scale)
            .shadow(elevation, RoundedCornerShape(6.dp))
            .then(
                if (onClick != null && isPlayable)
                    Modifier.clickable(onClick = onClick)
                else Modifier
            )
    ) {
        if (faceDown) {
            CardBack()
        } else {
            CardFace(card = card, borderColor = borderColor, borderWidth = borderWidth)
        }
    }
}

@Composable
private fun CardFace(card: Card, borderColor: Color, borderWidth: Dp) {
    val textColor = if (card.isRed) CardRed else CardBlack

    Box(
        modifier = Modifier
            .width(58.dp)
            .height(84.dp)
            .background(CardWhite, RoundedCornerShape(6.dp))
            .border(borderWidth, borderColor, RoundedCornerShape(6.dp))
    ) {
        // Top-left corner
        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 4.dp, top = 3.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text       = card.displayRank,
                color      = textColor,
                fontSize   = 13.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 13.sp
            )
            Text(
                text     = card.suitSymbol,
                color    = textColor,
                fontSize = 11.sp,
                lineHeight = 11.sp
            )
        }

        // Center suit
        Text(
            text     = card.suitSymbol,
            color    = textColor,
            fontSize = 26.sp,
            modifier = Modifier.align(Alignment.Center)
        )

        // Bottom-right (rotated)
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 4.dp, bottom = 3.dp)
                .rotate(180f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text       = card.displayRank,
                color      = textColor,
                fontSize   = 13.sp,
                fontWeight = FontWeight.Bold,
                lineHeight = 13.sp
            )
            Text(
                text     = card.suitSymbol,
                color    = textColor,
                fontSize = 11.sp,
                lineHeight = 11.sp
            )
        }
    }
}

@Composable
fun CardBack(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .width(58.dp)
            .height(84.dp)
            .background(
                brush  = Brush.linearGradient(listOf(CardBack1, CardBack2, CardBack1)),
                shape  = RoundedCornerShape(6.dp)
            )
            .border(0.5.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(6.dp)),
        contentAlignment = Alignment.Center
    ) {
        // Inner border decoration
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(5.dp)
                .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(3.dp))
        )
        Text(
            text     = "♠",
            color    = Color.White.copy(alpha = 0.25f),
            fontSize = 28.sp
        )
    }
}

@Composable
fun SmallCardBack(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .width(38.dp)
            .height(54.dp)
            .background(
                brush = Brush.linearGradient(listOf(CardBack1, CardBack2, CardBack1)),
                shape = RoundedCornerShape(4.dp)
            )
            .border(0.5.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(4.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "♠", color = Color.White.copy(alpha = 0.2f), fontSize = 16.sp)
    }
}
