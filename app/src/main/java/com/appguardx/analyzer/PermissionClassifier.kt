package com.appguardx.analyzer

import com.appguardx.data.model.PermissionInfo
import com.appguardx.data.model.RiskLevel

/**
 * Classifies Android permissions into risk levels (High / Medium / Low)
 * and provides human-readable descriptions.
 */
object PermissionClassifier {

    // ───────────── HIGH RISK ─────────────
    private val HIGH_RISK_PERMISSIONS = setOf(
        "READ_SMS",
        "SEND_SMS",
        "RECEIVE_SMS",
        "READ_CONTACTS",
        "WRITE_CONTACTS",
        "RECORD_AUDIO",
        "READ_CALL_LOG",
        "WRITE_CALL_LOG",
        "ACCESS_FINE_LOCATION",
        "CAMERA",
        "PROCESS_OUTGOING_CALLS",
        "CALL_PHONE",
        "ANSWER_PHONE_CALLS",
        "READ_PHONE_STATE",
        "READ_PHONE_NUMBERS",
        "USE_BIOMETRIC",
        "USE_FINGERPRINT",
        "BODY_SENSORS",
        "ACTIVITY_RECOGNITION",
        "MANAGE_EXTERNAL_STORAGE"
    )

    // ───────────── MEDIUM RISK ─────────────
    private val MEDIUM_RISK_PERMISSIONS = setOf(
        "READ_EXTERNAL_STORAGE",
        "WRITE_EXTERNAL_STORAGE",
        "ACCESS_COARSE_LOCATION",
        "ACCESS_BACKGROUND_LOCATION",
        "BLUETOOTH",
        "BLUETOOTH_ADMIN",
        "BLUETOOTH_SCAN",
        "BLUETOOTH_CONNECT",
        "INTERNET",
        "CHANGE_WIFI_STATE",
        "ACCESS_WIFI_STATE",
        "ACCESS_NETWORK_STATE",
        "NFC",
        "CHANGE_NETWORK_STATE",
        "RECEIVE_BOOT_COMPLETED",
        "VIBRATE",
        "FOREGROUND_SERVICE",
        "WAKE_LOCK",
        "GET_ACCOUNTS",
        "READ_CALENDAR",
        "WRITE_CALENDAR",
        "REQUEST_INSTALL_PACKAGES"
    )

    private val DESCRIPTIONS = mapOf(
        "READ_SMS" to "Can read your SMS messages",
        "SEND_SMS" to "Can send SMS on your behalf",
        "RECEIVE_SMS" to "Can intercept incoming SMS messages",
        "READ_CONTACTS" to "Can access your entire contacts list",
        "WRITE_CONTACTS" to "Can add or modify your contacts",
        "RECORD_AUDIO" to "Can record audio using the microphone",
        "READ_CALL_LOG" to "Can read your call history",
        "WRITE_CALL_LOG" to "Can modify your call history",
        "ACCESS_FINE_LOCATION" to "Can track your precise GPS location",
        "CAMERA" to "Can access front and back cameras",
        "CALL_PHONE" to "Can make phone calls without your confirmation",
        "PROCESS_OUTGOING_CALLS" to "Can monitor and redirect outgoing calls",
        "READ_PHONE_STATE" to "Can read phone state and identity",
        "READ_EXTERNAL_STORAGE" to "Can read files from external storage",
        "WRITE_EXTERNAL_STORAGE" to "Can write files to external storage",
        "ACCESS_COARSE_LOCATION" to "Can access approximate location (network-based)",
        "ACCESS_BACKGROUND_LOCATION" to "Can access location even when app is closed",
        "BLUETOOTH" to "Can connect to Bluetooth devices",
        "INTERNET" to "Can access the internet",
        "GET_ACCOUNTS" to "Can access accounts registered on the device",
        "READ_CALENDAR" to "Can read your calendar events",
        "WRITE_CALENDAR" to "Can create or modify calendar events",
        "REQUEST_INSTALL_PACKAGES" to "Can install other apps silently",
        "USE_BIOMETRIC" to "Can use fingerprint/face unlock",
        "BODY_SENSORS" to "Can access heart rate and body sensors",
        "ACTIVITY_RECOGNITION" to "Can detect physical activities (walking, running, etc.)",
        "MANAGE_EXTERNAL_STORAGE" to "Can access ALL files on device storage",
        "RECEIVE_BOOT_COMPLETED" to "Starts automatically when device boots",
        "WAKE_LOCK" to "Can keep device screen and CPU awake"
    )

    /**
     * Classifies a single permission by its simple name.
     */
    fun classify(fullPermissionName: String, isGranted: Boolean = false): PermissionInfo {
        val simpleName = extractSimpleName(fullPermissionName)
        val riskLevel = when {
            HIGH_RISK_PERMISSIONS.contains(simpleName) -> RiskLevel.HIGH
            MEDIUM_RISK_PERMISSIONS.contains(simpleName) -> RiskLevel.MEDIUM
            else -> RiskLevel.LOW
        }
        val description = DESCRIPTIONS[simpleName] ?: "System permission: $simpleName"
        return PermissionInfo(
            name = fullPermissionName,
            simpleName = simpleName,
            riskLevel = riskLevel,
            description = description,
            isGranted = isGranted
        )
    }

    /**
     * Extracts the simple name from full permission string.
     * e.g. "android.permission.CAMERA" → "CAMERA"
     */
    fun extractSimpleName(fullName: String): String {
        return fullName.substringAfterLast(".")
    }

    fun isHighRisk(simpleName: String) = HIGH_RISK_PERMISSIONS.contains(simpleName)
    fun isMediumRisk(simpleName: String) = MEDIUM_RISK_PERMISSIONS.contains(simpleName)
}
