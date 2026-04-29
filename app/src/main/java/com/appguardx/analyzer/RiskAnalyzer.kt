package com.appguardx.analyzer

import com.appguardx.data.model.AppCategory
import com.appguardx.data.model.AppInfo
import com.appguardx.data.model.PermissionInfo
import com.appguardx.data.model.RiskLevel

/**
 * Risk analysis engine that evaluates each app's permission profile
 * and produces risk classifications with detailed reasoning.
 */
object RiskAnalyzer {

    // ── Suspicious combos that dramatically raise risk ──
    private val SUSPICIOUS_COMBINATIONS = listOf(
        setOf("READ_SMS", "READ_CONTACTS") to "SMS + Contacts combo — potential data harvesting",
        setOf("RECORD_AUDIO", "ACCESS_FINE_LOCATION") to "Mic + Location — possible surveillance",
        setOf("CAMERA", "ACCESS_FINE_LOCATION") to "Camera + Location — privacy risk",
        setOf("READ_SMS", "INTERNET") to "SMS reading + Internet — possible data exfiltration",
        setOf("READ_CONTACTS", "INTERNET") to "Contacts + Internet — data leakage risk",
        setOf("READ_CALL_LOG", "INTERNET") to "Call logs + Internet — possible surveillance",
        setOf("REQUEST_INSTALL_PACKAGES", "INTERNET") to "Can silently download and install APKs",
        setOf("RECORD_AUDIO", "INTERNET") to "Microphone + Internet — potential eavesdropping"
    )

    // ── Expected permissions per app category ──
    private val EXPECTED_PERMISSIONS: Map<AppCategory, Set<String>> = mapOf(
        AppCategory.MESSAGING to setOf(
            "READ_SMS", "SEND_SMS", "RECEIVE_SMS",
            "READ_CONTACTS", "INTERNET", "RECORD_AUDIO",
            "CAMERA", "READ_EXTERNAL_STORAGE", "WRITE_EXTERNAL_STORAGE",
            "VIBRATE", "WAKE_LOCK", "FOREGROUND_SERVICE"
        ),
        AppCategory.CAMERA to setOf(
            "CAMERA", "READ_EXTERNAL_STORAGE", "WRITE_EXTERNAL_STORAGE",
            "RECORD_AUDIO", "ACCESS_FINE_LOCATION"
        ),
        AppCategory.CALCULATOR to setOf(
            "VIBRATE"
        ),
        AppCategory.BROWSER to setOf(
            "INTERNET", "ACCESS_NETWORK_STATE", "ACCESS_FINE_LOCATION",
            "READ_EXTERNAL_STORAGE", "WRITE_EXTERNAL_STORAGE", "CAMERA",
            "RECORD_AUDIO", "VIBRATE"
        ),
        AppCategory.SOCIAL_MEDIA to setOf(
            "CAMERA", "RECORD_AUDIO", "READ_CONTACTS",
            "READ_EXTERNAL_STORAGE", "WRITE_EXTERNAL_STORAGE",
            "ACCESS_FINE_LOCATION", "INTERNET", "VIBRATE",
            "WAKE_LOCK", "FOREGROUND_SERVICE"
        ),
        AppCategory.MAPS to setOf(
            "ACCESS_FINE_LOCATION", "ACCESS_COARSE_LOCATION",
            "ACCESS_BACKGROUND_LOCATION", "INTERNET",
            "ACCESS_NETWORK_STATE", "VIBRATE"
        ),
        AppCategory.MUSIC to setOf(
            "READ_EXTERNAL_STORAGE", "RECORD_AUDIO",
            "INTERNET", "FOREGROUND_SERVICE", "WAKE_LOCK"
        ),
        AppCategory.GAME to setOf(
            "INTERNET", "ACCESS_NETWORK_STATE", "VIBRATE",
            "READ_EXTERNAL_STORAGE", "WRITE_EXTERNAL_STORAGE"
        ),
        AppCategory.FITNESS to setOf(
            "BODY_SENSORS", "ACTIVITY_RECOGNITION",
            "ACCESS_FINE_LOCATION", "INTERNET", "FOREGROUND_SERVICE"
        ),
        AppCategory.FINANCE to setOf(
            "INTERNET", "USE_BIOMETRIC", "USE_FINGERPRINT",
            "CAMERA", "ACCESS_NETWORK_STATE"
        ),
        AppCategory.UTILITY to setOf(
            "INTERNET", "READ_EXTERNAL_STORAGE",
            "WRITE_EXTERNAL_STORAGE", "ACCESS_NETWORK_STATE"
        ),
        AppCategory.PRODUCTIVITY to setOf(
            "INTERNET", "READ_EXTERNAL_STORAGE",
            "WRITE_EXTERNAL_STORAGE", "READ_CALENDAR",
            "WRITE_CALENDAR", "ACCESS_NETWORK_STATE"
        ),
        AppCategory.SYSTEM to setOf(
            "INTERNET", "READ_EXTERNAL_STORAGE", "WRITE_EXTERNAL_STORAGE",
            "ACCESS_FINE_LOCATION", "ACCESS_COARSE_LOCATION",
            "CAMERA", "RECORD_AUDIO", "READ_SMS", "SEND_SMS",
            "READ_CONTACTS", "WRITE_CONTACTS", "READ_CALL_LOG",
            "WRITE_CALL_LOG", "CALL_PHONE", "READ_PHONE_STATE",
            "GET_ACCOUNTS", "VIBRATE", "RECEIVE_BOOT_COMPLETED",
            "WAKE_LOCK", "FOREGROUND_SERVICE"
        ),
        AppCategory.UNKNOWN to setOf()
    )

    /**
     * Analyzes an app's permissions and returns full risk analysis fields.
     */
    fun analyze(
        packageName: String,
        appName: String,
        requestedPermissions: List<PermissionInfo>,
        grantedPermissions: List<PermissionInfo>,
        category: AppCategory
    ): AnalysisResult {
        val highRiskPerms = requestedPermissions.filter { it.riskLevel == RiskLevel.HIGH }
        val mediumRiskPerms = requestedPermissions.filter { it.riskLevel == RiskLevel.MEDIUM }

        val simpleNames = requestedPermissions.map { it.simpleName }.toSet()

        // Check suspicious combinations
        val triggeredCombos = SUSPICIOUS_COMBINATIONS.filter { (combo, _) ->
            combo.all { simpleNames.contains(it) }
        }

        // Determine risk level
        val riskLevel = when {
            highRiskPerms.size >= 3 -> RiskLevel.HIGH
            triggeredCombos.isNotEmpty() -> RiskLevel.HIGH
            highRiskPerms.size >= 1 -> RiskLevel.MEDIUM
            mediumRiskPerms.size >= 3 -> RiskLevel.MEDIUM
            else -> RiskLevel.LOW
        }

        // Build reason
        val reasonParts = mutableListOf<String>()
        if (highRiskPerms.isNotEmpty()) {
            reasonParts.add("Uses ${highRiskPerms.size} high-risk permission(s): ${highRiskPerms.take(3).joinToString { it.simpleName }}")
        }
        triggeredCombos.forEach { (_, reason) -> reasonParts.add(reason) }
        if (mediumRiskPerms.isNotEmpty() && highRiskPerms.isEmpty()) {
            reasonParts.add("Uses ${mediumRiskPerms.size} medium-risk permission(s)")
        }
        if (reasonParts.isEmpty()) {
            reasonParts.add("Uses only low-risk or no sensitive permissions")
        }

        // Unnecessary permissions — requested but not in expected set for category
        val expectedForCategory = EXPECTED_PERMISSIONS[category] ?: emptySet()
        val unnecessaryPermissions = requestedPermissions
            .filter { it.riskLevel != RiskLevel.LOW }
            .filter { !expectedForCategory.contains(it.simpleName) }
            .map { it.simpleName }

        // Suspicious permissions — high-risk AND not expected
        val suspiciousPermissions = highRiskPerms
            .filter { !expectedForCategory.contains(it.simpleName) }
            .map { it.simpleName }
            .plus(triggeredCombos.map { (_, reason) -> reason })

        // Individual risk score (0–100, inverted: 100 = dangerous)
        val riskScore = calculateAppRiskScore(highRiskPerms.size, mediumRiskPerms.size, triggeredCombos.size)

        return AnalysisResult(
            riskLevel = riskLevel,
            riskScore = riskScore,
            riskReason = reasonParts.joinToString(". "),
            riskyPermissions = (highRiskPerms + mediumRiskPerms).distinctBy { it.simpleName },
            unnecessaryPermissions = unnecessaryPermissions,
            suspiciousPermissions = suspiciousPermissions
        )
    }

    /**
     * Categorizes an app based on its package name and app name keywords.
     */
    fun categorize(packageName: String, appName: String): AppCategory {
        val pkg = packageName.lowercase()
        val name = appName.lowercase()
        return when {
            containsAny(pkg + name, "sms", "message", "whatsapp", "telegram", "signal", "viber", "chat") -> AppCategory.MESSAGING
            containsAny(pkg + name, "camera", "photo", "gallery", "snap", "selfie", "picture") -> AppCategory.CAMERA
            containsAny(pkg + name, "calculator", "calc", "math") -> AppCategory.CALCULATOR
            containsAny(pkg + name, "browser", "chrome", "firefox", "opera", "edge", "surf", "web") -> AppCategory.BROWSER
            containsAny(pkg + name, "instagram", "facebook", "twitter", "tiktok", "social", "linkedin", "snapchat") -> AppCategory.SOCIAL_MEDIA
            containsAny(pkg + name, "maps", "gps", "navigate", "waze", "location") -> AppCategory.MAPS
            containsAny(pkg + name, "music", "spotify", "soundcloud", "player", "radio", "podcast") -> AppCategory.MUSIC
            containsAny(pkg + name, "game", "play", "puzzle", "arcade", "rpg", "strategy") -> AppCategory.GAME
            containsAny(pkg + name, "fitness", "workout", "health", "step", "yoga", "run", "exercise") -> AppCategory.FITNESS
            containsAny(pkg + name, "bank", "finance", "pay", "wallet", "money", "invest", "crypto") -> AppCategory.FINANCE
            containsAny(pkg + name, "note", "office", "docs", "sheet", "slides", "task", "todo", "calendar", "email", "mail") -> AppCategory.PRODUCTIVITY
            pkg.startsWith("com.android") || pkg.startsWith("com.google.android") -> AppCategory.SYSTEM
            else -> AppCategory.UNKNOWN
        }
    }

    private fun containsAny(text: String, vararg keywords: String): Boolean =
        keywords.any { text.contains(it) }

    private fun calculateAppRiskScore(highCount: Int, mediumCount: Int, comboCount: Int): Int {
        val score = (highCount * 15) + (mediumCount * 5) + (comboCount * 20)
        return score.coerceIn(0, 100)
    }
}

data class AnalysisResult(
    val riskLevel: RiskLevel,
    val riskScore: Int,
    val riskReason: String,
    val riskyPermissions: List<PermissionInfo>,
    val unnecessaryPermissions: List<String>,
    val suspiciousPermissions: List<String>
)
