package com.appguardx.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appguardx.data.model.PermissionInfo
import com.appguardx.data.model.RiskLevel
import com.appguardx.ui.theme.*

/**
 * Chip showing a permission name with its risk-level color.
 */
@Composable
fun PermissionChip(
    permission: PermissionInfo,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = when (permission.riskLevel) {
        RiskLevel.HIGH -> Pair(HighRiskRedLight, HighRiskRed)
        RiskLevel.MEDIUM -> Pair(MediumRiskAmberLight, MediumRiskAmber)
        RiskLevel.LOW -> Pair(LowRiskGreenLight, LowRiskGreen)
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(6.dp),
        color = bgColor
    ) {
        Text(
            text = permission.simpleName,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            color = textColor,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/**
 * Chip for flagged/suspicious permissions (always styled in red).
 */
@Composable
fun SuspiciousChip(
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(6.dp),
        color = HighRiskRedLight
    ) {
        Text(
            text = "⚠️ $label",
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            color = HighRiskRed,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/**
 * Chip for unnecessary permissions (yellow warning).
 */
@Composable
fun UnnecessaryChip(
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(6.dp),
        color = MediumRiskAmberLight
    ) {
        Text(
            text = "❗ $label",
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            color = MediumRiskAmber,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}
