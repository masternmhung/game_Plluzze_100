package com.puzzle100.game

import com.puzzle100.game.engine.GameEngine
import com.puzzle100.game.engine.LineClearResult
import com.puzzle100.game.engine.ScoreEvaluator
import com.puzzle100.game.model.BOARD_SIZE
import com.puzzle100.game.model.BlockColorType
import com.puzzle100.game.model.BoardCellState
import com.puzzle100.game.model.PuzzleShapesRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import kotlin.random.Random

/**
 * Phase 2 Unit Tests covering all 12 core Game Engine requirements:
 * 1. Kiểm tra vị trí đặt quân hợp lệ
 * 2. Kiểm tra vị trí ngoài bảng
 * 3. Kiểm tra quân chồng lên nhau
 * 4. Đặt quân thành công
 * 5. Xóa một hàng
 * 6. Xóa một cột
 * 7. Xóa nhiều hàng và cột cùng lúc
 * 8. Tính điểm
 * 9. Tính combo
 * 10. Sinh 3 quân ngẫu nhiên
 * 11. Kiểm tra khả năng đặt quân
 * 12. Xác định Game Over
 */
class GameEngineTest {

    private lateinit var emptyBoard: List<List<BoardCellState>>

    @Before
    fun setUp() {
        emptyBoard = List(BOARD_SIZE) {
            List(BOARD_SIZE) { BoardCellState(isFilled = false, colorType = null) }
        }
    }

    // 1. Kiểm tra vị trí đặt quân hợp lệ
    @Test
    fun testValidPlacement() {
        val squarePiece = PuzzleShapesRepository.SQUARE_2X2
        val canPlace = GameEngine.canPlacePiece(squarePiece, 0, 0, emptyBoard)
        assertTrue("Square 2x2 should be placeable at (0, 0)", canPlace)

        val canPlaceMiddle = GameEngine.canPlacePiece(squarePiece, 4, 4, emptyBoard)
        assertTrue("Square 2x2 should be placeable at (4, 4)", canPlaceMiddle)
    }

    // 2. Kiểm tra vị trí ngoài bảng
    @Test
    fun testOutOfBoundsPlacement() {
        val squarePiece = PuzzleShapesRepository.SQUARE_2X2 // 2 rows x 2 cols
        // Placed at (9, 9) would require cells at (9,9), (9,10), (10,9), (10,10) which exceed 10x10
        val canPlaceAtCorner = GameEngine.canPlacePiece(squarePiece, 9, 9, emptyBoard)
        assertFalse("Square 2x2 should NOT be placeable at bottom-right out of bounds (9, 9)", canPlaceAtCorner)

        val line5H = PuzzleShapesRepository.LINE_5H // 1 row x 5 cols
        val canPlaceLineOffEdge = GameEngine.canPlacePiece(line5H, 0, 6, emptyBoard)
        assertFalse("Line 5H at col 6 extends to col 10 (out of bounds)", canPlaceLineOffEdge)

        val negativeCoord = GameEngine.canPlacePiece(squarePiece, -1, 0, emptyBoard)
        assertFalse("Negative coordinate must be rejected", negativeCoord)
    }

    // 3. Kiểm tra quân chồng lên nhau
    @Test
    fun testOverlappingPlacement() {
        val squarePiece = PuzzleShapesRepository.SQUARE_2X2
        // Place piece at (2, 2)
        val boardWithSquare = GameEngine.placePiece(squarePiece, 2, 2, emptyBoard)

        // Try placing another piece that overlaps with (2, 2)
        val dot = PuzzleShapesRepository.DOT_1X1
        val canPlaceOverlapping = GameEngine.canPlacePiece(dot, 2, 2, boardWithSquare)
        assertFalse("Cannot place piece on an already occupied cell", canPlaceOverlapping)

        val canPlaceOverlapping2 = GameEngine.canPlacePiece(squarePiece, 1, 1, boardWithSquare)
        assertFalse("Piece overlapping partially with (2,2) must be rejected", canPlaceOverlapping2)
    }

    // 4. Đặt quân thành công
    @Test
    fun testSuccessfulPlacement() {
        val line3H = PuzzleShapesRepository.LINE_3H
        val newBoard = GameEngine.placePiece(line3H, 3, 4, emptyBoard)

        // Verify the 3 target cells are now filled with the piece's color
        assertTrue(newBoard[3][4].isFilled)
        assertTrue(newBoard[3][5].isFilled)
        assertTrue(newBoard[3][6].isFilled)
        assertEquals(line3H.colorType, newBoard[3][4].colorType)

        // Verify unaffected cells remain empty
        assertFalse(newBoard[3][3].isFilled)
        assertFalse(newBoard[3][7].isFilled)
        assertFalse(newBoard[4][4].isFilled)
    }

    // 5. Xóa một hàng
    @Test
    fun testSingleRowClear() {
        // Fill 9 cells in row 4
        var board = emptyBoard
        val line5 = PuzzleShapesRepository.LINE_5H
        val line4 = PuzzleShapesRepository.LINE_4H
        val dot = PuzzleShapesRepository.DOT_1X1

        board = GameEngine.placePiece(line5, 4, 0, board)
        board = GameEngine.placePiece(line4, 4, 5, board)

        // Before completing row 4
        var clearResult = GameEngine.detectLinesToClear(board)
        assertEquals(0, clearResult.clearedRows.size)

        // Place final dot at (4, 9) to complete the row
        board = GameEngine.placePiece(dot, 4, 9, board)
        clearResult = GameEngine.detectLinesToClear(board)

        assertEquals("Row 4 should be marked for clear", listOf(4), clearResult.clearedRows)
        assertEquals(0, clearResult.clearedCols.size)

        // Clear row
        val boardAfterClear = GameEngine.clearLines(board, clearResult.clearedRows, clearResult.clearedCols)
        for (col in 0 until BOARD_SIZE) {
            assertFalse("Cell (4, $col) should be cleared", boardAfterClear[4][col].isFilled)
        }
    }

    // 6. Xóa một cột
    @Test
    fun testSingleColumnClear() {
        var board = emptyBoard
        val line5V = PuzzleShapesRepository.LINE_5V

        // Place two 5V lines to fill column 2
        board = GameEngine.placePiece(line5V, 0, 2, board)
        board = GameEngine.placePiece(line5V, 5, 2, board)

        val clearResult = GameEngine.detectLinesToClear(board)
        assertEquals("Column 2 should be marked for clear", listOf(2), clearResult.clearedCols)
        assertEquals(0, clearResult.clearedRows.size)

        val boardAfterClear = GameEngine.clearLines(board, clearResult.clearedRows, clearResult.clearedCols)
        for (row in 0 until BOARD_SIZE) {
            assertFalse("Cell ($row, 2) should be cleared", boardAfterClear[row][2].isFilled)
        }
    }

    // 7. Xóa nhiều hàng và cột cùng lúc (giao nhau)
    @Test
    fun testSimultaneousMultipleRowAndColClear() {
        // Setup row 5 and col 5 almost full, intersecting at (5, 5)
        var board = emptyBoard
        val dot = PuzzleShapesRepository.DOT_1X1

        for (i in 0 until BOARD_SIZE) {
            if (i != 5) {
                board = GameEngine.placePiece(dot, 5, i, board) // Fill row 5 except col 5
                board = GameEngine.placePiece(dot, i, 5, board) // Fill col 5 except row 5
            }
        }

        // Place the pivot dot at intersection (5, 5)
        board = GameEngine.placePiece(dot, 5, 5, board)

        val clearResult = GameEngine.detectLinesToClear(board)
        assertEquals(listOf(5), clearResult.clearedRows)
        assertEquals(listOf(5), clearResult.clearedCols)
        assertEquals(2, clearResult.totalLines)

        // Clear both row 5 and col 5
        val boardAfterClear = GameEngine.clearLines(board, clearResult.clearedRows, clearResult.clearedCols)

        // Intersection (5, 5) must be empty without error
        assertFalse(boardAfterClear[5][5].isFilled)
        for (i in 0 until BOARD_SIZE) {
            assertFalse(boardAfterClear[5][i].isFilled)
            assertFalse(boardAfterClear[i][5].isFilled)
        }
    }

    // 8. Tính điểm (Score Calculation)
    @Test
    fun testScoreCalculation() {
        val square = PuzzleShapesRepository.SQUARE_2X2 // 4 cells
        // Placement only (0 lines)
        val scoreNoLines = ScoreEvaluator.evaluateTurn(square, LineClearResult(emptyList(), emptyList()), 0)
        assertEquals(40, scoreNoLines.placementScore) // 4 cells * 10
        assertEquals(0, scoreNoLines.lineClearScore)
        assertEquals(40, scoreNoLines.totalAdded)
        assertEquals(0, scoreNoLines.newCombo)

        // 1 line clear with previous combo 0
        val scoreOneLine = ScoreEvaluator.evaluateTurn(square, LineClearResult(listOf(3), emptyList()), 0)
        assertEquals(40, scoreOneLine.placementScore)
        assertEquals(100, scoreOneLine.lineClearScore)
        assertEquals(0, scoreOneLine.multiLineBonus)
        assertEquals(50, scoreOneLine.comboBonus) // Combo 1 * 50
        assertEquals(190, scoreOneLine.totalAdded) // 40 + 100 + 0 + 50

        // 2 lines clear bonus
        val scoreTwoLines = ScoreEvaluator.evaluateTurn(square, LineClearResult(listOf(1, 2), emptyList()), 0)
        assertEquals(100, scoreTwoLines.multiLineBonus) // Multi-line bonus for 2 lines
        assertEquals(200, scoreTwoLines.lineClearScore) // 2 * 100
    }

    // 9. Tính combo (Combo Calculation)
    @Test
    fun testComboCalculation() {
        val dot = PuzzleShapesRepository.DOT_1X1

        // Turn 1: 1 line cleared
        val turn1 = ScoreEvaluator.evaluateTurn(dot, LineClearResult(listOf(0), emptyList()), 0)
        assertEquals(1, turn1.newCombo)
        assertEquals(50, turn1.comboBonus) // 1 * 50

        // Turn 2: another line cleared consecutively
        val turn2 = ScoreEvaluator.evaluateTurn(dot, LineClearResult(listOf(1), emptyList()), turn1.newCombo)
        assertEquals(2, turn2.newCombo)
        assertEquals(100, turn2.comboBonus) // 2 * 50

        // Turn 3: no line cleared -> resets combo to 0
        val turn3 = ScoreEvaluator.evaluateTurn(dot, LineClearResult(emptyList(), emptyList()), turn2.newCombo)
        assertEquals(0, turn3.newCombo)
        assertEquals(0, turn3.comboBonus)
    }

    // 10. Sinh 3 quân ngẫu nhiên (Random Piece Spawning)
    @Test
    fun testSpawnRandomPieces() {
        val spawned = GameEngine.spawnPieces(emptyBoard, level = 1, random = Random(42))
        assertEquals("Must spawn exactly 3 pieces", 3, spawned.size)
        spawned.forEach { piece ->
            assertNotNull(piece)
            assertTrue(piece.cellCount in 1..9)
        }

        // Must guarantee at least one piece can be placed
        assertTrue(spawned.any { GameEngine.canPlaceAnywhere(it, emptyBoard) })
    }

    // 11. Kiểm tra khả năng đặt quân (canPlaceAnywhere)
    @Test
    fun testCanPlaceAnywhere() {
        val square3x3 = PuzzleShapesRepository.SQUARE_3X3
        assertTrue("Square 3x3 should fit on an empty 10x10 board", GameEngine.canPlaceAnywhere(square3x3, emptyBoard))

        // Create a checkered board where no 2x2 contiguous area is free
        val filledBoard = emptyBoard.mapIndexed { r, row ->
            row.mapIndexed { c, _ ->
                // Fill every even row & col
                BoardCellState(isFilled = (r % 2 == 0 && c % 2 == 0), colorType = BlockColorType.TURQUOISE)
            }
        }
        val dot = PuzzleShapesRepository.DOT_1X1
        assertTrue("Single dot should still fit in odd cells", GameEngine.canPlaceAnywhere(dot, filledBoard))
    }

    // 12. Xác định Game Over (Game Over Detection)
    @Test
    fun testGameOverDetection() {
        // Completely fill the board
        val fullBoard = List(BOARD_SIZE) {
            List(BOARD_SIZE) { BoardCellState(isFilled = true, colorType = BlockColorType.TURQUOISE) }
        }

        val availablePieces = listOf(
            PuzzleShapesRepository.DOT_1X1,
            PuzzleShapesRepository.LINE_3H,
            PuzzleShapesRepository.SQUARE_2X2
        )

        val isGameOver = GameEngine.isGameOver(availablePieces, fullBoard)
        assertTrue("Full board with no open space must be Game Over", isGameOver)

        // When space exists, not game over
        val notGameOver = GameEngine.isGameOver(availablePieces, emptyBoard)
        assertFalse("Empty board is NOT Game Over", notGameOver)
    }
}
