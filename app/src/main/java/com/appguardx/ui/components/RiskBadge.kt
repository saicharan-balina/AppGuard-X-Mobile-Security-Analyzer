package com.appguardx.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appguardx.data.model.RiskLevel
import com.appguardx.ui.theme.*

/**
 * Badge showing risk level (HIGH / MEDIUM / LOW) with color coding.
 */
@Composable
fun RiskBadge(
    riskLevel: RiskLevel,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    val (bgColor, textColor, label, emoji) = when (riskLevel) {
        RiskLevel.HIGH -> BadgeStyle(HighRiskRedLight, HighRiskRed, "HIGH", "🔴")
        RiskLevel.MEDIUM -> BadgeStyle(MediumRiskAmberLight, MediumRiskAmber, "MEDIUM", "🟡")
        RiskLevel.LOW -> BadgeStyle(LowRiskGreenLight, LowRiskGreen, "LOW", "🟢")
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(if (compact) 6.dp else 8.dp),
        color = bgColor
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = if (compact) 8.dp else 12.dp,
                vertical = if (compact) 3.dp else 5.dp
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            if (!compact) {
                Text(
                    text = emoji,
                    fontSize = if (compact) 10.sp else 12.sp
                )
            }
            Text(
                text = label,
                color = textColor,
                fontSize = if (compact) 9.sp else 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
            )
        }
    }
}

data class BadgeStyle(
    val bgColor: Color,
    val textColor: Color,
    val label: String,
    val emoji: String
)
