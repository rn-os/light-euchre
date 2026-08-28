package dev.ryanness.lighteuchre.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.thelightphone.sdk.ui.LightText
import com.thelightphone.sdk.ui.LightTextVariant
import com.thelightphone.sdk.ui.LightThemeTokens
import com.thelightphone.sdk.ui.gridUnitsAsDp
import com.thelightphone.sdk.ui.lightClickable
import dev.ryanness.lighteuchre.engine.Card

private val CARD_WIDTH_UNITS = 3.2f
private val CARD_HEIGHT_UNITS = 4.4f

@Composable
fun CardFace(
    card: Card,
    modifier: Modifier = Modifier,
    dim: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    val colors = LightThemeTokens.colors
    val borderColor = if (dim) colors.contentSecondary else colors.content
    val textColor = if (dim) colors.contentSecondary else null

    Box(
        modifier = modifier
            .size(CARD_WIDTH_UNITS.gridUnitsAsDp(), CARD_HEIGHT_UNITS.gridUnitsAsDp())
            .border(width = 1.dp, color = borderColor)
            .let { m -> if (onClick != null) m.lightClickable(onClick = onClick) else m }
            .padding(4.dp),
        contentAlignment = Alignment.Center,
    ) {
        LightText(
            text = card.label,
            variant = LightTextVariant.Subheading,
            color = textColor,
        )
    }
}

@Composable
fun CardBack(modifier: Modifier = Modifier) {
    val colors = LightThemeTokens.colors
    Box(
        modifier = modifier
            .size(CARD_WIDTH_UNITS.gridUnitsAsDp(), CARD_HEIGHT_UNITS.gridUnitsAsDp())
            .border(width = 1.dp, color = colors.contentSecondary),
    )
}

@Composable
fun PlayerHandRow(
    hand: List<Card>,
    modifier: Modifier = Modifier,
    legalPlays: Set<Card>? = null,
    onPlay: ((Card) -> Unit)? = null,
) {
    Row(modifier = modifier) {
        hand.forEach { card ->
            val isLegal = legalPlays == null || card in legalPlays
            CardFace(
                card = card,
                dim = !isLegal,
                onClick = if (onPlay != null && isLegal) ({ onPlay(card) }) else null,
                modifier = Modifier.padding(end = 6.dp),
            )
        }
    }
}
