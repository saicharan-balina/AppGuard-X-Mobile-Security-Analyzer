package com.appguardx.analyzer

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import com.appguardx.data.model.AppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Scans device for installed apps using PackageManager and builds AppInfo objects.
 */
class AppScanner(private val context: Context) {

    /**
     * Fetches all installed (non-system) apps or all apps based on [includeSystemApps].
     * Runs on IO dispatcher.
     */
    suspend fun scanApps(
        includeSystemApps: Boolean = false,
        onProgress: (current: Int, total: Int) -> Unit = { _, _ -> }
    ): List<AppInfo> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val packages = getInstalledPackages(pm)

        val filtered = if (includeSystemApps) {
            packages
        } else {
            packages.filter { pkg ->
                (pkg.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) == 0
            }
        }

        val total = filtered.size
        filtered.mapIndexedNotNull { index, pkg ->
            onProgress(index + 1, total)
            buildAppInfo(pm, pkg)
        }.sortedWith(compareByDescending<AppInfo> { it.riskLevel.ordinal }.thenBy { it.appName })
    }

    private fun getInstalledPackages(pm: PackageManager): List<PackageInfo> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.getInstalledPackages(
                PackageManager.PackageInfoFlags.of(PackageManager.GET_PERMISSIONS.toLong())
            )
        } else {
            @Suppress("DEPRECATION")
            pm.getInstalledPackages(PackageManager.GET_PERMISSIONS)
        }
    }

    private fun buildAppInfo(pm: PackageManager, pkg: PackageInfo): AppInfo? {
        return try {
            val appName = pkg.applicationInfo.loadLabel(pm).toString()
            val packageName = pkg.packageName
            val icon = try { pkg.applicationInfo.loadIcon(pm) } catch (e: Exception) { null }
            val isSystemApp = (pkg.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0

            // Collect requested permissions
            val requestedPerms = pkg.requestedPermissions?.toList() ?: emptyList()
            val grantedFlags = pkg.requestedPermissionsFlags ?: IntArray(0)

            val requestedPermissionInfos = requestedPerms.mapIndexed { i, permName ->
                val isGranted = if (i < grantedFlags.size) {
                    (grantedFlags[i] and PackageInfo.REQUESTED_PERMISSION_GRANTED) != 0
                } else false
                PermissionClassifier.classify(permName, isGranted)
            }

            val grantedPermissionInfos = requestedPermissionInfos.filter { it.isGranted }

            // Categorize and analyze
            val category = RiskAnalyzer.categorize(packageName, appName)
            val analysis = RiskAnalyzer.analyze(
                packageName = packageName,
                appName = appName,
                requestedPermissions = requestedPermissionInfos,
                grantedPermissions = grantedPermissionInfos,
                category = category
            )

            AppInfo(
                appName = appName,
                packageName = packageName,
                icon = icon,
                isSystemApp = isSystemApp,
                requestedPermissions = requestedPermissionInfos,
                grantedPermissions = grantedPermissionInfos,
                riskLevel = analysis.riskLevel,
                riskScore = analysis.riskScore,
                riskReason = analysis.riskReason,
                riskyPermissions = analysis.riskyPermissions,
                unnecessaryPermissions = analysis.unnecessaryPermissions,
                suspiciousPermissions = analysis.suspiciousPermissions,
                appCategory = category,
                installTime = pkg.firstInstallTime,
                lastUpdateTime = pkg.lastUpdateTime
            )
        } catch (e: Exception) {
            null
        }
    }
}
