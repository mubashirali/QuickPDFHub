package com.mobiapps.quickpdfhub.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.mobiapps.quickpdfhub.ui.screens.*

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Route.SPLASH,
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
                    navController.navigate(Route.RECENT_FILES)
                },
                onSettingsClick = {
                    navController.navigate(Route.SETTINGS)
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
                        ToolType.COMPRESS -> navController.navigate(Route.COMPRESS_OPTIONS)
                        ToolType.PDF_TO_JPG, ToolType.JPG_TO_PDF ->
                            navController.navigate(Route.processing(tool.name.lowercase()))
                        else -> navController.navigate(Route.pageThumbnail(tool.name.lowercase()))
                    }
                },
                onSettingsClick = { navController.navigate(Route.SETTINGS) },
                onViewAllRecentClick = { navController.navigate(Route.RECENT_FILES) },
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

        composable(Route.COMPRESS_OPTIONS) {
            CompressOptionsScreen(
                onCompressClick = {
                    navController.navigate(Route.processing(ToolType.COMPRESS.name.lowercase()))
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
                onCancel = { navController.popBackStack() },
                onSettingsClick = { navController.navigate(Route.SETTINGS) },
                onFinished = {
                    navController.navigate(Route.result(toolKey)) {
                        popUpTo(Route.processing(toolKey)) { inclusive = true }
                    }
                },
            )
        }

        composable(
            route = Route.RESULT,
            arguments = listOf(navArgument("toolType") { type = NavType.StringType }),
        ) {
            ResultScreen(
                onDoAnotherClick = {
                    navController.navigate(Route.HOME) {
                        popUpTo(Route.HOME) { inclusive = false }
                    }
                },
                onSettingsClick = { navController.navigate(Route.SETTINGS) },
            )
        }

        composable(Route.RECENT_FILES) {
            RecentFilesScreen(
                onSettingsClick = { navController.navigate(Route.SETTINGS) },
            )
        }

        composable(Route.SETTINGS) {
            SettingsScreen(
                onSettingsClick = {},
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
                onTryAgain = { navController.popBackStack() },
                onSettingsClick = { navController.navigate(Route.SETTINGS) },
            )
        }
    }
}
