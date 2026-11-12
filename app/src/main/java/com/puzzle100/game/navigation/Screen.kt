package com.puzzle100.game.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash_screen")
    object Home : Screen("home_screen")
    object LevelSelection : Screen("level_selection_screen")
    object Gameplay : Screen("gameplay_screen/{levelNumber}") {
        fun createRoute(levelNumber: Int) = "gameplay_screen/$levelNumber"
    }
}
