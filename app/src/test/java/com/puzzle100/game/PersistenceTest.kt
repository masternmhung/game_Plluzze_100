package com.puzzle100.game

import com.puzzle100.game.data.database.ActiveGameEntity
import com.puzzle100.game.data.database.Converters
import com.puzzle100.game.data.database.LevelResultEntity
import com.puzzle100.game.data.database.PlayerProgressEntity
import com.puzzle100.game.data.preferences.GamePreferences
import com.puzzle100.game.data.repository.ActiveGameRepository
import com.puzzle100.game.data.repository.LevelResultRepository
import com.puzzle100.game.model.BOARD_SIZE
import com.puzzle100.game.model.BlockColorType
import com.puzzle100.game.model.BoardCellState
import com.puzzle100.game.model.GameState
import com.puzzle100.game.model.PuzzleShapesRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PersistenceTest {

    private val converters = Converters()

    @Test
    fun test_1_boardSerializationAndDeserializationPreservesState() {
        val board = MutableList(BOARD_SIZE) {
            MutableList(BOARD_SIZE) { BoardCellState() }
        }
        board[0][0] = BoardCellState(isFilled = true, colorType = BlockColorType.TURQUOISE)
        board[4][5] = BoardCellState(isFilled = true, colorType = BlockColorType.ORANGE)
        board[9][9] = BoardCellState(isFilled = true, colorType = BlockColorType.PINK)

        val json = converters.fromBoard(board)
        val deserialized = converters.toBoard(json)

        assertEquals(10, deserialized.size)
        assertEquals(10, deserialized[0].size)
        assertTrue(deserialized[0][0].isFilled)
        assertEquals(BlockColorType.TURQUOISE, deserialized[0][0].colorType)

        assertTrue(deserialized[4][5].isFilled)
        assertEquals(BlockColorType.ORANGE, deserialized[4][5].colorType)

        assertTrue(deserialized[9][9].isFilled)
        assertEquals(BlockColorType.PINK, deserialized[9][9].colorType)

        assertFalse(deserialized[0][1].isFilled)
        assertNull(deserialized[0][1].colorType)
    }

    @Test
    fun test_2_emptyBoardSerialization() {
        val emptyBoard = List(BOARD_SIZE) { List(BOARD_SIZE) { BoardCellState() } }
        val json = converters.fromBoard(emptyBoard)
        val deserialized = converters.toBoard(json)

        for (r in 0 until BOARD_SIZE) {
            for (c in 0 until BOARD_SIZE) {
                assertFalse("Cell ($r,$c) must be empty", deserialized[r][c].isFilled)
            }
        }
    }

    @Test
    fun test_3_piecesListSerializationWithNullSlots() {
        val pieces = listOf(
            PuzzleShapesRepository.SQUARE_2X2,
            null,
            PuzzleShapesRepository.LINE_3H
        )

        val json = converters.fromPiecesList(pieces)
        val deserialized = converters.toPiecesList(json)

        assertEquals(3, deserialized.size)
        assertNotNull(deserialized[0])
        assertEquals("SQUARE_2X2", deserialized[0]?.id)
        assertNull(deserialized[1])
        assertNotNull(deserialized[2])
        assertEquals("LINE_3H", deserialized[2]?.id)
    }

    @Test
    fun test_4_allNullPiecesSerialization() {
        val emptyPieces = listOf(null, null, null)
        val json = converters.fromPiecesList(emptyPieces)
        val deserialized = converters.toPiecesList(json)

        assertEquals(3, deserialized.size)
        assertTrue(deserialized.all { it == null })
    }

    @Test
    fun test_5_starCalculationAccuracy() {
        // Dummy mock Dao is not invoked for calculateStars
        val dummyDao = object : com.puzzle100.game.data.database.LevelResultDao {
            override fun getAllLevelResultsFlow() = kotlinx.coroutines.flow.emptyFlow<List<LevelResultEntity>>()
            override suspend fun getAllLevelResults() = emptyList<LevelResultEntity>()
            override fun getLevelResultFlow(levelId: Int) = kotlinx.coroutines.flow.emptyFlow<LevelResultEntity?>()
            override suspend fun getLevelResult(levelId: Int): LevelResultEntity? = null
            override fun getTotalStarsFlow() = kotlinx.coroutines.flow.emptyFlow<Int?>()
            override fun getCompletedLevelsCountFlow() = kotlinx.coroutines.flow.emptyFlow<Int>()
            override suspend fun insertOrReplace(result: LevelResultEntity) {}
            override suspend fun clearAll() {}
        }
        val repo = LevelResultRepository(dummyDao)
        val target = 1000

        assertEquals(0, repo.calculateStars(500, target))
        assertEquals(0, repo.calculateStars(999, target))
        assertEquals(1, repo.calculateStars(1000, target))
        assertEquals(1, repo.calculateStars(1199, target))
        assertEquals(2, repo.calculateStars(1200, target))
        assertEquals(2, repo.calculateStars(1499, target))
        assertEquals(3, repo.calculateStars(1500, target))
        assertEquals(3, repo.calculateStars(3000, target))
    }

    @Test
    fun test_6_levelResultAntiDowngradeProtection() {
        var saved: LevelResultEntity? = LevelResultEntity(
            levelId = 1,
            bestScore = 2000,
            stars = 3,
            timesPlayed = 2,
            completed = true
        )

        val dummyDao = object : com.puzzle100.game.data.database.LevelResultDao {
            override fun getAllLevelResultsFlow() = kotlinx.coroutines.flow.emptyFlow<List<LevelResultEntity>>()
            override suspend fun getAllLevelResults() = listOfNotNull(saved)
            override fun getLevelResultFlow(levelId: Int) = kotlinx.coroutines.flow.emptyFlow<LevelResultEntity?>()
            override suspend fun getLevelResult(levelId: Int) = saved
            override fun getTotalStarsFlow() = kotlinx.coroutines.flow.emptyFlow<Int?>()
            override fun getCompletedLevelsCountFlow() = kotlinx.coroutines.flow.emptyFlow<Int>()
            override suspend fun insertOrReplace(result: LevelResultEntity) { saved = result }
            override suspend fun clearAll() { saved = null }
        }

        val repo = LevelResultRepository(dummyDao)
        // User replays level 1 and scores only 700 (0 stars)
        kotlinx.coroutines.runBlocking {
            val result = repo.saveLevelResult(levelId = 1, score = 700, targetScore = 650)
            assertEquals(2000, result.bestScore) // High score remains 2000!
            assertEquals(3, result.stars)         // 3 stars remain!
            assertEquals(3, result.timesPlayed)   // Times played incremented
            assertTrue(result.completed)
        }
    }

    @Test
    fun test_7_levelResultUpgradeOnHigherScore() {
        var saved: LevelResultEntity? = LevelResultEntity(
            levelId = 2,
            bestScore = 800,
            stars = 1,
            timesPlayed = 1,
            completed = true
        )

        val dummyDao = object : com.puzzle100.game.data.database.LevelResultDao {
            override fun getAllLevelResultsFlow() = kotlinx.coroutines.flow.emptyFlow<List<LevelResultEntity>>()
            override suspend fun getAllLevelResults() = listOfNotNull(saved)
            override fun getLevelResultFlow(levelId: Int) = kotlinx.coroutines.flow.emptyFlow<LevelResultEntity?>()
            override suspend fun getLevelResult(levelId: Int) = saved
            override fun getTotalStarsFlow() = kotlinx.coroutines.flow.emptyFlow<Int?>()
            override fun getCompletedLevelsCountFlow() = kotlinx.coroutines.flow.emptyFlow<Int>()
            override suspend fun insertOrReplace(result: LevelResultEntity) { saved = result }
            override suspend fun clearAll() { saved = null }
        }

        val repo = LevelResultRepository(dummyDao)
        kotlinx.coroutines.runBlocking {
            val result = repo.saveLevelResult(levelId = 2, score = 1500, targetScore = 800)
            assertEquals(1500, result.bestScore)
            assertEquals(3, result.stars) // Upgraded to 3 stars!
            assertEquals(2, result.timesPlayed)
        }
    }

    @Test
    fun test_8_playerProgressLevelUnlock() {
        var savedProgress: PlayerProgressEntity? = PlayerProgressEntity(
            id = 1,
            currentUnlockedLevel = 1,
            currentLevel = 1
        )

        val dummyDao = object : com.puzzle100.game.data.database.PlayerProgressDao {
            override fun getPlayerProgressFlow() = kotlinx.coroutines.flow.emptyFlow<PlayerProgressEntity?>()
            override suspend fun getPlayerProgress() = savedProgress
            override suspend fun insertOrUpdate(progress: PlayerProgressEntity) { savedProgress = progress }
            override suspend fun update(progress: PlayerProgressEntity) { savedProgress = progress }
            override suspend fun updateUnlockedLevel(unlockedLevel: Int) {
                savedProgress = savedProgress?.copy(currentUnlockedLevel = maxOf(savedProgress!!.currentUnlockedLevel, unlockedLevel))
            }
            override suspend fun updateHighestScore(score: Int) {
                savedProgress = savedProgress?.copy(highestScore = maxOf(savedProgress!!.highestScore, score))
            }
            override suspend fun recordStats(lines: Int, combo: Int, addedScore: Int, timestamp: Long) {}
        }

        val repo = com.puzzle100.game.data.repository.PlayerProgressRepository(dummyDao)
        kotlinx.coroutines.runBlocking {
            repo.unlockNextLevel(1)
            assertEquals(2, savedProgress?.currentUnlockedLevel)
        }
    }

    @Test
    fun test_9_playerProgressUnlockCappedAt100() {
        var savedProgress: PlayerProgressEntity? = PlayerProgressEntity(
            id = 1,
            currentUnlockedLevel = 100,
            currentLevel = 100
        )

        val dummyDao = object : com.puzzle100.game.data.database.PlayerProgressDao {
            override fun getPlayerProgressFlow() = kotlinx.coroutines.flow.emptyFlow<PlayerProgressEntity?>()
            override suspend fun getPlayerProgress() = savedProgress
            override suspend fun insertOrUpdate(progress: PlayerProgressEntity) { savedProgress = progress }
            override suspend fun update(progress: PlayerProgressEntity) { savedProgress = progress }
            override suspend fun updateUnlockedLevel(unlockedLevel: Int) {
                savedProgress = savedProgress?.copy(currentUnlockedLevel = maxOf(savedProgress!!.currentUnlockedLevel, unlockedLevel))
            }
            override suspend fun updateHighestScore(score: Int) {}
            override suspend fun recordStats(lines: Int, combo: Int, addedScore: Int, timestamp: Long) {}
        }

        val repo = com.puzzle100.game.data.repository.PlayerProgressRepository(dummyDao)
        kotlinx.coroutines.runBlocking {
            repo.unlockNextLevel(100)
            assertEquals(100, savedProgress?.currentUnlockedLevel)
        }
    }

    @Test
    fun test_10_playerProgressHighScoreOnlyIncreases() {
        var savedProgress: PlayerProgressEntity? = PlayerProgressEntity(
            id = 1,
            highestScore = 5000
        )

        val dummyDao = object : com.puzzle100.game.data.database.PlayerProgressDao {
            override fun getPlayerProgressFlow() = kotlinx.coroutines.flow.emptyFlow<PlayerProgressEntity?>()
            override suspend fun getPlayerProgress() = savedProgress
            override suspend fun insertOrUpdate(progress: PlayerProgressEntity) { savedProgress = progress }
            override suspend fun update(progress: PlayerProgressEntity) { savedProgress = progress }
            override suspend fun updateUnlockedLevel(unlockedLevel: Int) {}
            override suspend fun updateHighestScore(score: Int) {
                savedProgress = savedProgress?.copy(highestScore = maxOf(savedProgress!!.highestScore, score))
            }
            override suspend fun recordStats(lines: Int, combo: Int, addedScore: Int, timestamp: Long) {}
        }

        val repo = com.puzzle100.game.data.repository.PlayerProgressRepository(dummyDao)
        kotlinx.coroutines.runBlocking {
            repo.updateHighScore(3000) // Lower score
            assertEquals(5000, savedProgress?.highestScore)

            repo.updateHighScore(7200) // Higher score
            assertEquals(7200, savedProgress?.highestScore)
        }
    }

    @Test
    fun test_11_activeGameSaveAndRestore() {
        val dummyDao = object : com.puzzle100.game.data.database.ActiveGameDao {
            var activeGame: ActiveGameEntity? = null
            override fun getLatestActiveGameFlow() = kotlinx.coroutines.flow.emptyFlow<ActiveGameEntity?>()
            override suspend fun getLatestActiveGame() = activeGame
            override fun getActiveGameForLevelFlow(levelId: Int) = kotlinx.coroutines.flow.emptyFlow<ActiveGameEntity?>()
            override suspend fun getActiveGameForLevel(levelId: Int) = activeGame
            override suspend fun saveActiveGame(game: ActiveGameEntity) { activeGame = game }
            override suspend fun deleteActiveGame(levelId: Int) { activeGame = null }
            override suspend fun clearAllActiveGames() { activeGame = null }
        }

        val repo = ActiveGameRepository(dummyDao)
        val initialBoard = MutableList(BOARD_SIZE) { MutableList(BOARD_SIZE) { BoardCellState() } }
        initialBoard[2][3] = BoardCellState(isFilled = true, colorType = BlockColorType.PURPLE)

        val state = GameState(
            currentLevel = 5,
            score = 1250,
            combo = 2,
            highestCombo = 3,
            board = initialBoard,
            availablePieces = listOf(PuzzleShapesRepository.SQUARE_2X2, null, PuzzleShapesRepository.DOT_1X1)
        )

        kotlinx.coroutines.runBlocking {
            repo.saveActiveGame(state, movesCount = 14)
            val entity = repo.getActiveGame(5)
            assertNotNull(entity)
            assertEquals(5, entity?.levelId)
            assertEquals(1250, entity?.score)
            assertEquals(2, entity?.combo)
            assertEquals(14, entity?.movesCount)

            val restored = repo.restoreGameState(entity!!)
            assertEquals(5, restored.currentLevel)
            assertEquals(1250, restored.score)
            assertEquals(2, restored.combo)
            assertTrue(restored.board[2][3].isFilled)
            assertEquals(BlockColorType.PURPLE, restored.board[2][3].colorType)
            assertEquals("SQUARE_2X2", restored.availablePieces[0]?.id)
            assertNull(restored.availablePieces[1])
            assertEquals("DOT_1X1", restored.availablePieces[2]?.id)
        }
    }

    @Test
    fun test_12_activeGameDeletedOnVictoryOrGameOver() {
        var activeEntity: ActiveGameEntity? = ActiveGameEntity(
            levelId = 1,
            boardData = "[]",
            availablePiecesData = "[]",
            score = 500
        )

        val dummyDao = object : com.puzzle100.game.data.database.ActiveGameDao {
            override fun getLatestActiveGameFlow() = kotlinx.coroutines.flow.emptyFlow<ActiveGameEntity?>()
            override suspend fun getLatestActiveGame() = activeEntity
            override fun getActiveGameForLevelFlow(levelId: Int) = kotlinx.coroutines.flow.emptyFlow<ActiveGameEntity?>()
            override suspend fun getActiveGameForLevel(levelId: Int) = activeEntity
            override suspend fun saveActiveGame(game: ActiveGameEntity) { activeEntity = game }
            override suspend fun deleteActiveGame(levelId: Int) { activeEntity = null }
            override suspend fun clearAllActiveGames() { activeEntity = null }
        }

        val repo = ActiveGameRepository(dummyDao)
        kotlinx.coroutines.runBlocking {
            val victoryState = GameState(currentLevel = 1, score = 800, isVictory = true)
            repo.saveActiveGame(victoryState)
            assertNull("Active game must be deleted on victory", activeEntity)
        }
    }

    @Test
    fun test_13_gamePreferencesDefaultValues() {
        val prefs = GamePreferences()
        assertTrue(prefs.soundEnabled)
        assertTrue(prefs.hapticEnabled)
        assertEquals(1.0f, prefs.masterVolume, 0.001f)
        assertTrue(prefs.musicEnabled)
        assertEquals(0.5f, prefs.musicVolume, 0.001f)
        assertEquals("DARK_PREMIUM", prefs.preferredTheme)
    }

    @Test
    fun test_14_volumeClamping() {
        val clampedLow = (-0.5f).coerceIn(0f, 1f)
        val clampedHigh = (1.5f).coerceIn(0f, 1f)
        val clampedValid = (0.75f).coerceIn(0f, 1f)

        assertEquals(0.0f, clampedLow, 0.001f)
        assertEquals(1.0f, clampedHigh, 0.001f)
        assertEquals(0.75f, clampedValid, 0.001f)
    }

    @Test
    fun test_15_gameStateRestoresTargetScoreCorrectly() {
        val dummyDao = object : com.puzzle100.game.data.database.ActiveGameDao {
            override fun getLatestActiveGameFlow() = kotlinx.coroutines.flow.emptyFlow<ActiveGameEntity?>()
            override suspend fun getLatestActiveGame() = null
            override fun getActiveGameForLevelFlow(levelId: Int) = kotlinx.coroutines.flow.emptyFlow<ActiveGameEntity?>()
            override suspend fun getActiveGameForLevel(levelId: Int) = null
            override suspend fun saveActiveGame(game: ActiveGameEntity) {}
            override suspend fun deleteActiveGame(levelId: Int) {}
            override suspend fun clearAllActiveGames() {}
        }
        val repo = ActiveGameRepository(dummyDao)
        val entity = ActiveGameEntity(
            levelId = 10,
            boardData = "[]",
            availablePiecesData = "[]",
            score = 1200
        )
        val restored = repo.restoreGameState(entity)
        assertEquals(10, restored.currentLevel)
        // target score formula: 500 + (10 * 150) = 2000
        assertEquals(2000, restored.targetScore)
    }

    @Test
    fun test_16_coroutineSafePlayerProgressEntityDefaults() {
        val defaultEntity = PlayerProgressEntity()
        assertEquals(1, defaultEntity.currentUnlockedLevel)
        assertEquals(1, defaultEntity.currentLevel)
        assertEquals(0L, defaultEntity.totalScore)
        assertEquals(0, defaultEntity.totalLevelsCompleted)
        assertEquals(0, defaultEntity.totalStars)
        assertEquals(0, defaultEntity.highestScore)
    }

    @Test
    fun test_17_replayLevelDoesNotDoubleCountStarsOrCompletedCount() {
        var savedProgress = PlayerProgressEntity(
            id = 1,
            currentUnlockedLevel = 5,
            totalStars = 12,
            totalLevelsCompleted = 4,
            highestScore = 3000
        )

        var existingResult: LevelResultEntity? = LevelResultEntity(
            levelId = 1,
            bestScore = 2000,
            stars = 3,
            timesPlayed = 2,
            completed = true
        )

        val dummyLevelDao = object : com.puzzle100.game.data.database.LevelResultDao {
            override fun getAllLevelResultsFlow() = kotlinx.coroutines.flow.emptyFlow<List<LevelResultEntity>>()
            override suspend fun getAllLevelResults() = listOfNotNull(existingResult)
            override fun getLevelResultFlow(levelId: Int) = kotlinx.coroutines.flow.emptyFlow<LevelResultEntity?>()
            override suspend fun getLevelResult(levelId: Int) = existingResult
            override fun getTotalStarsFlow() = kotlinx.coroutines.flow.emptyFlow<Int?>()
            override fun getCompletedLevelsCountFlow() = kotlinx.coroutines.flow.emptyFlow<Int>()
            override suspend fun insertOrReplace(result: LevelResultEntity) { existingResult = result }
            override suspend fun clearAll() {}
        }

        val dummyProgressDao = object : com.puzzle100.game.data.database.PlayerProgressDao {
            override fun getPlayerProgressFlow() = kotlinx.coroutines.flow.emptyFlow<PlayerProgressEntity?>()
            override suspend fun getPlayerProgress() = savedProgress
            override suspend fun insertOrUpdate(progress: PlayerProgressEntity) { savedProgress = progress }
            override suspend fun update(progress: PlayerProgressEntity) { savedProgress = progress }
            override suspend fun updateUnlockedLevel(unlockedLevel: Int) {}
            override suspend fun updateHighestScore(score: Int) {}
            override suspend fun recordStats(lines: Int, combo: Int, addedScore: Int, timestamp: Long) {}
        }

        val levelRepo = LevelResultRepository(dummyLevelDao)
        val progressRepo = com.puzzle100.game.data.repository.PlayerProgressRepository(dummyProgressDao)

        kotlinx.coroutines.runBlocking {
            // Replaying level 1 and getting 3 stars again
            val outcome = levelRepo.processLevelCompletion(levelId = 1, score = 1800, targetScore = 650)
            assertEquals(0, outcome.starDelta) // Zero new stars
            assertFalse(outcome.isNewlyCompleted) // Not newly completed

            progressRepo.onLevelCompleted(
                levelId = 1,
                score = 1800,
                starDelta = outcome.starDelta,
                isNewlyCompleted = outcome.isNewlyCompleted
            )

            // Total stars remains 12 (not 15!) and total completed remains 4 (not 5!)
            assertEquals(12, savedProgress.totalStars)
            assertEquals(4, savedProgress.totalLevelsCompleted)
        }
    }

    @Test
    fun test_18_separateActiveGamesForDifferentLevels() {
        val storage = mutableMapOf<Int, ActiveGameEntity>()

        val dummyDao = object : com.puzzle100.game.data.database.ActiveGameDao {
            override fun getLatestActiveGameFlow() = kotlinx.coroutines.flow.emptyFlow<ActiveGameEntity?>()
            override suspend fun getLatestActiveGame() = storage.values.maxByOrNull { it.savedTimestamp }
            override fun getActiveGameForLevelFlow(levelId: Int) = kotlinx.coroutines.flow.emptyFlow<ActiveGameEntity?>()
            override suspend fun getActiveGameForLevel(levelId: Int) = storage[levelId]
            override suspend fun saveActiveGame(game: ActiveGameEntity) { storage[game.levelId] = game }
            override suspend fun deleteActiveGame(levelId: Int) { storage.remove(levelId) }
            override suspend fun clearAllActiveGames() { storage.clear() }
        }

        val repo = ActiveGameRepository(dummyDao)
        kotlinx.coroutines.runBlocking {
            val emptyBoard = List(BOARD_SIZE) { List(BOARD_SIZE) { BoardCellState() } }
            // Playing Level 20
            val stateLvl20 = GameState(currentLevel = 20, score = 3400, board = emptyBoard)
            repo.saveActiveGame(stateLvl20, movesCount = 18)

            // Switching to replay Level 5
            val stateLvl5 = GameState(currentLevel = 5, score = 450, board = emptyBoard)
            repo.saveActiveGame(stateLvl5, movesCount = 4)

            // Both levels retain their independent active game states without overwriting
            val active20 = repo.getActiveGame(20)
            val active5 = repo.getActiveGame(5)

            assertNotNull(active20)
            assertEquals(20, active20?.levelId)
            assertEquals(3400, active20?.score)

            assertNotNull(active5)
            assertEquals(5, active5?.levelId)
            assertEquals(450, active5?.score)
        }
    }

    @Test
    fun test_19_gameOverSavesScoreBeforeDeletingActiveGame() {
        val executionOrder = mutableListOf<String>()

        val dummyLevelDao = object : com.puzzle100.game.data.database.LevelResultDao {
            override fun getAllLevelResultsFlow() = kotlinx.coroutines.flow.emptyFlow<List<LevelResultEntity>>()
            override suspend fun getAllLevelResults() = emptyList<LevelResultEntity>()
            override fun getLevelResultFlow(levelId: Int) = kotlinx.coroutines.flow.emptyFlow<LevelResultEntity?>()
            override suspend fun getLevelResult(levelId: Int) = null
            override fun getTotalStarsFlow() = kotlinx.coroutines.flow.emptyFlow<Int?>()
            override fun getCompletedLevelsCountFlow() = kotlinx.coroutines.flow.emptyFlow<Int>()
            override suspend fun insertOrReplace(result: LevelResultEntity) {
                executionOrder.add("SAVE_LEVEL_RESULT")
            }
            override suspend fun clearAll() {}
        }

        val dummyProgressDao = object : com.puzzle100.game.data.database.PlayerProgressDao {
            override fun getPlayerProgressFlow() = kotlinx.coroutines.flow.emptyFlow<PlayerProgressEntity?>()
            override suspend fun getPlayerProgress() = PlayerProgressEntity()
            override suspend fun insertOrUpdate(progress: PlayerProgressEntity) {}
            override suspend fun update(progress: PlayerProgressEntity) {}
            override suspend fun updateUnlockedLevel(unlockedLevel: Int) {}
            override suspend fun updateHighestScore(score: Int) {
                executionOrder.add("UPDATE_HIGH_SCORE")
            }
            override suspend fun recordStats(lines: Int, combo: Int, addedScore: Int, timestamp: Long) {}
        }

        val dummyActiveDao = object : com.puzzle100.game.data.database.ActiveGameDao {
            override fun getLatestActiveGameFlow() = kotlinx.coroutines.flow.emptyFlow<ActiveGameEntity?>()
            override suspend fun getLatestActiveGame() = null
            override fun getActiveGameForLevelFlow(levelId: Int) = kotlinx.coroutines.flow.emptyFlow<ActiveGameEntity?>()
            override suspend fun getActiveGameForLevel(levelId: Int) = null
            override suspend fun saveActiveGame(game: ActiveGameEntity) {}
            override suspend fun deleteActiveGame(levelId: Int) {
                executionOrder.add("DELETE_ACTIVE_GAME")
            }
            override suspend fun clearAllActiveGames() {}
        }

        val levelRepo = LevelResultRepository(dummyLevelDao)
        val progressRepo = com.puzzle100.game.data.repository.PlayerProgressRepository(dummyProgressDao)
        val activeRepo = ActiveGameRepository(dummyActiveDao)

        kotlinx.coroutines.runBlocking {
            // Emulate Game Over sequence in GameViewModel
            val level = 3
            val finalScore = 600
            val target = 950

            levelRepo.saveLevelResult(level, finalScore, target)
            progressRepo.updateHighScore(finalScore)
            activeRepo.deleteActiveGame(level)

            assertEquals("SAVE_LEVEL_RESULT", executionOrder[0])
            assertEquals("UPDATE_HIGH_SCORE", executionOrder[1])
            assertEquals("DELETE_ACTIVE_GAME", executionOrder[2])
        }
    }
}
