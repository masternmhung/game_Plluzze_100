package com.puzzle100.game.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "player_progress")
data class PlayerProgressEntity(
    @PrimaryKey
    val id: Int = 1,
    val currentUnlockedLevel: Int = 1,
    val currentLevel: Int = 1,
    val totalScore: Long = 0L,
    val totalLevelsCompleted: Int = 0,
    val totalStars: Int = 0,
    val highestScore: Int = 0,
    val totalLinesCleared: Int = 0,
    val highestCombo: Int = 0,
    val lastPlayedTimestamp: Long = System.currentTimeMillis()
)
