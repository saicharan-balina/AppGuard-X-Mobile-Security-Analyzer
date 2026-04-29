package com.appguardx.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appguardx.data.model.AppInfo
import com.appguardx.data.model.RiskLevel
import com.appguardx.ui.components.AppCard
import com.appguardx.ui.theme.*
import com.appguardx.viewmodel.AppGuardUiState
import com.appguardx.viewmodel.SortOrder

/**
 * App list screen: Search, filter by risk, sort, pull-to-refresh, animated list.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppListScreen(
    uiState: AppGuardUiState,
    apps: List<AppInfo>,
    onBack: () -> Unit,
    onAppClick: (AppInfo) -> Unit,
    onSearchQuery: (String) -> Unit,
    onRiskFilter: (RiskLevel?) -> Unit,
    onSortOrder: (SortOrder) -> Unit,
    onRefresh: () -> Unit
) {
    var showSortMenu by remember { mutableStateOf(false) }
    val pullState = rememberPullToRefreshState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Installed Apps",
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = TextPrimary)
                    }
                },
                actions = {
                    // Sort menu
                    Box {
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(Icons.Default.Sort, "Sort", tint = TextSecondary)
                        }
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false },
                            modifier = Modifier.background(DarkCard)
                        ) {
                            SortOrder.entries.forEach { order ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            order.label,
                                            color = if (uiState.sortOrder == order) PrimaryPurple else TextPrimary
                                        )
                                    },
                                    onClick = {
                                        onSortOrder(order)
                                        showSortMenu = false
                                    },
                                    leadingIcon = {
                                        if (uiState.sortOrder == order) {
                                            Icon(Icons.Default.Check, null, tint = PrimaryPurple)
                                        }
                                    }
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        containerColor = DarkBackground
    ) { paddingValues ->

        PullToRefreshBox(
            isRefreshing = uiState.isScanning,
            onRefresh = onRefresh,
            state = pullState,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Search Bar
                item {
                    SearchBar(
                        query = uiState.searchQuery,
                        onQueryChange = onSearchQuery
                    )
                }

                // Risk Filter chips
                item {
                    RiskFilterRow(
                        selected = uiState.filterRiskLevel,
                        onSelect = onRiskFilter
                    )
                }

                // Result count
                item {
                    Text(
                        text = "${apps.size} app${if (apps.size != 1) "s" else ""} found",
                        color = TextSecondary,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                // App cards with staggered animation
                itemsIndexed(apps, key = { _, app -> app.packageName }) { index, app ->
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 4 })
                    ) {
                        AppCard(app = app, onClick = { onAppClick(app) })
                    }
                }

                if (apps.isEmpty() && !uiState.isScanning) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("🔍", fontSize = 40.sp)
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    text = "No apps match your search",
                                    color = TextSecondary,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
fun SearchBar(query: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("Search apps or packages...", color = TextHint) },
        leadingIcon = {
            Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary)
        },
        trailingIcon = {
            if (query.isNotBlank()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextSecondary)
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PrimaryPurple,
            unfocusedBorderColor = DividerColor,
            focusedContainerColor = DarkCard,
            unfocusedContainerColor = DarkCard,
            cursorColor = PrimaryPurple,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
        )
    )
}

@Composable
fun RiskFilterRow(selected: RiskLevel?, onSelect: (RiskLevel?) -> Unit) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 4.dp)
    ) {
        items(
            listOf(null, RiskLevel.HIGH, RiskLevel.MEDIUM, RiskLevel.LOW)
        ) { level ->
            val (label, color, bg) = when (level) {
                null -> Triple("All", TextPrimary, DarkCard)
                RiskLevel.HIGH -> Triple("🔴 High", HighRiskRed, HighRiskRedLight)
                RiskLevel.MEDIUM -> Triple("🟡 Medium", MediumRiskAmber, MediumRiskAmberLight)
                RiskLevel.LOW -> Triple("🟢 Low", LowRiskGreen, LowRiskGreenLight)
            }
            val isSelected = selected == level
            FilterChip(
                selected = isSelected,
                onClick = { onSelect(if (isSelected && level != null) null else level) },
                label = { Text(label, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = if (level == null) PrimaryPurple else bg,
                    selectedLabelColor = if (level == null) Color.White else color,
                    containerColor = DarkCard,
                    labelColor = TextSecondary
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = DividerColor,
                    selectedBorderColor = if (level == null) PrimaryPurple else color
                )
            )
        }
    }
}
