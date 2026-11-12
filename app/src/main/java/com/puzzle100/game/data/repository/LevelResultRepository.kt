package com.puzzle100.game.data.repository

import com.puzzle100.game.data.database.LevelResultDao
import com.puzzle100.game.data.database.LevelResultEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class LevelSaveOutcome(
    val entity: LevelResultEntity,
    val starDelta: Int,
    val isNewlyCompleted: Boolean
)

class LevelResultRepository(private val levelResultDao: LevelResultDao) {

    val allLevelResultsFlow: Flow<List<LevelResultEntity>> = levelResultDao.getAllLevelResultsFlow()

    val totalStarsFlow: Flow<Int> = levelResultDao.getTotalStarsFlow().map { it ?: 0 }

    val completedLevelsCountFlow: Flow<Int> = levelResultDao.getCompletedLevelsCountFlow()

    suspend fun getLevelResult(levelId: Int): LevelResultEntity? {
        return levelResultDao.getLevelResult(levelId)
    }

    suspend fun getAllLevelResults(): List<LevelResultEntity> {
        return levelResultDao.getAllLevelResults()
    }

    /**
     * Evaluates stars based on performance against target score:
     * - 3 stars: Score >= target * 1.5
     * - 2 stars: Score >= target * 1.2
     * - 1 star:  Score >= target
     * - 0 stars: Score < target
     */
    fun calculateStars(score: Int, targetScore: Int): Int {
        return when {
            score >= (targetScore * 1.5f).toInt() -> 3
            score >= (targetScore * 1.2f).toInt() -> 2
            score >= targetScore -> 1
            else -> 0
        }
    }

    /**
     * Saves level result with anti-downgrade protection:
     * High scores and earned stars are NEVER reduced on replay.
     */
    suspend fun saveLevelResult(levelId: Int, score: Int, targetScore: Int): LevelResultEntity {
        return processLevelCompletion(levelId, score, targetScore).entity
    }

    /**
     * Processes level completion atomically, calculating delta stars and new completion flag
     * to prevent double-counting stats when a level is replayed.
     */
    suspend fun processLevelCompletion(levelId: Int, score: Int, targetScore: Int): LevelSaveOutcome {
        val existing = levelResultDao.getLevelResult(levelId)
        val earnedStars = calculateStars(score, targetScore)
        val isCompleted = score >= targetScore
        val wasCompletedBefore = existing?.completed == true
        val previousStars = existing?.stars ?: 0

        val finalStars = maxOf(previousStars, earnedStars)
        val starDelta = (finalStars - previousStars).coerceAtLeast(0)
        val isNewlyCompleted = !wasCompletedBefore && isCompleted

        val updatedEntity = if (existing != null) {
            existing.copy(
                bestScore = maxOf(existing.bestScore, score),
                stars = finalStars,
                timesPlayed = existing.timesPlayed + 1,
                completed = wasCompletedBefore || isCompleted,
                bestCompletionTimestamp = if (score >= existing.bestScore) System.currentTimeMillis() else existing.bestCompletionTimestamp
            )
        } else {
            LevelResultEntity(
                levelId = levelId,
                bestScore = score,
                stars = earnedStars,
                timesPlayed = 1,
                completed = isCompleted,
                bestCompletionTimestamp = System.currentTimeMillis()
            )
        }

        levelResultDao.insertOrReplace(updatedEntity)
        return LevelSaveOutcome(
            entity = updatedEntity,
            starDelta = starDelta,
            isNewlyCompleted = isNewlyCompleted
        )
    }
}
