package com.appguardx.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.appguardx.analyzer.AppScanner
import com.appguardx.data.model.AppInfo
import com.appguardx.data.model.RiskLevel
import com.appguardx.data.model.ScanResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * MVVM ViewModel for AppGuard X.
 * Exposes UI state via StateFlow and drives scanning via coroutines.
 */
class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val scanner = AppScanner(application)

    // ───── UI State ─────
    private val _uiState = MutableStateFlow(AppGuardUiState())
    val uiState: StateFlow<AppGuardUiState> = _uiState.asStateFlow()

    // ───── Filtered / Searched list ─────
    private val _filteredApps = MutableStateFlow<List<AppInfo>>(emptyList())
    val filteredApps: StateFlow<List<AppInfo>> = _filteredApps.asStateFlow()

    // ───── Selected app for detail screen ─────
    private val _selectedApp = MutableStateFlow<AppInfo?>(null)
    val selectedApp: StateFlow<AppInfo?> = _selectedApp.asStateFlow()

    fun startScan(includeSystemApps: Boolean = false) {
        viewModelScope.launch {
            _uiState.update { it.copy(isScanning = true, scanProgress = 0f, error = null) }
            try {
                val apps = scanner.scanApps(
                    includeSystemApps = includeSystemApps,
                    onProgress = { current, total ->
                        _uiState.update { state ->
                            state.copy(scanProgress = current.toFloat() / total.toFloat())
                        }
                    }
                )
                val result = ScanResult(apps = apps)
                _uiState.update { state ->
                    state.copy(
                        isScanning = false,
                        scanResult = result,
                        scanProgress = 1f,
                        includeSystemApps = includeSystemApps,
                        searchQuery = "",
                        filterRiskLevel = null
                    )
                }
                applyFilters(apps, "", null)
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(
                        isScanning = false,
                        error = e.message ?: "Unknown error during scan"
                    )
                }
            }
        }
    }

    fun selectApp(app: AppInfo) {
        _selectedApp.update { app }
    }

    fun clearSelectedApp() {
        _selectedApp.update { null }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        val allApps = _uiState.value.scanResult?.apps ?: return
        applyFilters(allApps, query, _uiState.value.filterRiskLevel)
    }

    fun updateRiskFilter(level: RiskLevel?) {
        _uiState.update { it.copy(filterRiskLevel = level) }
        val allApps = _uiState.value.scanResult?.apps ?: return
        applyFilters(allApps, _uiState.value.searchQuery, level)
    }

    fun updateSortOrder(order: SortOrder) {
        _uiState.update { it.copy(sortOrder = order) }
        val allApps = _uiState.value.scanResult?.apps ?: return
        val sorted = sortApps(allApps, order)
        applyFilters(sorted, _uiState.value.searchQuery, _uiState.value.filterRiskLevel)
    }

    fun toggleSystemApps(include: Boolean) {
        _uiState.update { it.copy(includeSystemApps = include) }
        startScan(include)
    }

    private fun applyFilters(apps: List<AppInfo>, query: String, riskFilter: RiskLevel?) {
        var result = apps
        if (query.isNotBlank()) {
            result = result.filter {
                it.appName.contains(query, ignoreCase = true) ||
                        it.packageName.contains(query, ignoreCase = true)
            }
        }
        if (riskFilter != null) {
            result = result.filter { it.riskLevel == riskFilter }
        }
        result = sortApps(result, _uiState.value.sortOrder)
        _filteredApps.update { result }
    }

    private fun sortApps(apps: List<AppInfo>, order: SortOrder): List<AppInfo> {
        return when (order) {
            SortOrder.RISK_HIGH_FIRST -> apps.sortedWith(compareBy<AppInfo> { it.riskLevel }.thenBy { it.appName })
            SortOrder.RISK_LOW_FIRST -> apps.sortedWith(compareByDescending<AppInfo> { it.riskLevel }.thenBy { it.appName })
            SortOrder.NAME_AZ -> apps.sortedBy { it.appName }
            SortOrder.NAME_ZA -> apps.sortedByDescending { it.appName }
        }
    }
}

data class AppGuardUiState(
    val isScanning: Boolean = false,
    val scanProgress: Float = 0f,
    val scanResult: ScanResult? = null,
    val searchQuery: String = "",
    val filterRiskLevel: RiskLevel? = null,
    val sortOrder: SortOrder = SortOrder.RISK_HIGH_FIRST,
    val includeSystemApps: Boolean = false,
    val error: String? = null
)

enum class SortOrder(val label: String) {
    RISK_HIGH_FIRST("Risk: High First"),
    RISK_LOW_FIRST("Risk: Low First"),
    NAME_AZ("Name: A–Z"),
    NAME_ZA("Name: Z–A")
}
