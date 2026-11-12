package com.puzzle100.game.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.puzzle100.game.ui.screens.game.GameplayScreen
import com.puzzle100.game.ui.screens.home.HomeScreen
import com.puzzle100.game.ui.screens.level.LevelSelectionScreen
import com.puzzle100.game.ui.screens.splash.SplashScreen

@Composable
fun PuzzleNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                onPlayClicked = {
                    navController.navigate(Screen.Gameplay.createRoute(1))
                },
                onContinueClicked = { levelNumber ->
                    navController.navigate(Screen.Gameplay.createRoute(levelNumber))
                },
                onSelectLevelClicked = {
                    navController.navigate(Screen.LevelSelection.route)
                }
            )
        }

        composable(Screen.LevelSelection.route) {
            LevelSelectionScreen(
                onBackClicked = {
                    navController.popBackStack()
                },
                onLevelSelected = { levelNumber ->
                    navController.navigate(Screen.Gameplay.createRoute(levelNumber))
                }
            )
        }

        composable(
            route = Screen.Gameplay.route,
            arguments = listOf(
                navArgument("levelNumber") {
                    type = NavType.IntType
                    defaultValue = 1
                }
            )
        ) { backStackEntry ->
            val levelNumber = backStackEntry.arguments?.getInt("levelNumber") ?: 1
            GameplayScreen(
                levelNumber = levelNumber,
                onBackClicked = {
                    navController.popBackStack()
                }
            )
        }
    }
}
