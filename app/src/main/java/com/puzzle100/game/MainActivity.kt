package com.puzzle100.game

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.puzzle100.game.navigation.PuzzleNavGraph
import com.puzzle100.game.ui.theme.Puzzle100Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Puzzle100Theme {
                val navController = rememberNavController()
                PuzzleNavGraph(navController = navController)
            }
        }
    }
}
