package com.appguardx.data.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Represents the overall result of a full device scan.
 */
data class ScanResult(
    val apps: List<AppInfo>,
    val scanTimestamp: Long = System.currentTimeMillis(),
    val totalApps: Int = apps.size,
    val highRiskCount: Int = apps.count { it.riskLevel == RiskLevel.HIGH },
    val mediumRiskCount: Int = apps.count { it.riskLevel == RiskLevel.MEDIUM },
    val lowRiskCount: Int = apps.count { it.riskLevel == RiskLevel.LOW },
    val securityScore: Int = calculateSecurityScore(apps)
) {
    val scanTimestampFormatted: String
        get() {
            val sdf = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
            return sdf.format(Date(scanTimestamp))
        }
}

private fun calculateSecurityScore(apps: List<AppInfo>): Int {
    if (apps.isEmpty()) return 100
    var score = 100
    score -= apps.count { it.riskLevel == RiskLevel.HIGH } * 10
    score -= apps.count { it.riskLevel == RiskLevel.MEDIUM } * 5
    return score.coerceIn(0, 100)
}
