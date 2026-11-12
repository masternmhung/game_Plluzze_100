package com.puzzle100.game.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ActiveGameDao {

    @Query("SELECT * FROM active_game ORDER BY savedTimestamp DESC LIMIT 1")
    fun getLatestActiveGameFlow(): Flow<ActiveGameEntity?>

    @Query("SELECT * FROM active_game ORDER BY savedTimestamp DESC LIMIT 1")
    suspend fun getLatestActiveGame(): ActiveGameEntity?

    @Query("SELECT * FROM active_game WHERE levelId = :levelId LIMIT 1")
    fun getActiveGameForLevelFlow(levelId: Int): Flow<ActiveGameEntity?>

    @Query("SELECT * FROM active_game WHERE levelId = :levelId LIMIT 1")
    suspend fun getActiveGameForLevel(levelId: Int): ActiveGameEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveActiveGame(activeGame: ActiveGameEntity)

    @Query("DELETE FROM active_game WHERE levelId = :levelId")
    suspend fun deleteActiveGame(levelId: Int)

    @Query("DELETE FROM active_game")
    suspend fun clearAllActiveGames()
}
