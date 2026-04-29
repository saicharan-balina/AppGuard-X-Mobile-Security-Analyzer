package com.appguardx.data.model

import android.graphics.drawable.Drawable

/**
 * Represents a scanned installed application with full risk analysis data.
 */
data class AppInfo(
    val appName: String,
    val packageName: String,
    val icon: Drawable?,
    val isSystemApp: Boolean,
    val requestedPermissions: List<PermissionInfo>,
    val grantedPermissions: List<PermissionInfo>,
    val riskLevel: RiskLevel,
    val riskScore: Int,                          // 0-100 individual app risk score
    val riskReason: String,
    val riskyPermissions: List<PermissionInfo>,
    val unnecessaryPermissions: List<String>,    // Permissions flagged as unnecessary for this app type
    val suspiciousPermissions: List<String>,     // Permissions flagged as suspicious
    val appCategory: AppCategory,
    val installTime: Long = 0L,
    val lastUpdateTime: Long = 0L
)

/**
 * App category used for permission expectation prediction.
 */
enum class AppCategory(val displayName: String) {
    MESSAGING("Messaging"),
    CAMERA("Camera / Photography"),
    CALCULATOR("Calculator / Math"),
    BROWSER("Browser"),
    SOCIAL_MEDIA("Social Media"),
    MAPS("Maps / Navigation"),
    MUSIC("Music / Audio"),
    GAME("Game"),
    FITNESS("Fitness / Health"),
    FINANCE("Finance / Banking"),
    UTILITY("Utility"),
    PRODUCTIVITY("Productivity"),
    SYSTEM("System"),
    UNKNOWN("Unknown")
}
