package com.appguardx.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appguardx.data.model.RiskLevel
import com.appguardx.data.model.ScanResult
import com.appguardx.ui.theme.*
import com.appguardx.viewmodel.AppGuardUiState

/**
 * Home screen: Title, scan button, system app toggle, progress, and dashboard summary.
 */
@Composable
fun HomeScreen(
    uiState: AppGuardUiState,
    onScanClick: () -> Unit,
    onSystemAppsToggle: (Boolean) -> Unit,
    onNavigateToList: () -> Unit
) {
    val scrollState = rememberScrollState()

    // Animated rotating gradient ring
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val rotationAnim by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(4000, easing = LinearEasing)),
        label = "rotate"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Background glow blobs
        Box(
            Modifier
                .size(300.dp)
                .offset((-80).dp, (-60).dp)
                .background(
                    Brush.radialGradient(listOf(PrimaryPurple.copy(0.15f), Color.Transparent)),
                    CircleShape
                )
        )
        Box(
            Modifier
                .size(250.dp)
                .align(Alignment.BottomEnd)
                .offset(60.dp, 60.dp)
                .background(
                    Brush.radialGradient(listOf(AccentCyan.copy(0.10f), Color.Transparent)),
                    CircleShape
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // ── App Title ──
            Spacer(Modifier.height(16.dp))
            Text(
                text = "🛡️",
                fontSize = 56.sp
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "AppGuard X",
                style = MaterialTheme.typography.displayLarge,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary
            )
            Text(
                text = "Mobile Security Analyzer",
                style = MaterialTheme.typography.bodyLarge,
                color = PrimaryPurpleLight,
                letterSpacing = 2.sp
            )

            Spacer(Modifier.height(36.dp))

            // ── System Apps Toggle ──
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCard)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Include System Apps",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                        Text(
                            text = "Scan pre-installed system apps too",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                    Switch(
                        checked = uiState.includeSystemApps,
                        onCheckedChange = onSystemAppsToggle,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = PrimaryPurple
                        )
                    )
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Scan Button ──
            Button(
                onClick = {
                    if (!uiState.isScanning) onScanClick()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryPurple,
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
            ) {
                if (uiState.isScanning) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = Color.White,
                        strokeWidth = 2.5.dp
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = "Scanning... ${(uiState.scanProgress * 100).toInt()}%",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                } else {
                    Icon(Icons.Default.Search, contentDescription = null)
                    Spacer(Modifier.width(10.dp))
                    Text(
                        text = if (uiState.scanResult != null) "Rescan Device" else "Start Scan",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                }
            }

            // ── Progress bar ──
            if (uiState.isScanning) {
                Spacer(Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { uiState.scanProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = AccentCyan,
                    trackColor = DarkCardElevated,
                    strokeCap = StrokeCap.Round
                )
            }

            // ── Error ──
            if (uiState.error != null) {
                Spacer(Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = HighRiskRedLight)
                ) {
                    Text(
                        text = "⚠️ ${uiState.error}",
                        modifier = Modifier.padding(14.dp),
                        color = HighRiskRed,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            // ── Dashboard Summary (shown after scan) ──
            val result = uiState.scanResult
            if (result != null && !uiState.isScanning) {
                Spacer(Modifier.height(28.dp))
                DashboardSummary(result = result, onViewAll = onNavigateToList)
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
fun DashboardSummary(result: ScanResult, onViewAll: () -> Unit) {
    // Security Score
    val scoreColor = when {
        result.securityScore >= 70 -> ScoreGood
        result.securityScore >= 40 -> ScoreWarning
        else -> ScoreDanger
    }
    val scoreLabel = when {
        result.securityScore >= 70 -> "Good"
        result.securityScore >= 40 -> "At Risk"
        else -> "Dangerous"
    }

    Text(
        text = "📊 Scan Summary",
        style = MaterialTheme.typography.headlineMedium,
        color = TextPrimary,
        modifier = Modifier.fillMaxWidth()
    )
    Text(
        text = result.scanTimestampFormatted,
        style = MaterialTheme.typography.bodyMedium,
        color = TextSecondary,
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(Modifier.height(16.dp))

    // Score card
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(listOf(DarkCardElevated, DarkCard))
                )
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Security Score", color = TextSecondary, fontSize = 13.sp)
            Spacer(Modifier.height(8.dp))
            Text(
                text = "${result.securityScore}",
                fontSize = 64.sp,
                fontWeight = FontWeight.ExtraBold,
                color = scoreColor
            )
            Text(
                text = scoreLabel,
                color = scoreColor,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { result.securityScore / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = scoreColor,
                trackColor = DividerColor,
                strokeCap = StrokeCap.Round
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "Total apps scanned: ${result.totalApps}",
                color = TextHint,
                fontSize = 12.sp
            )
        }
    }

    Spacer(Modifier.height(16.dp))

    // Risk breakdown row
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        RiskStatCard(
            label = "High Risk",
            count = result.highRiskCount,
            color = HighRiskRed,
            bgColor = HighRiskRedLight,
            emoji = "🔴",
            modifier = Modifier.weight(1f)
        )
        RiskStatCard(
            label = "Medium",
            count = result.mediumRiskCount,
            color = MediumRiskAmber,
            bgColor = MediumRiskAmberLight,
            emoji = "🟡",
            modifier = Modifier.weight(1f)
        )
        RiskStatCard(
            label = "Low Risk",
            count = result.lowRiskCount,
            color = LowRiskGreen,
            bgColor = LowRiskGreenLight,
            emoji = "🟢",
            modifier = Modifier.weight(1f)
        )
    }

    Spacer(Modifier.height(20.dp))

    // View All button
    OutlinedButton(
        onClick = onViewAll,
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryPurpleLight),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, PrimaryPurple)
    ) {
        Icon(Icons.Default.List, contentDescription = null)
        Spacer(Modifier.width(8.dp))
        Text("View All Apps", fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun RiskStatCard(
    label: String,
    count: Int,
    color: Color,
    bgColor: Color,
    emoji: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = emoji, fontSize = 22.sp)
            Spacer(Modifier.height(4.dp))
            Text(
                text = count.toString(),
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
            Text(
                text = label,
                fontSize = 11.sp,
                color = color.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
        }
    }
}
