package com.appguardx.ui.components

import android.graphics.drawable.Drawable
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.appguardx.data.model.AppInfo
import com.appguardx.data.model.RiskLevel
import com.appguardx.ui.theme.*

/**
 * Card for each app in the list — shows icon, name, package, risk badge, permission count.
 */
@Composable
fun AppCard(
    app: AppInfo,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = when (app.riskLevel) {
        RiskLevel.HIGH -> HighRiskRed.copy(alpha = 0.4f)
        RiskLevel.MEDIUM -> MediumRiskAmber.copy(alpha = 0.4f)
        RiskLevel.LOW -> LowRiskGreen.copy(alpha = 0.2f)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // App Icon
            AppIconView(drawable = app.icon, appName = app.appName, riskLevel = app.riskLevel)

            // App Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = app.appName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = app.packageName,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextHint,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(6.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RiskBadge(riskLevel = app.riskLevel, compact = true)
                    Text(
                        text = "${app.requestedPermissions.size} perms",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                    if (app.suspiciousPermissions.isNotEmpty()) {
                        Text(
                            text = "⚠️ ${app.suspiciousPermissions.size} suspicious",
                            fontSize = 11.sp,
                            color = MediumRiskAmber,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Chevron
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "View details",
                tint = TextHint,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun AppIconView(
    drawable: Drawable?,
    appName: String,
    riskLevel: RiskLevel,
    modifier: Modifier = Modifier,
    size: Int = 52
) {
    val ringColor = when (riskLevel) {
        RiskLevel.HIGH -> HighRiskRed
        RiskLevel.MEDIUM -> MediumRiskAmber
        RiskLevel.LOW -> LowRiskGreen
    }

    Box(
        modifier = modifier
            .size(size.dp)
            .background(ringColor.copy(alpha = 0.15f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (drawable != null) {
            AsyncImage(
                model = drawable,
                contentDescription = appName,
                modifier = Modifier
                    .size((size - 8).dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Text(
                text = appName.take(1).uppercase(),
                color = ringColor,
                fontSize = (size / 2.5).sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
