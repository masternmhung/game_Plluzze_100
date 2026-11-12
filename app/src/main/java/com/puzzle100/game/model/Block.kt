package com.puzzle100.game.model

import androidx.compose.ui.graphics.Color
import com.puzzle100.game.ui.theme.BlockOrange
import com.puzzle100.game.ui.theme.BlockPink
import com.puzzle100.game.ui.theme.BlockPurple
import com.puzzle100.game.ui.theme.BlockTurquoise

enum class BlockColorType(val color: Color, val hex: String, val label: String) {
    TURQUOISE(BlockTurquoise, "#00E5FF", "Turquoise"),
    ORANGE(BlockOrange, "#FF6D00", "Cam"),
    PURPLE(BlockPurple, "#7C4DFF", "Tím"),
    PINK(BlockPink, "#FF4081", "Hồng")
}

enum class PieceCategory {
    SMALL,   // 1 to 3 cells
    MEDIUM,  // 4 to 5 cells
    LARGE    // 6 to 9 cells
}

data class BlockCell(
    val row: Int,
    val col: Int
)

data class PuzzleShape(
    val id: String,
    val name: String,
    val shapeMatrix: List<List<Boolean>>, // 2D matrix
    val colorType: BlockColorType,
    val category: PieceCategory
) {
    val width: Int get() = shapeMatrix.firstOrNull()?.size ?: 0
    val height: Int get() = shapeMatrix.size
    val cellCount: Int get() = shapeMatrix.sumOf { row -> row.count { it } }

    /**
     * Relative coordinates (r, c) for each occupied cell from top-left (0,0)
     */
    val occupiedCells: List<BlockCell> by lazy {
        val cells = mutableListOf<BlockCell>()
        shapeMatrix.forEachIndexed { r, row ->
            row.forEachIndexed { c, filled ->
                if (filled) cells.add(BlockCell(r, c))
            }
        }
        cells
    }
}

/**
 * Repository of 32 distinct Puzzle Shapes covering:
 * - 1x1 dot
 * - Horizontal and vertical lines (2x1, 3x1, 4x1, 5x1, 1x2, 1x3, 1x4, 1x5)
 * - Squares (2x2, 3x3)
 * - L-shapes (Small 2x2 in 4 rotations, Large 3x3 in 4 rotations)
 * - T-shapes (4 rotations)
 * - Z and S shapes (horizontal & vertical)
 * - Rectangles (2x3, 3x2)
 * - Complex shapes (Plus 3x3, U-shape 3x2, Corner 3x3, C-shape)
 */
object PuzzleShapesRepository {

    // --- 1. SINGLE DOT (1 CELL) ---
    val DOT_1X1 = PuzzleShape(
        id = "DOT_1X1",
        name = "Điểm Đơn 1x1",
        shapeMatrix = listOf(listOf(true)),
        colorType = BlockColorType.TURQUOISE,
        category = PieceCategory.SMALL
    )

    // --- 2. LINES HORIZONTAL (2 to 5 cells) ---
    val LINE_2H = PuzzleShape(
        id = "LINE_2H",
        name = "Thanh Ngang 2x1",
        shapeMatrix = listOf(listOf(true, true)),
        colorType = BlockColorType.TURQUOISE,
        category = PieceCategory.SMALL
    )

    val LINE_3H = PuzzleShape(
        id = "LINE_3H",
        name = "Thanh Ngang 3x1",
        shapeMatrix = listOf(listOf(true, true, true)),
        colorType = BlockColorType.PURPLE,
        category = PieceCategory.SMALL
    )

    val LINE_4H = PuzzleShape(
        id = "LINE_4H",
        name = "Thanh Ngang 4x1",
        shapeMatrix = listOf(listOf(true, true, true, true)),
        colorType = BlockColorType.PINK,
        category = PieceCategory.MEDIUM
    )

    val LINE_5H = PuzzleShape(
        id = "LINE_5H",
        name = "Thanh Ngang 5x1",
        shapeMatrix = listOf(listOf(true, true, true, true, true)),
        colorType = BlockColorType.ORANGE,
        category = PieceCategory.LARGE
    )

    // --- 3. LINES VERTICAL (2 to 5 cells) ---
    val LINE_2V = PuzzleShape(
        id = "LINE_2V",
        name = "Thanh Dọc 1x2",
        shapeMatrix = listOf(listOf(true), listOf(true)),
        colorType = BlockColorType.TURQUOISE,
        category = PieceCategory.SMALL
    )

    val LINE_3V = PuzzleShape(
        id = "LINE_3V",
        name = "Thanh Dọc 1x3",
        shapeMatrix = listOf(listOf(true), listOf(true), listOf(true)),
        colorType = BlockColorType.PURPLE,
        category = PieceCategory.SMALL
    )

    val LINE_4V = PuzzleShape(
        id = "LINE_4V",
        name = "Thanh Dọc 1x4",
        shapeMatrix = listOf(listOf(true), listOf(true), listOf(true), listOf(true)),
        colorType = BlockColorType.PINK,
        category = PieceCategory.MEDIUM
    )

    val LINE_5V = PuzzleShape(
        id = "LINE_5V",
        name = "Thanh Dọc 1x5",
        shapeMatrix = listOf(listOf(true), listOf(true), listOf(true), listOf(true), listOf(true)),
        colorType = BlockColorType.ORANGE,
        category = PieceCategory.LARGE
    )

    // --- 4. SQUARES ---
    val SQUARE_2X2 = PuzzleShape(
        id = "SQUARE_2X2",
        name = "Khối Vuông 2x2",
        shapeMatrix = listOf(
            listOf(true, true),
            listOf(true, true)
        ),
        colorType = BlockColorType.ORANGE,
        category = PieceCategory.MEDIUM
    )

    val SQUARE_3X3 = PuzzleShape(
        id = "SQUARE_3X3",
        name = "Khối Vuông Lớn 3x3",
        shapeMatrix = listOf(
            listOf(true, true, true),
            listOf(true, true, true),
            listOf(true, true, true)
        ),
        colorType = BlockColorType.PINK,
        category = PieceCategory.LARGE
    )

    // --- 5. SMALL L SHAPES (2x2, 3 cells) ---
    val L_SMALL_TL = PuzzleShape(
        id = "L_SMALL_TL",
        name = "Chữ L Nhỏ (Góc Tây Bắc)",
        shapeMatrix = listOf(
            listOf(true, true),
            listOf(true, false)
        ),
        colorType = BlockColorType.TURQUOISE,
        category = PieceCategory.SMALL
    )

    val L_SMALL_TR = PuzzleShape(
        id = "L_SMALL_TR",
        name = "Chữ L Nhỏ (Góc Đông Bắc)",
        shapeMatrix = listOf(
            listOf(true, true),
            listOf(false, true)
        ),
        colorType = BlockColorType.TURQUOISE,
        category = PieceCategory.SMALL
    )

    val L_SMALL_BL = PuzzleShape(
        id = "L_SMALL_BL",
        name = "Chữ L Nhỏ (Góc Tây Nam)",
        shapeMatrix = listOf(
            listOf(true, false),
            listOf(true, true)
        ),
        colorType = BlockColorType.TURQUOISE,
        category = PieceCategory.SMALL
    )

    val L_SMALL_BR = PuzzleShape(
        id = "L_SMALL_BR",
        name = "Chữ L Nhỏ (Góc Đông Nam)",
        shapeMatrix = listOf(
            listOf(false, true),
            listOf(true, true)
        ),
        colorType = BlockColorType.TURQUOISE,
        category = PieceCategory.SMALL
    )

    // --- 6. LARGE L SHAPES (3x3, 5 cells) ---
    val L_LARGE_1 = PuzzleShape(
        id = "L_LARGE_1",
        name = "Chữ L Lớn (Góc Dưới Phải)",
        shapeMatrix = listOf(
            listOf(true, false, false),
            listOf(true, false, false),
            listOf(true, true, true)
        ),
        colorType = BlockColorType.ORANGE,
        category = PieceCategory.MEDIUM
    )

    val L_LARGE_2 = PuzzleShape(
        id = "L_LARGE_2",
        name = "Chữ L Lớn (Góc Dưới Trái)",
        shapeMatrix = listOf(
            listOf(false, false, true),
            listOf(false, false, true),
            listOf(true, true, true)
        ),
        colorType = BlockColorType.ORANGE,
        category = PieceCategory.MEDIUM
    )

    val L_LARGE_3 = PuzzleShape(
        id = "L_LARGE_3",
        name = "Chữ L Lớn (Góc Trên Phải)",
        shapeMatrix = listOf(
            listOf(true, true, true),
            listOf(true, false, false),
            listOf(true, false, false)
        ),
        colorType = BlockColorType.ORANGE,
        category = PieceCategory.MEDIUM
    )

    val L_LARGE_4 = PuzzleShape(
        id = "L_LARGE_4",
        name = "Chữ L Lớn (Góc Trên Trái)",
        shapeMatrix = listOf(
            listOf(true, true, true),
            listOf(false, false, true),
            listOf(false, false, true)
        ),
        colorType = BlockColorType.ORANGE,
        category = PieceCategory.MEDIUM
    )

    // --- 7. T SHAPES (4 rotations, 4 cells) ---
    val T_UP = PuzzleShape(
        id = "T_UP",
        name = "Chữ T Ngửa",
        shapeMatrix = listOf(
            listOf(false, true, false),
            listOf(true, true, true)
        ),
        colorType = BlockColorType.PURPLE,
        category = PieceCategory.MEDIUM
    )

    val T_DOWN = PuzzleShape(
        id = "T_DOWN",
        name = "Chữ T Xuôi",
        shapeMatrix = listOf(
            listOf(true, true, true),
            listOf(false, true, false)
        ),
        colorType = BlockColorType.PURPLE,
        category = PieceCategory.MEDIUM
    )

    val T_LEFT = PuzzleShape(
        id = "T_LEFT",
        name = "Chữ T Sang Trái",
        shapeMatrix = listOf(
            listOf(false, true),
            listOf(true, true),
            listOf(false, true)
        ),
        colorType = BlockColorType.PURPLE,
        category = PieceCategory.MEDIUM
    )

    val T_RIGHT = PuzzleShape(
        id = "T_RIGHT",
        name = "Chữ T Sang Phải",
        shapeMatrix = listOf(
            listOf(true, false),
            listOf(true, true),
            listOf(true, false)
        ),
        colorType = BlockColorType.PURPLE,
        category = PieceCategory.MEDIUM
    )

    // --- 8. Z & S SHAPES (4 cells) ---
    val Z_HORIZ = PuzzleShape(
        id = "Z_HORIZ",
        name = "Khối Z Ngang",
        shapeMatrix = listOf(
            listOf(true, true, false),
            listOf(false, true, true)
        ),
        colorType = BlockColorType.PINK,
        category = PieceCategory.MEDIUM
    )

    val Z_VERT = PuzzleShape(
        id = "Z_VERT",
        name = "Khối Z Dọc",
        shapeMatrix = listOf(
            listOf(false, true),
            listOf(true, true),
            listOf(true, false)
        ),
        colorType = BlockColorType.PINK,
        category = PieceCategory.MEDIUM
    )

    val S_HORIZ = PuzzleShape(
        id = "S_HORIZ",
        name = "Khối S Ngang",
        shapeMatrix = listOf(
            listOf(false, true, true),
            listOf(true, true, false)
        ),
        colorType = BlockColorType.PINK,
        category = PieceCategory.MEDIUM
    )

    val S_VERT = PuzzleShape(
        id = "S_VERT",
        name = "Khối S Dọc",
        shapeMatrix = listOf(
            listOf(true, false),
            listOf(true, true),
            listOf(false, true)
        ),
        colorType = BlockColorType.PINK,
        category = PieceCategory.MEDIUM
    )

    // --- 9. RECTANGLES (6 cells) ---
    val RECT_2X3 = PuzzleShape(
        id = "RECT_2X3",
        name = "Chữ Nhật 2x3",
        shapeMatrix = listOf(
            listOf(true, true, true),
            listOf(true, true, true)
        ),
        colorType = BlockColorType.PURPLE,
        category = PieceCategory.LARGE
    )

    val RECT_3X2 = PuzzleShape(
        id = "RECT_3X2",
        name = "Chữ Nhật 3x2",
        shapeMatrix = listOf(
            listOf(true, true),
            listOf(true, true),
            listOf(true, true)
        ),
        colorType = BlockColorType.PURPLE,
        category = PieceCategory.LARGE
    )

    // --- 10. COMPLEX SHAPES (5 to 8 cells) ---
    val PLUS_CROSS = PuzzleShape(
        id = "PLUS_CROSS",
        name = "Dấu Cộng (Cross Plus)",
        shapeMatrix = listOf(
            listOf(false, true, false),
            listOf(true, true, true),
            listOf(false, true, false)
        ),
        colorType = BlockColorType.TURQUOISE,
        category = PieceCategory.MEDIUM
    )

    val U_SHAPE = PuzzleShape(
        id = "U_SHAPE",
        name = "Chữ U 3x2",
        shapeMatrix = listOf(
            listOf(true, false, true),
            listOf(true, true, true)
        ),
        colorType = BlockColorType.ORANGE,
        category = PieceCategory.MEDIUM
    )

    val CORNER_3X3 = PuzzleShape(
        id = "CORNER_3X3",
        name = "Góc Viền C 3x3",
        shapeMatrix = listOf(
            listOf(true, true, true),
            listOf(true, false, false),
            listOf(true, true, true)
        ),
        colorType = BlockColorType.PINK,
        category = PieceCategory.LARGE
    )

    /**
     * All 32 distinct registered shapes
     */
    val ALL_SHAPES: List<PuzzleShape> = listOf(
        DOT_1X1,
        LINE_2H, LINE_3H, LINE_4H, LINE_5H,
        LINE_2V, LINE_3V, LINE_4V, LINE_5V,
        SQUARE_2X2, SQUARE_3X3,
        L_SMALL_TL, L_SMALL_TR, L_SMALL_BL, L_SMALL_BR,
        L_LARGE_1, L_LARGE_2, L_LARGE_3, L_LARGE_4,
        T_UP, T_DOWN, T_LEFT, T_RIGHT,
        Z_HORIZ, Z_VERT, S_HORIZ, S_VERT,
        RECT_2X3, RECT_3X2,
        PLUS_CROSS, U_SHAPE, CORNER_3X3
    )

    val SMALL_SHAPES: List<PuzzleShape> = ALL_SHAPES.filter { it.category == PieceCategory.SMALL }
    val MEDIUM_SHAPES: List<PuzzleShape> = ALL_SHAPES.filter { it.category == PieceCategory.MEDIUM }
    val LARGE_SHAPES: List<PuzzleShape> = ALL_SHAPES.filter { it.category == PieceCategory.LARGE }
}
