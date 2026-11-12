package com.puzzle100.game.ui.screens.game

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.puzzle100.game.data.database.ActiveGameEntity
import com.puzzle100.game.data.database.GameDatabase
import com.puzzle100.game.data.preferences.GamePreferencesRepository
import com.puzzle100.game.data.repository.ActiveGameRepository
import com.puzzle100.game.data.repository.LevelResultRepository
import com.puzzle100.game.data.repository.PlayerProgressRepository
import com.puzzle100.game.engine.GameEngine
import com.puzzle100.game.engine.ScoreEvaluator
import com.puzzle100.game.model.BOARD_SIZE
import com.puzzle100.game.model.BlockCell
import com.puzzle100.game.model.BoardCellState
import com.puzzle100.game.model.ClearAnimationState
import com.puzzle100.game.model.DragState
import com.puzzle100.game.model.GameState
import com.puzzle100.game.model.GhostPreviewState
import com.puzzle100.game.model.PuzzleShape
import com.puzzle100.game.model.ScorePopupEffect
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class GameEffectEvent {
    object Pickup : GameEffectEvent()
    object Place : GameEffectEvent()
    object InvalidMove : GameEffectEvent()
    object LineClear : GameEffectEvent()
    data class Combo(val level: Int) : GameEffectEvent()
    object GameOver : GameEffectEvent()
    object LevelComplete : GameEffectEvent()
}

data class GameSettingsState(
    val isSoundEnabled: Boolean = true,
    val isHapticEnabled: Boolean = true,
    val masterVolume: Float = 1.0f,
    val isSettingsOpen: Boolean = false
)

class GameViewModel(
    private var activeGameRepository: ActiveGameRepository? = null,
    private var levelResultRepository: LevelResultRepository? = null,
    private var playerProgressRepository: PlayerProgressRepository? = null,
    private var preferencesRepository: GamePreferencesRepository? = null
) : ViewModel() {

    private val _gameState = MutableStateFlow(createInitialGameState(1))
    val gameState: StateFlow<GameState> = _gameState.asStateFlow()

    private val _settingsState = MutableStateFlow(GameSettingsState())
    val settingsState: StateFlow<GameSettingsState> = _settingsState.asStateFlow()

    private var movesCount = 0

    // One-shot side-effect event flow for decoupled audio & haptic triggering
    private val _effectsFlow = MutableSharedFlow<GameEffectEvent>(
        extraBufferCapacity = 16,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val effectsFlow: SharedFlow<GameEffectEvent> = _effectsFlow.asSharedFlow()

    init {
        observePreferences()
    }

    fun attachContext(context: Context) {
        if (preferencesRepository == null || activeGameRepository == null) {
            val db = GameDatabase.getInstance(context)
            activeGameRepository = activeGameRepository ?: ActiveGameRepository(db.activeGameDao())
            levelResultRepository = levelResultRepository ?: LevelResultRepository(db.levelResultDao())
            playerProgressRepository = playerProgressRepository ?: PlayerProgressRepository(db.playerProgressDao())
            preferencesRepository = preferencesRepository ?: GamePreferencesRepository.getInstance(context)
            observePreferences()
        }
    }

    private fun observePreferences() {
        preferencesRepository?.let { repo ->
            viewModelScope.launch {
                repo.gamePreferencesFlow.collect { prefs ->
                    _settingsState.update {
                        it.copy(
                            isSoundEnabled = prefs.soundEnabled,
                            isHapticEnabled = prefs.hapticEnabled,
                            masterVolume = prefs.masterVolume
                        )
                    }
                }
            }
        }
    }

    fun initLevel(levelNumber: Int, forceNewGame: Boolean = false) {
        viewModelScope.launch {
            if (!forceNewGame && activeGameRepository != null) {
                val activeGame = activeGameRepository?.getActiveGame(levelNumber)
                if (activeGame != null) {
                    val restoredState = activeGameRepository!!.restoreGameState(activeGame)
                    movesCount = activeGame.movesCount
                    _gameState.value = restoredState
                    return@launch
                }
            }
            movesCount = 0
            _gameState.value = createInitialGameState(levelNumber)
        }
    }

    fun resumeGame(activeGame: ActiveGameEntity) {
        val repo = activeGameRepository ?: return
        val restored = repo.restoreGameState(activeGame)
        movesCount = activeGame.movesCount
        _gameState.value = restored
    }

    fun restartGame() {
        val level = _gameState.value.currentLevel
        viewModelScope.launch {
            activeGameRepository?.deleteActiveGame(level)
            initLevel(level, forceNewGame = true)
        }
    }

    fun togglePause() {
        _gameState.update { it.copy(isPaused = !it.isPaused) }
        val current = _gameState.value
        if (!current.isGameOver && !current.isVictory) {
            viewModelScope.launch {
                activeGameRepository?.saveActiveGame(current, movesCount)
            }
        }
    }

    fun toggleSettingsDialog(isOpen: Boolean) {
        _settingsState.update { it.copy(isSettingsOpen = isOpen) }
    }

    fun setSoundEnabled(enabled: Boolean) {
        _settingsState.update { it.copy(isSoundEnabled = enabled) }
        viewModelScope.launch {
            preferencesRepository?.updateSoundEnabled(enabled)
        }
    }

    fun setHapticEnabled(enabled: Boolean) {
        _settingsState.update { it.copy(isHapticEnabled = enabled) }
        viewModelScope.launch {
            preferencesRepository?.updateHapticEnabled(enabled)
        }
    }

    fun setMasterVolume(volume: Float) {
        val clamped = volume.coerceIn(0f, 1f)
        _settingsState.update { it.copy(masterVolume = clamped) }
        viewModelScope.launch {
            preferencesRepository?.updateMasterVolume(clamped)
        }
    }

    fun saveSessionOnExit() {
        val current = _gameState.value
        if (!current.isGameOver && !current.isVictory && current.score > 0) {
            viewModelScope.launch {
                activeGameRepository?.saveActiveGame(current, movesCount)
            }
        }
    }

    /**
     * User starts dragging a piece from tray (index 0, 1, or 2)
     */
    fun onDragStarted(pieceIndex: Int) {
        val pieces = _gameState.value.availablePieces
        val piece = pieces.getOrNull(pieceIndex) ?: return

        _gameState.update { current ->
            current.copy(
                dragState = DragState(
                    isDragging = true,
                    pieceIndex = pieceIndex,
                    piece = piece,
                    touchX = 0f,
                    touchY = 0f
                ),
                ghostPreview = null
            )
        }

        // Trigger PICKUP sound & haptic once
        _effectsFlow.tryEmit(GameEffectEvent.Pickup)
    }

    /**
     * Updates drag position and recalculates ghost preview based on board relative coordinates.
     * Note: Does NOT trigger audio, haptics, or database writes during drag movement.
     */
    fun onDragMoved(
        touchX: Float,
        touchY: Float,
        boardLeft: Float,
        boardTop: Float,
        boardSizePx: Float,
        fingerOffsetY: Float = 100f
    ) {
        val currentDrag = _gameState.value.dragState
        val piece = currentDrag.piece ?: return
        if (!currentDrag.isDragging) return

        val cellSize = boardSizePx / BOARD_SIZE.toFloat()
        val adjustedTouchY = touchY - fingerOffsetY

        val pieceWidthPx = piece.width * cellSize
        val pieceHeightPx = piece.height * cellSize

        val relX = touchX - boardLeft - (pieceWidthPx / 2f)
        val relY = adjustedTouchY - boardTop - (pieceHeightPx / 2f)

        val col = kotlin.math.round(relX / cellSize).toInt()
        val row = kotlin.math.round(relY / cellSize).toInt()

        val isWithinBoardZone = row in -1 until BOARD_SIZE && col in -1 until BOARD_SIZE

        if (isWithinBoardZone) {
            val isValid = GameEngine.canPlacePiece(piece, row, col, _gameState.value.board)
            val previewCells = piece.occupiedCells.map { BlockCell(row + it.row, col + it.col) }

            _gameState.update { current ->
                current.copy(
                    dragState = currentDrag.copy(
                        touchX = touchX,
                        touchY = touchY,
                        hoverRow = row,
                        hoverCol = col,
                        isHoverValid = isValid
                    ),
                    ghostPreview = GhostPreviewState(
                        originRow = row,
                        originCol = col,
                        isValid = isValid,
                        previewCells = previewCells,
                        colorType = piece.colorType
                    )
                )
            }
        } else {
            _gameState.update { current ->
                current.copy(
                    dragState = currentDrag.copy(
                        touchX = touchX,
                        touchY = touchY,
                        hoverRow = null,
                        hoverCol = null,
                        isHoverValid = false
                    ),
                    ghostPreview = null
                )
            }
        }
    }

    /**
     * User releases the finger: attempt placement or snap back to tray
     */
    fun onDragEnded() {
        val currentDrag = _gameState.value.dragState
        val piece = currentDrag.piece
        val pieceIndex = currentDrag.pieceIndex
        val hoverRow = currentDrag.hoverRow
        val hoverCol = currentDrag.hoverCol

        if (piece != null && hoverRow != null && hoverCol != null && currentDrag.isHoverValid) {
            placePiece(piece, pieceIndex, hoverRow, hoverCol)
        } else {
            // Cancel / Snap back to tray safely - piece is never lost
            _gameState.update { current ->
                current.copy(
                    dragState = DragState(),
                    ghostPreview = null
                )
            }
            // Trigger INVALID_MOVE sound & haptic once
            _effectsFlow.tryEmit(GameEffectEvent.InvalidMove)
        }
    }

    fun onDragCancelled() {
        _gameState.update { current ->
            current.copy(
                dragState = DragState(),
                ghostPreview = null
            )
        }
        _effectsFlow.tryEmit(GameEffectEvent.InvalidMove)
    }

    /**
     * Executes piece placement, line clearing, score & combo calculation,
     * tray restocking, and game over check with atomic state updates.
     * Persists game state safely only on this discrete event.
     */
    fun placePiece(
        piece: PuzzleShape,
        pieceIndex: Int,
        row: Int,
        col: Int
    ) {
        movesCount++
        var stateToPersist: GameState? = null

        _gameState.update { current ->
            val board = current.board

            if (!GameEngine.canPlacePiece(piece, row, col, board)) {
                _effectsFlow.tryEmit(GameEffectEvent.InvalidMove)
                return@update current.copy(
                    dragState = DragState(),
                    ghostPreview = null
                )
            }

            // 1. Place piece on board
            val boardAfterPlacement = GameEngine.placePiece(piece, row, col, board)

            // 2. Consume piece from tray
            val updatedPieces = current.availablePieces.toMutableList()
            if (pieceIndex in updatedPieces.indices) {
                updatedPieces[pieceIndex] = null
            }

            // 3. Detect line clears
            val clearResult = GameEngine.detectLinesToClear(boardAfterPlacement)

            // 4. Calculate score & combo
            val scoreBreakdown = ScoreEvaluator.evaluateTurn(
                placedPiece = piece,
                linesResult = clearResult,
                previousCombo = current.combo
            )

            val newScore = current.score + scoreBreakdown.totalAdded
            val isVictory = newScore >= current.targetScore

            // 5. Apply line clearing to board
            val finalBoard = if (clearResult.hasClears) {
                GameEngine.clearLines(boardAfterPlacement, clearResult.clearedRows, clearResult.clearedCols)
            } else {
                boardAfterPlacement
            }

            // 6. Spawn new pieces ONLY when tray is completely empty (all 3 used)
            val finalPieces = if (updatedPieces.all { it == null }) {
                GameEngine.spawnPieces(finalBoard, current.currentLevel)
            } else {
                updatedPieces
            }

            // 7. Check Game Over
            val isGameOver = GameEngine.isGameOver(finalPieces, finalBoard)

            // Emit distinct events
            _effectsFlow.tryEmit(GameEffectEvent.Place)

            if (clearResult.hasClears) {
                _effectsFlow.tryEmit(GameEffectEvent.LineClear)
                if (scoreBreakdown.newCombo >= 2) {
                    _effectsFlow.tryEmit(GameEffectEvent.Combo(scoreBreakdown.newCombo))
                }
            }

            if (isVictory) {
                _effectsFlow.tryEmit(GameEffectEvent.LevelComplete)
            } else if (isGameOver) {
                _effectsFlow.tryEmit(GameEffectEvent.GameOver)
            }

            // Create score popup message
            val popupMsg = when {
                scoreBreakdown.newCombo > 1 -> "COMBO x${scoreBreakdown.newCombo}! +${scoreBreakdown.totalAdded}"
                clearResult.totalLines > 1 -> "${clearResult.totalLines} HÀNG! +${scoreBreakdown.totalAdded}"
                clearResult.totalLines == 1 -> "XÓA HÀNG! +${scoreBreakdown.totalAdded}"
                else -> "+${scoreBreakdown.placementScore}"
            }

            val popupEffect = ScorePopupEffect(
                addedScore = scoreBreakdown.totalAdded,
                combo = scoreBreakdown.newCombo,
                message = popupMsg,
                timestamp = System.currentTimeMillis()
            )

            val animState = if (clearResult.hasClears) {
                ClearAnimationState(
                    clearedRows = clearResult.clearedRows,
                    clearedCols = clearResult.clearedCols,
                    timestamp = System.currentTimeMillis()
                )
            } else {
                ClearAnimationState()
            }

            val updated = current.copy(
                board = finalBoard,
                score = newScore,
                combo = scoreBreakdown.newCombo,
                highestCombo = maxOf(current.highestCombo, scoreBreakdown.newCombo),
                totalClearedRows = current.totalClearedRows + clearResult.clearedRows.size,
                totalClearedCols = current.totalClearedCols + clearResult.clearedCols.size,
                availablePieces = finalPieces,
                dragState = DragState(),
                ghostPreview = null,
                clearAnimation = animState,
                scorePopup = popupEffect,
                isVictory = isVictory,
                isGameOver = isGameOver
            )

            stateToPersist = updated
            updated
        }

        // Discrete persistence handling outside Compose render pipeline
        stateToPersist?.let { state ->
            val level = state.currentLevel
            val score = state.score
            val target = state.targetScore

            viewModelScope.launch {
                if (state.isVictory) {
                    val outcome = levelResultRepository?.processLevelCompletion(level, score, target)
                    val starDelta = outcome?.starDelta ?: 0
                    val isNewlyCompleted = outcome?.isNewlyCompleted ?: true
                    playerProgressRepository?.onLevelCompleted(level, score, starDelta, isNewlyCompleted)
                    activeGameRepository?.deleteActiveGame(level)
                } else if (state.isGameOver) {
                    levelResultRepository?.saveLevelResult(level, score, target)
                    playerProgressRepository?.updateHighScore(score)
                    activeGameRepository?.deleteActiveGame(level)
                } else {
                    activeGameRepository?.saveActiveGame(state, movesCount)
                    playerProgressRepository?.recordStats(
                        linesCleared = state.totalClearedRows + state.totalClearedCols,
                        combo = state.combo,
                        addedScore = state.score
                    )
                }
            }
        }

        // Auto-dismiss line flash animation
        if (_gameState.value.clearAnimation.isActive) {
            viewModelScope.launch {
                delay(400L)
                _gameState.update { it.copy(clearAnimation = ClearAnimationState()) }
            }
        }
    }

    private fun createInitialGameState(levelNumber: Int): GameState {
        val target = 500 + (levelNumber * 150)
        val emptyBoard = List(BOARD_SIZE) {
            List(BOARD_SIZE) { BoardCellState() }
        }
        val initialPieces = GameEngine.spawnPieces(emptyBoard, levelNumber)

        return GameState(
            currentLevel = levelNumber,
            score = 0,
            targetScore = target,
            board = emptyBoard,
            availablePieces = initialPieces
        )
    }
}
