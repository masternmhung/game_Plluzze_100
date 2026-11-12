package com.puzzle100.game.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "active_game")
data class ActiveGameEntity(
    @PrimaryKey
    val levelId: Int,
    val boardData: String,
    val availablePiecesData: String,
    val score: Int,
    val combo: Int = 0,
    val highestCombo: Int = 0,
    val totalRowsCleared: Int = 0,
    val totalColumnsCleared: Int = 0,
    val movesCount: Int = 0,
    val gameStatus: String = "PLAYING",
    val savedTimestamp: Long = System.currentTimeMillis()
)
