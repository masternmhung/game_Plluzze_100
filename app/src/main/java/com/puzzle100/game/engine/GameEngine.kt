package com.puzzle100.game.engine

import com.puzzle100.game.model.BOARD_SIZE
import com.puzzle100.game.model.BoardCellState
import com.puzzle100.game.model.PuzzleShape
import com.puzzle100.game.model.PuzzleShapesRepository
import kotlin.random.Random

data class LineClearResult(
    val clearedRows: List<Int>,
    val clearedCols: List<Int>
) {
    val totalLines: Int get() = clearedRows.size + clearedCols.size
    val hasClears: Boolean get() = totalLines > 0
}

data class ScoreBreakdown(
    val placementScore: Int,
    val lineClearScore: Int,
    val multiLineBonus: Int,
    val comboBonus: Int,
    val totalAdded: Int,
    val newCombo: Int
)

/**
 * Isolated scoring engine for easy balancing and testing
 */
object ScoreEvaluator {
    const val POINTS_PER_CELL = 10
    const val POINTS_PER_LINE = 100
    const val POINTS_PER_COMBO_LEVEL = 50

    fun calculatePlacementScore(piece: PuzzleShape): Int {
        return piece.cellCount * POINTS_PER_CELL
    }

    fun calculateMultiLineBonus(totalLines: Int): Int {
        return when (totalLines) {
            0, 1 -> 0
            2 -> 100
            3 -> 300
            4 -> 600
            else -> 600 + (totalLines - 4) * 400
        }
    }

    fun evaluateTurn(
        placedPiece: PuzzleShape,
        linesResult: LineClearResult,
        previousCombo: Int
    ): ScoreBreakdown {
        val placementScore = calculatePlacementScore(placedPiece)
        val lineCount = linesResult.totalLines

        if (lineCount == 0) {
            return ScoreBreakdown(
                placementScore = placementScore,
                lineClearScore = 0,
                multiLineBonus = 0,
                comboBonus = 0,
                totalAdded = placementScore,
                newCombo = 0
            )
        }

        val nextCombo = previousCombo + 1
        val lineClearScore = lineCount * POINTS_PER_LINE
        val multiBonus = calculateMultiLineBonus(lineCount)
        val comboBonus = nextCombo * POINTS_PER_COMBO_LEVEL
        val total = placementScore + lineClearScore + multiBonus + comboBonus

        return ScoreBreakdown(
            placementScore = placementScore,
            lineClearScore = lineClearScore,
            multiLineBonus = multiBonus,
            comboBonus = comboBonus,
            totalAdded = total,
            newCombo = nextCombo
        )
    }
}

/**
 * Pure Game Engine containing all core logic independent of Android UI/Compose
 */
object GameEngine {

    /**
     * Checks if a piece can be legally placed at the given board position.
     * @param piece The shape to be placed
     * @param originRow Top-left anchor row on 10x10 board
     * @param originCol Top-left anchor col on 10x10 board
     * @param board The current 10x10 board state
     */
    fun canPlacePiece(
        piece: PuzzleShape,
        originRow: Int,
        originCol: Int,
        board: List<List<BoardCellState>>
    ): Boolean {
        for (cell in piece.occupiedCells) {
            val targetRow = originRow + cell.row
            val targetCol = originCol + cell.col

            // 1. Boundary check
            if (targetRow !in 0 until BOARD_SIZE || targetCol !in 0 until BOARD_SIZE) {
                return false
            }

            // 2. Overlap check
            if (board[targetRow][targetCol].isFilled) {
                return false
            }
        }
        return true
    }

    /**
     * Places the piece onto the board and returns a new immutable board state.
     */
    fun placePiece(
        piece: PuzzleShape,
        originRow: Int,
        originCol: Int,
        board: List<List<BoardCellState>>
    ): List<List<BoardCellState>> {
        require(canPlacePiece(piece, originRow, originCol, board)) {
            "Cannot place piece ${piece.id} at ($originRow, $originCol)"
        }

        val newBoard = board.map { row -> row.toMutableList() }
        for (cell in piece.occupiedCells) {
            val r = originRow + cell.row
            val c = originCol + cell.col
            newBoard[r][c] = BoardCellState(isFilled = true, colorType = piece.colorType)
        }
        return newBoard.map { it.toList() }
    }

    /**
     * Scans all 10 rows and 10 columns to identify completed lines.
     */
    fun detectLinesToClear(board: List<List<BoardCellState>>): LineClearResult {
        val clearedRows = mutableListOf<Int>()
        val clearedCols = mutableListOf<Int>()

        // Check rows
        for (r in 0 until BOARD_SIZE) {
            if (board[r].all { it.isFilled }) {
                clearedRows.add(r)
            }
        }

        // Check columns
        for (c in 0 until BOARD_SIZE) {
            var colFilled = true
            for (r in 0 until BOARD_SIZE) {
                if (!board[r][c].isFilled) {
                    colFilled = false
                    break
                }
            }
            if (colFilled) {
                clearedCols.add(c)
            }
        }

        return LineClearResult(clearedRows, clearedCols)
    }

    /**
     * Clears specified rows and columns simultaneously.
     * Intersections are cleared cleanly once.
     */
    fun clearLines(
        board: List<List<BoardCellState>>,
        clearedRows: List<Int>,
        clearedCols: List<Int>
    ): List<List<BoardCellState>> {
        if (clearedRows.isEmpty() && clearedCols.isEmpty()) return board

        val newBoard = board.map { it.toMutableList() }
        for (r in 0 until BOARD_SIZE) {
            for (c in 0 until BOARD_SIZE) {
                if (r in clearedRows || c in clearedCols) {
                    newBoard[r][c] = BoardCellState(isFilled = false, colorType = null)
                }
            }
        }
        return newBoard.map { it.toList() }
    }

    /**
     * Checks if a piece can be placed anywhere on the 10x10 board.
     */
    fun canPlaceAnywhere(
        piece: PuzzleShape,
        board: List<List<BoardCellState>>
    ): Boolean {
        for (r in 0 until BOARD_SIZE) {
            for (c in 0 until BOARD_SIZE) {
                if (canPlacePiece(piece, r, c, board)) {
                    return true
                }
            }
        }
        return false
    }

    /**
     * Checks if the game is over: none of the remaining pieces in the tray can fit anywhere.
     */
    fun isGameOver(
        availablePieces: List<PuzzleShape?>,
        board: List<List<BoardCellState>>
    ): Boolean {
        val remaining = availablePieces.filterNotNull()
        if (remaining.isEmpty()) return false
        return remaining.none { piece -> canPlaceAnywhere(piece, board) }
    }

    /**
     * Spawns 3 random pieces with balanced difficulty distribution.
     * GUARANTEE: Never spawns 3 pieces where none can fit if space exists on board.
     */
    fun spawnPieces(
        board: List<List<BoardCellState>>,
        level: Int = 1,
        random: Random = Random.Default
    ): List<PuzzleShape> {
        val pool = PuzzleShapesRepository.ALL_SHAPES
        val smallPool = PuzzleShapesRepository.SMALL_SHAPES
        val mediumPool = PuzzleShapesRepository.MEDIUM_SHAPES
        val largePool = PuzzleShapesRepository.LARGE_SHAPES

        val result = mutableListOf<PuzzleShape>()

        // Piece 1: Always guarantee a small to medium piece
        val piece1 = smallPool.random(random)
        result.add(piece1)

        // Piece 2: Medium piece
        val piece2 = (if (level > 25) mediumPool else smallPool).random(random)
        result.add(piece2)

        // Piece 3: Large or Medium based on level
        val pool3 = when {
            level >= 50 -> largePool + mediumPool
            level >= 20 -> mediumPool
            else -> smallPool + mediumPool
        }
        val piece3 = pool3.random(random)
        result.add(piece3)

        // SAFETY VERIFICATION: Ensure at least 1 piece is placable on the current board
        val hasPlacable = result.any { canPlaceAnywhere(it, board) }
        if (!hasPlacable) {
            // Find a piece from smallPool that fits on board
            val fittingPiece = smallPool.firstOrNull { canPlaceAnywhere(it, board) }
                ?: PuzzleShapesRepository.DOT_1X1
            result[0] = fittingPiece
        }

        return result
    }
}
