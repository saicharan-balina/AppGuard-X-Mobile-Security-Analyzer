package com.appguardx.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.appguardx.ui.screens.AppDetailScreen
import com.appguardx.ui.screens.AppListScreen
import com.appguardx.ui.screens.HomeScreen
import com.appguardx.viewmodel.AppViewModel

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object AppList : Screen("app_list")
    object AppDetail : Screen("app_detail")
}

@Composable
fun AppNavigation(viewModel: AppViewModel) {
    val navController = rememberNavController()
    val uiState by viewModel.uiState.collectAsState()
    val filteredApps by viewModel.filteredApps.collectAsState()
    val selectedApp by viewModel.selectedApp.collectAsState()

    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                uiState = uiState,
                onScanClick = { viewModel.startScan(uiState.includeSystemApps) },
                onSystemAppsToggle = { viewModel.toggleSystemApps(it) },
                onNavigateToList = { navController.navigate(Screen.AppList.route) }
            )
        }

        composable(Screen.AppList.route) {
            AppListScreen(
                uiState = uiState,
                apps = filteredApps,
                onBack = { navController.popBackStack() },
                onAppClick = { app ->
                    viewModel.selectApp(app)
                    navController.navigate(Screen.AppDetail.route)
                },
                onSearchQuery = { viewModel.updateSearchQuery(it) },
                onRiskFilter = { viewModel.updateRiskFilter(it) },
                onSortOrder = { viewModel.updateSortOrder(it) },
                onRefresh = { viewModel.startScan(uiState.includeSystemApps) }
            )
        }

        composable(Screen.AppDetail.route) {
            val app = selectedApp
            if (app != null) {
                AppDetailScreen(
                    app = app,
                    onBack = {
                        viewModel.clearSelectedApp()
                        navController.popBackStack()
                    }
                )
            }
        }
    }
}
