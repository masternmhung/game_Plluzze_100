package com.puzzle100.game.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "level_results")
data class LevelResultEntity(
    @PrimaryKey
    val levelId: Int,
    val bestScore: Int,
    val stars: Int, // 0 to 3
    val timesPlayed: Int = 1,
    val completed: Boolean = false,
    val bestCompletionTimestamp: Long = System.currentTimeMillis()
)
