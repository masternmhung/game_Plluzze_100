package com.puzzle100.game.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PlayerProgressDao {

    @Query("SELECT * FROM player_progress WHERE id = 1 LIMIT 1")
    fun getPlayerProgressFlow(): Flow<PlayerProgressEntity?>

    @Query("SELECT * FROM player_progress WHERE id = 1 LIMIT 1")
    suspend fun getPlayerProgress(): PlayerProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(progress: PlayerProgressEntity)

    @Update
    suspend fun update(progress: PlayerProgressEntity)

    @Query("UPDATE player_progress SET currentUnlockedLevel = MAX(currentUnlockedLevel, :unlockedLevel) WHERE id = 1")
    suspend fun updateUnlockedLevel(unlockedLevel: Int)

    @Query("UPDATE player_progress SET highestScore = MAX(highestScore, :score) WHERE id = 1")
    suspend fun updateHighestScore(score: Int)

    @Query("UPDATE player_progress SET totalScore = totalScore + :addedScore, totalLinesCleared = totalLinesCleared + :lines, highestCombo = MAX(highestCombo, :combo), lastPlayedTimestamp = :timestamp WHERE id = 1")
    suspend fun recordStats(lines: Int, combo: Int, addedScore: Int, timestamp: Long = System.currentTimeMillis())
}
