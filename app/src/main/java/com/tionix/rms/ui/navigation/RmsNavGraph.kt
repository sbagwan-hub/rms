package com.tionix.rms.ui.navigation

import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tionix.rms.feature.auth.presentation.LoginScreen
import com.tionix.rms.feature.dashboard.domain.model.TaskType
import com.tionix.rms.feature.dashboard.presentation.DashboardScreen
import com.tionix.rms.feature.freshboxmove.presentation.FreshBoxMoveScreen
import com.tionix.rms.feature.inventory.presentation.InventoryVerificationScreen
import com.tionix.rms.feature.merge.presentation.MergeScreen
import com.tionix.rms.feature.notifications.presentation.NotificationsScreen
import com.tionix.rms.feature.refile.presentation.RefileScreen
import com.tionix.rms.feature.reports.presentation.ReportsScreen
import com.tionix.rms.feature.filesearch.presentation.FileDetailScreen
import com.tionix.rms.feature.filesearch.presentation.FileSearchScreen
import com.tionix.rms.feature.history.presentation.HistoryScreen
import com.tionix.rms.feature.search.presentation.BoxDetailScreen
import com.tionix.rms.feature.search.presentation.SearchScreen
import com.tionix.rms.feature.settings.presentation.SettingsScreen
import com.tionix.rms.feature.sync.presentation.SyncQueueScreen
import com.tionix.rms.feature.segregation.presentation.SegregationScreen
import com.tionix.rms.feature.transfer.presentation.TransferScreen
import com.tionix.rms.ui.dashboard.RmsBottomNavigation
import com.tionix.rms.ui.dashboard.ScannerFab
import com.tionix.rms.ui.screens.ProfileScreen
import com.tionix.rms.ui.screens.splash.SplashScreen
import com.tionix.rms.ui.screens.scan.ScanScreen

@Composable
fun RmsNavGraph() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.hierarchy?.firstOrNull()?.route

    Scaffold(
        bottomBar = {
            if (currentRoute in RmsRoutes.bottomBarRoutes) {
                RmsBottomNavigation(
                    currentRoute = currentRoute ?: RmsRoutes.HOME,
                    onItemSelected = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            if (currentRoute in RmsRoutes.bottomBarRoutes) {
                ScannerFab(
                    modifier = Modifier.offset(y = 56.dp),
                    onClick = {
                        navController.navigate(RmsRoutes.SCAN) {
                            launchSingleTop = true
                        }
                    }
                )
            }
        },
        floatingActionButtonPosition = FabPosition.Center,
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = RmsRoutes.SPLASH,
            modifier = Modifier.padding(paddingValues)
        ) {
            composable(RmsRoutes.SCAN) {
                ScanScreen(onBack = { navController.popBackStack() })
            }

            composable(RmsRoutes.SPLASH) {
                SplashScreen(
                    onNavigateToLogin = {
                        navController.navigate(RmsRoutes.LOGIN) {
                            popUpTo(RmsRoutes.SPLASH) { inclusive = true }
                        }
                    },
                    onNavigateToDashboard = {
                        navController.navigate(RmsRoutes.HOME) {
                            popUpTo(RmsRoutes.SPLASH) { inclusive = true }
                        }
                    }
                )
            }

            composable(RmsRoutes.LOGIN) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(RmsRoutes.HOME) {
                            popUpTo(RmsRoutes.LOGIN) { inclusive = true }
                        }
                    }
                )
            }

            composable(RmsRoutes.HOME) {
                DashboardScreen(
                    onTaskClick = { task ->
                        val route = when (task.type) {
                            TaskType.FRESH_BOX_MOVE -> RmsRoutes.FRESH_BOX_MOVE
                            TaskType.INVENTORY_VERIFICATION -> RmsRoutes.INVENTORY_VERIFICATION
                            TaskType.REFILE -> RmsRoutes.REFILE
                            TaskType.SEGREGATION -> RmsRoutes.SEGREGATION
                            TaskType.MERGE -> RmsRoutes.MERGE
                            TaskType.TRANSFER -> RmsRoutes.TRANSFER
                        }
                        navController.navigate(route)
                    },
                    onLogout = {
                        navController.navigate(RmsRoutes.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            composable(RmsRoutes.SEARCH) {
                SearchScreen(
                    onBack = { navController.popBackStack() },
                    onResultClick = { result ->
                        if (result is com.tionix.rms.feature.search.domain.model.SearchResult.BoxResult) {
                            navController.navigate(RmsRoutes.boxDetail(result.id))
                        }
                    }
                )
            }

            composable(RmsRoutes.FILE_SEARCH) {
                FileSearchScreen(
                    onBack = { navController.popBackStack() },
                    onResultClick = { result ->
                        if (result is com.tionix.rms.feature.search.domain.model.SearchResult.FileRecordResult) {
                            navController.navigate(RmsRoutes.fileDetail(result.id))
                        }
                    }
                )
            }

            composable(
                route = RmsRoutes.BOX_DETAIL,
                arguments = listOf(navArgument("boxId") { type = NavType.StringType })
            ) { backStackEntry ->
                val boxId = backStackEntry.arguments?.getString("boxId") ?: ""
                BoxDetailScreen(
                    boxId = boxId,
                    onBack = { navController.popBackStack() },
                    onNavigateToTransfer = { /* Navigate to Transfer with boxId */ },
                    onNavigateToRefile = { /* Navigate to Refile with boxId */ },
                    canTransfer = false,
                    canRefile = true
                )
            }

            composable(
                route = RmsRoutes.FILE_DETAIL,
                arguments = listOf(navArgument("fileId") { type = NavType.StringType })
            ) { backStackEntry ->
                val fileId = backStackEntry.arguments?.getString("fileId") ?: ""
                FileDetailScreen(
                    fileId = fileId,
                    onBack = { navController.popBackStack() },
                    onNavigateToRefile = { /* Navigate to Refile with fileId */ },
                    canRefile = true
                )
            }

            composable(RmsRoutes.HISTORY) {
                HistoryScreen(
                    onBack = { navController.popBackStack() },
                    userRole = "OPERATOR",
                    userId = "user-123"
                )
            }

            composable(RmsRoutes.SETTINGS) {
                SettingsScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable(RmsRoutes.SYNC_QUEUE) {
                SyncQueueScreen(
                    onBack = { navController.popBackStack() },
                    userRole = "OPERATOR"
                )
            }

            composable(RmsRoutes.REPORTS) {
                ReportsScreen(onBack = { navController.popBackStack() })
            }

            composable(RmsRoutes.PROFILE) {
                ProfileScreen(
                    onLogout = {
                        navController.navigate(RmsRoutes.LOGIN) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }

            composable(RmsRoutes.NOTIFICATIONS) {
                NotificationsScreen(onBack = { navController.popBackStack() })
            }

            composable(RmsRoutes.FRESH_BOX_MOVE) {
                FreshBoxMoveScreen(onBack = { navController.popBackStack() })
            }

            composable(RmsRoutes.INVENTORY_VERIFICATION) {
                InventoryVerificationScreen(onBack = { navController.popBackStack() })
            }

            composable(RmsRoutes.REFILE) {
                RefileScreen(
                    onBack = { navController.popBackStack() },
                    canOverride = false
                )
            }

            composable(RmsRoutes.TRANSFER) {
                TransferScreen(
                    onBack = { navController.popBackStack() },
                    canStartTransfer = false
                )
            }

            composable(RmsRoutes.SEGREGATION) {
                SegregationScreen(
                    onBack = { navController.popBackStack() },
                    canStartSegregation = false
                )
            }

            composable(RmsRoutes.MERGE) {
                MergeScreen(
                    onBack = { navController.popBackStack() },
                    canStartMerge = false
                )
            }
        }
    }
}
