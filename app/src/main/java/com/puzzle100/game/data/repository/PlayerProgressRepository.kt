package com.puzzle100.game.data.repository

import com.puzzle100.game.data.database.PlayerProgressDao
import com.puzzle100.game.data.database.PlayerProgressEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class PlayerProgressRepository(private val playerProgressDao: PlayerProgressDao) {

    val playerProgressFlow: Flow<PlayerProgressEntity> = playerProgressDao.getPlayerProgressFlow().map { entity ->
        entity ?: PlayerProgressEntity(
            id = 1,
            currentUnlockedLevel = 1,
            currentLevel = 1,
            totalScore = 0L,
            totalLevelsCompleted = 0,
            totalStars = 0,
            highestScore = 0,
            totalLinesCleared = 0,
            highestCombo = 0,
            lastPlayedTimestamp = System.currentTimeMillis()
        )
    }

    suspend fun getProgress(): PlayerProgressEntity {
        return playerProgressDao.getPlayerProgress() ?: PlayerProgressEntity().also {
            playerProgressDao.insertOrUpdate(it)
        }
    }

    suspend fun unlockNextLevel(completedLevelId: Int) {
        val nextLevel = (completedLevelId + 1).coerceAtMost(100)
        playerProgressDao.updateUnlockedLevel(nextLevel)
    }

    suspend fun updateCurrentLevel(levelId: Int) {
        val current = getProgress()
        playerProgressDao.insertOrUpdate(current.copy(currentLevel = levelId))
    }

    suspend fun updateHighScore(score: Int) {
        playerProgressDao.updateHighestScore(score)
    }

    suspend fun recordStats(linesCleared: Int, combo: Int, addedScore: Int) {
        playerProgressDao.recordStats(
            lines = linesCleared,
            combo = combo,
            addedScore = addedScore,
            timestamp = System.currentTimeMillis()
        )
    }

    /**
     * Updates player progress upon completing a level:
     * - Only increments totalStars by the star delta (never double counts replayed stars)
     * - Only increments totalLevelsCompleted if the level was newly completed
     * - Only unlocks next level up to 100
     */
    suspend fun onLevelCompleted(
        levelId: Int,
        score: Int,
        starDelta: Int,
        isNewlyCompleted: Boolean = true
    ) {
        val current = getProgress()
        val nextUnlocked = maxOf(current.currentUnlockedLevel, (levelId + 1).coerceAtMost(100))
        val newHighest = maxOf(current.highestScore, score)
        val newStars = current.totalStars + starDelta
        val newCompleted = current.totalLevelsCompleted + (if (isNewlyCompleted) 1 else 0)

        playerProgressDao.insertOrUpdate(
            current.copy(
                currentUnlockedLevel = nextUnlocked,
                highestScore = newHighest,
                totalStars = newStars,
                totalLevelsCompleted = newCompleted,
                lastPlayedTimestamp = System.currentTimeMillis()
            )
        )
    }
}
