package com.appguardx.data.model

/**
 * Represents a single Android permission with its risk classification.
 */
data class PermissionInfo(
    val name: String,           // Full permission name e.g. android.permission.CAMERA
    val simpleName: String,     // Short name e.g. CAMERA
    val riskLevel: RiskLevel,
    val description: String,
    val isGranted: Boolean = false
)
