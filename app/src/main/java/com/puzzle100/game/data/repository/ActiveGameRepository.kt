package com.puzzle100.game.data.repository

import com.puzzle100.game.data.database.ActiveGameDao
import com.puzzle100.game.data.database.ActiveGameEntity
import com.puzzle100.game.data.database.Converters
import com.puzzle100.game.model.GameState
import kotlinx.coroutines.flow.Flow

class ActiveGameRepository(
    private val activeGameDao: ActiveGameDao,
    private val converters: Converters = Converters()
) {

    val latestActiveGameFlow: Flow<ActiveGameEntity?> = activeGameDao.getLatestActiveGameFlow()

    fun getActiveGameForLevelFlow(levelId: Int): Flow<ActiveGameEntity?> =
        activeGameDao.getActiveGameForLevelFlow(levelId)

    suspend fun getLatestActiveGame(): ActiveGameEntity? {
        return activeGameDao.getLatestActiveGame()
    }

    suspend fun getActiveGame(levelId: Int): ActiveGameEntity? {
        return activeGameDao.getActiveGameForLevel(levelId)
    }

    /**
     * Persists active game state. Only called on discrete gameplay events
     * (e.g. piece placed, line clear, pause, screen exit) — NEVER during drag frames!
     */
    suspend fun saveActiveGame(gameState: GameState, movesCount: Int = 0) {
        // Do not persist game over or victory as active sessions
        if (gameState.isGameOver || gameState.isVictory) {
            activeGameDao.deleteActiveGame(gameState.currentLevel)
            return
        }

        val boardJson = converters.fromBoard(gameState.board)
        val piecesJson = converters.fromPiecesList(gameState.availablePieces)

        val entity = ActiveGameEntity(
            levelId = gameState.currentLevel,
            boardData = boardJson,
            availablePiecesData = piecesJson,
            score = gameState.score,
            combo = gameState.combo,
            highestCombo = gameState.highestCombo,
            totalRowsCleared = gameState.totalClearedRows,
            totalColumnsCleared = gameState.totalClearedCols,
            movesCount = movesCount,
            gameStatus = if (gameState.isPaused) "PAUSED" else "PLAYING",
            savedTimestamp = System.currentTimeMillis()
        )

        activeGameDao.saveActiveGame(entity)
    }

    suspend fun deleteActiveGame(levelId: Int) {
        activeGameDao.deleteActiveGame(levelId)
    }

    suspend fun clearAllActiveGames() {
        activeGameDao.clearAllActiveGames()
    }

    /**
     * Restores a GameState from an ActiveGameEntity
     */
    fun restoreGameState(entity: ActiveGameEntity): GameState {
        val board = converters.toBoard(entity.boardData)
        val pieces = converters.toPiecesList(entity.availablePiecesData)
        val target = 500 + (entity.levelId * 150)

        return GameState(
            currentLevel = entity.levelId,
            score = entity.score,
            targetScore = target,
            combo = entity.combo,
            highestCombo = entity.highestCombo,
            totalClearedRows = entity.totalRowsCleared,
            totalClearedCols = entity.totalColumnsCleared,
            board = board,
            availablePieces = pieces,
            isPaused = entity.gameStatus == "PAUSED",
            isGameOver = false,
            isVictory = false
        )
    }
}
