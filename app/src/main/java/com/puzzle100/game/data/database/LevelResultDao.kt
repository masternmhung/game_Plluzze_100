package com.puzzle100.game.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LevelResultDao {

    @Query("SELECT * FROM level_results ORDER BY levelId ASC")
    fun getAllLevelResultsFlow(): Flow<List<LevelResultEntity>>

    @Query("SELECT * FROM level_results ORDER BY levelId ASC")
    suspend fun getAllLevelResults(): List<LevelResultEntity>

    @Query("SELECT * FROM level_results WHERE levelId = :levelId LIMIT 1")
    fun getLevelResultFlow(levelId: Int): Flow<LevelResultEntity?>

    @Query("SELECT * FROM level_results WHERE levelId = :levelId LIMIT 1")
    suspend fun getLevelResult(levelId: Int): LevelResultEntity?

    @Query("SELECT SUM(stars) FROM level_results WHERE completed = 1")
    fun getTotalStarsFlow(): Flow<Int?>

    @Query("SELECT COUNT(*) FROM level_results WHERE completed = 1")
    fun getCompletedLevelsCountFlow(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(result: LevelResultEntity)

    @Query("DELETE FROM level_results")
    suspend fun clearAll()
}
