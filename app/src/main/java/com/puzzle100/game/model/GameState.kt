package com.puzzle100.game.model

const val BOARD_SIZE = 10

data class BoardCellState(
    val isFilled: Boolean = false,
    val colorType: BlockColorType? = null
)

data class GhostPreviewState(
    val originRow: Int,
    val originCol: Int,
    val isValid: Boolean,
    val previewCells: List<BlockCell>,
    val colorType: BlockColorType
)

data class ClearAnimationState(
    val clearedRows: List<Int> = emptyList(),
    val clearedCols: List<Int> = emptyList(),
    val timestamp: Long = 0L
) {
    val isActive: Boolean get() = clearedRows.isNotEmpty() || clearedCols.isNotEmpty()
}

data class ScorePopupEffect(
    val addedScore: Int,
    val combo: Int,
    val message: String,
    val timestamp: Long = 0L
)

data class DragState(
    val isDragging: Boolean = false,
    val pieceIndex: Int = -1,
    val piece: PuzzleShape? = null,
    val touchX: Float = 0f,
    val touchY: Float = 0f,
    val hoverRow: Int? = null,
    val hoverCol: Int? = null,
    val isHoverValid: Boolean = false
)

data class GameState(
    val currentLevel: Int = 1,
    val score: Int = 0,
    val targetScore: Int = 650,
    val combo: Int = 0,
    val highestCombo: Int = 0,
    val totalClearedRows: Int = 0,
    val totalClearedCols: Int = 0,
    val board: List<List<BoardCellState>> = List(BOARD_SIZE) {
        List(BOARD_SIZE) { BoardCellState() }
    },
    val availablePieces: List<PuzzleShape?> = listOf(
        PuzzleShapesRepository.SQUARE_2X2,
        PuzzleShapesRepository.LINE_3H,
        PuzzleShapesRepository.L_SMALL_BL
    ),
    val dragState: DragState = DragState(),
    val ghostPreview: GhostPreviewState? = null,
    val clearAnimation: ClearAnimationState = ClearAnimationState(),
    val scorePopup: ScorePopupEffect? = null,
    val isGameOver: Boolean = false,
    val isVictory: Boolean = false,
    val isPaused: Boolean = false
) {
    val progress: Float
        get() = (score.toFloat() / targetScore.toFloat()).coerceIn(0f, 1f)

    val remainingPiecesCount: Int
        get() = availablePieces.count { it != null }
}
