package com.appguardx.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appguardx.data.model.AppInfo
import com.appguardx.data.model.PermissionInfo
import com.appguardx.data.model.RiskLevel
import com.appguardx.ui.components.*
import com.appguardx.ui.theme.*

/**
 * App detail screen: Full permission analysis, risk score, suspicious flags, category insight.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AppDetailScreen(
    app: AppInfo,
    onBack: () -> Unit
) {
    val riskColor = when (app.riskLevel) {
        RiskLevel.HIGH -> HighRiskRed
        RiskLevel.MEDIUM -> MediumRiskAmber
        RiskLevel.LOW -> LowRiskGreen
    }
    val riskBgColor = when (app.riskLevel) {
        RiskLevel.HIGH -> HighRiskRedLight
        RiskLevel.MEDIUM -> MediumRiskAmberLight
        RiskLevel.LOW -> LowRiskGreenLight
    }

    val animatedScore by animateFloatAsState(
        targetValue = app.riskScore / 100f,
        animationSpec = tween(durationMillis = 1000),
        label = "score_anim"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        app.appName,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        containerColor = DarkBackground
    ) { padding ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {

            // ── App Header ──
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCard)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.verticalGradient(
                                    listOf(riskBgColor.copy(alpha = 0.3f), Color.Transparent)
                                )
                            )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            AppIconView(
                                drawable = app.icon,
                                appName = app.appName,
                                riskLevel = app.riskLevel,
                                size = 68
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = app.appName,
                                    style = MaterialTheme.typography.headlineMedium,
                                    color = TextPrimary
                                )
                                Text(
                                    text = app.packageName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary
                                )
                                Spacer(Modifier.height(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    RiskBadge(riskLevel = app.riskLevel)
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = PrimaryPurple.copy(0.2f)
                                    ) {
                                        Text(
                                            text = app.appCategory.displayName,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                            color = PrimaryPurpleLight,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ── Risk Score ──
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkCard)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Risk Score",
                            color = TextSecondary,
                            fontSize = 13.sp,
                            letterSpacing = 1.sp
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "${app.riskScore}",
                            fontSize = 52.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = riskColor
                        )
                        Text(
                            text = "out of 100",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                        Spacer(Modifier.height(10.dp))
                        LinearProgressIndicator(
                            progress = { animatedScore },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = riskColor,
                            trackColor = DividerColor,
                            strokeCap = StrokeCap.Round
                        )
                    }
                }
            }

            // ── Risk Reason ──
            item {
                InfoCard(
                    icon = Icons.Default.Info,
                    title = "Risk Assessment",
                    iconTint = riskColor
                ) {
                    Text(
                        text = app.riskReason,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }

            // ── Suspicious Permissions ──
            if (app.suspiciousPermissions.isNotEmpty()) {
                item {
                    InfoCard(
                        icon = Icons.Default.Warning,
                        title = "⚠️ Suspicious Permissions",
                        iconTint = HighRiskRed
                    ) {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            app.suspiciousPermissions.forEach { perm ->
                                SuspiciousChip(label = perm)
                            }
                        }
                    }
                }
            }

            // ── Unnecessary Permissions ──
            if (app.unnecessaryPermissions.isNotEmpty()) {
                item {
                    InfoCard(
                        icon = Icons.Default.Report,
                        title = "❗ Unnecessary Permissions",
                        iconTint = MediumRiskAmber
                    ) {
                        Text(
                            text = "These permissions are not typically needed for a ${app.appCategory.displayName} app.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                        Spacer(Modifier.height(8.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            app.unnecessaryPermissions.forEach { perm ->
                                UnnecessaryChip(label = perm)
                            }
                        }
                    }
                }
            }

            // ── All Permissions ──
            item {
                InfoCard(
                    icon = Icons.Default.Lock,
                    title = "All Permissions (${app.requestedPermissions.size})",
                    iconTint = PrimaryPurple
                ) {
                    if (app.requestedPermissions.isEmpty()) {
                        Text(
                            "No permissions requested",
                            color = TextSecondary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            app.requestedPermissions
                                .sortedByDescending { it.riskLevel.ordinal }
                                .forEach { perm ->
                                    PermissionRow(permission = perm)
                                }
                        }
                    }
                }
            }

            // ── App Meta ──
            item {
                InfoCard(
                    icon = Icons.Default.AppRegistration,
                    title = "App Details",
                    iconTint = AccentCyan
                ) {
                    DetailRow("Package", app.packageName)
                    DetailRow("Category", app.appCategory.displayName)
                    DetailRow("System App", if (app.isSystemApp) "Yes" else "No")
                    DetailRow("Permissions Requested", "${app.requestedPermissions.size}")
                    DetailRow("Permissions Granted", "${app.grantedPermissions.size}")
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
fun InfoCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    iconTint: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
            }
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
fun PermissionRow(permission: PermissionInfo) {
    val (indicatorColor, bg) = when (permission.riskLevel) {
        RiskLevel.HIGH -> Pair(HighRiskRed, HighRiskRedLight)
        RiskLevel.MEDIUM -> Pair(MediumRiskAmber, MediumRiskAmberLight)
        RiskLevel.LOW -> Pair(LowRiskGreen, LowRiskGreenLight)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(bg.copy(alpha = 0.5f))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Color dot indicator
        Box(
            Modifier
                .size(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(indicatorColor)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = permission.simpleName,
                color = TextPrimary,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp
            )
            Text(
                text = permission.description,
                color = TextSecondary,
                fontSize = 11.sp
            )
        }
        if (permission.isGranted) {
            Icon(
                Icons.Default.Check,
                contentDescription = "Granted",
                tint = LowRiskGreen,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, color = TextSecondary, fontSize = 13.sp)
        Text(
            text = value,
            color = TextPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.End,
            modifier = Modifier.widthIn(max = 220.dp)
        )
    }
    HorizontalDivider(color = DividerColor.copy(alpha = 0.5f), thickness = 0.5.dp)
}
