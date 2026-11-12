package com.puzzle100.game.data.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.puzzle100.game.model.BOARD_SIZE
import com.puzzle100.game.model.BlockColorType
import com.puzzle100.game.model.BoardCellState
import com.puzzle100.game.model.PuzzleShape
import com.puzzle100.game.model.PuzzleShapesRepository

data class SerializedCell(
    val r: Int,
    val c: Int,
    val color: String
)

class Converters {

    private val gson = Gson()
    private val shapeMap by lazy { PuzzleShapesRepository.ALL_SHAPES.associateBy { it.id } }

    @TypeConverter
    fun fromBoard(board: List<List<BoardCellState>>?): String {
        if (board == null) return ""
        val filledCells = mutableListOf<SerializedCell>()
        for (r in board.indices) {
            for (c in board[r].indices) {
                val cell = board[r][c]
                if (cell.isFilled && cell.colorType != null) {
                    filledCells.add(SerializedCell(r, c, cell.colorType.name))
                }
            }
        }
        return gson.toJson(filledCells)
    }

    @TypeConverter
    fun toBoard(data: String?): List<List<BoardCellState>> {
        val emptyBoard = MutableList(BOARD_SIZE) {
            MutableList(BOARD_SIZE) { BoardCellState(isFilled = false, colorType = null) }
        }
        if (data.isNullOrBlank()) return emptyBoard

        return try {
            val type = object : TypeToken<List<SerializedCell>>() {}.type
            val list: List<SerializedCell> = gson.fromJson(data, type) ?: emptyList()
            for (item in list) {
                if (item.r in 0 until BOARD_SIZE && item.c in 0 until BOARD_SIZE) {
                    val color = try {
                        BlockColorType.valueOf(item.color)
                    } catch (e: Exception) {
                        BlockColorType.TURQUOISE
                    }
                    emptyBoard[item.r][item.c] = BoardCellState(isFilled = true, colorType = color)
                }
            }
            emptyBoard
        } catch (e: Exception) {
            emptyBoard
        }
    }

    @TypeConverter
    fun fromPiecesList(pieces: List<PuzzleShape?>?): String {
        if (pieces == null) return "[]"
        val ids = pieces.map { it?.id }
        return gson.toJson(ids)
    }

    @TypeConverter
    fun toPiecesList(data: String?): List<PuzzleShape?> {
        if (data.isNullOrBlank()) return listOf(null, null, null)
        return try {
            val type = object : TypeToken<List<String?>>() {}.type
            val ids: List<String?> = gson.fromJson(data, type) ?: listOf(null, null, null)
            ids.map { id ->
                if (id == null) null else shapeMap[id]
            }
        } catch (e: Exception) {
            listOf(null, null, null)
        }
    }
}
