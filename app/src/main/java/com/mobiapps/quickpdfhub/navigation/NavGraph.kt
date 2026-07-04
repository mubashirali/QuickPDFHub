package com.mobiapps.quickpdfhub.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.mobiapps.quickpdfhub.data.PdfWorkSession
import com.mobiapps.quickpdfhub.ui.screens.*

@Composable
fun AppNavGraph(navController: NavHostController, modifier: Modifier = Modifier) {
    NavHost(
        navController = navController,
        startDestination = Route.SPLASH,
        modifier = modifier,
    ) {
        composable(Route.SPLASH) {
            SplashScreen(
                onFinished = {
                    navController.navigate(Route.HOME) {
                        popUpTo(Route.SPLASH) { inclusive = true }
                    }
                },
            )
        }

        composable(Route.HOME) {
            HomeScreen(
                onToolClick = { tool ->
                    navController.navigate(Route.toolEntry(tool.name.lowercase()))
                },
                onViewAllRecentClick = {
                    navController.navigate(Route.recentFiles())
                },
                onSettingsClick = {
                    navController.navigate(Route.SETTINGS)
                },
                onSearchClick = {
                    navController.navigate(Route.recentFiles(searchActive = true))
                },
            )
        }

        composable(
            route = Route.TOOL_ENTRY,
            arguments = listOf(navArgument("toolType") { type = NavType.StringType }),
        ) { backStack ->
            val toolKey = backStack.arguments?.getString("toolType") ?: "merge"
            val tool = ToolType.fromKey(toolKey)
            ToolEntryScreen(
                toolType = tool,
                onFilesSelected = {
                    when (tool) {
                        ToolType.COMPRESS, ToolType.DOCX_TO_PDF, ToolType.PDF_TO_DOCX ->
                            navController.navigate(Route.processing(tool.name.lowercase()))
                        ToolType.PDF_TO_JPG, ToolType.JPG_TO_PDF, ToolType.MERGE ->
                            navController.navigate(Route.processing(tool.name.lowercase()))
                        else -> // SPLIT, DELETE, REORDER
                            navController.navigate(Route.pageThumbnail(tool.name.lowercase()))
                    }
                },
                onSettingsClick = { navController.navigate(Route.SETTINGS) },
                onViewAllRecentClick = { navController.navigate(Route.recentFiles()) },
            )
        }

        composable(
            route = Route.PAGE_THUMBNAIL,
            arguments = listOf(navArgument("toolType") { type = NavType.StringType }),
        ) { backStack ->
            val toolKey = backStack.arguments?.getString("toolType") ?: "merge"
            val tool = ToolType.fromKey(toolKey)
            PageThumbnailScreen(
                toolType = tool,
                onProcessClick = {
                    navController.navigate(Route.processing(tool.name.lowercase()))
                },
                onSettingsClick = { navController.navigate(Route.SETTINGS) },
            )
        }

        composable(
            route = Route.PROCESSING,
            arguments = listOf(navArgument("toolType") { type = NavType.StringType }),
        ) { backStack ->
            val toolKey = backStack.arguments?.getString("toolType") ?: "merge"
            ProcessingScreen(
                toolType = toolKey,
                onCancel = { navController.popBackStack() },
                onSettingsClick = { navController.navigate(Route.SETTINGS) },
                onFinished = {
                    navController.navigate(Route.result(toolKey)) {
                        // Pop everything back to HOME so the system back button
                        // and the in-app home arrow both land on the home screen.
                        popUpTo(Route.HOME) { inclusive = false }
                    }
                },
                onError = { message ->
                    PdfWorkSession.lastErrorMessage = message
                    navController.navigate(Route.ERROR) {
                        popUpTo(Route.processing(toolKey)) { inclusive = true }
                    }
                },
            )
        }

        composable(
            route = Route.RESULT,
            arguments = listOf(navArgument("toolType") { type = NavType.StringType }),
        ) {
            val goHome = {
                navController.navigate(Route.HOME) {
                    popUpTo(Route.HOME) { inclusive = false }
                }
            }
            ResultScreen(
                onGoHome = goHome,
                onDoAnotherClick = goHome,
                onSettingsClick = { navController.navigate(Route.SETTINGS) },
            )
        }

        composable(
            route = Route.RECENT_FILES,
            arguments = listOf(navArgument("searchActive") {
                type = NavType.BoolType
                defaultValue = false
            }),
        ) { backStack ->
            val openSearch = backStack.arguments?.getBoolean("searchActive") ?: false
            RecentFilesScreen(
                onSettingsClick = { navController.navigate(Route.SETTINGS) },
                initialSearchActive = openSearch,
            )
        }

        composable(Route.SETTINGS) {
            SettingsScreen(
                onBack = { navController.popBackStack() },
            )
        }

        composable(Route.PERMISSION_RATIONALE) {
            PermissionRationaleScreen(
                onContinueClick = { navController.navigate(Route.HOME) },
                onSettingsClick = { navController.navigate(Route.SETTINGS) },
            )
        }

        composable(Route.ERROR) {
            ErrorScreen(
                body = PdfWorkSession.lastErrorMessage
                    ?: "It may be corrupted or an unsupported format.",
                onTryAgain = { navController.popBackStack() },
                onSettingsClick = { navController.navigate(Route.SETTINGS) },
            )
        }
    }
}
