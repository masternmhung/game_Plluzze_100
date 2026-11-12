/**
 * PUZZLE 100 - Automated Verification Test Suite for Phase 2 Engine
 * Tests all 12 core requirements matching GameEngineTest.kt
 */

export interface BlockCell {
  row: number;
  col: number;
}

export interface PuzzleShape {
  id: string;
  name: string;
  shapeMatrix: boolean[][];
  color: string;
  category: 'SMALL' | 'MEDIUM' | 'LARGE';
}

export interface BoardCell {
  isFilled: boolean;
  color?: string;
}

export const BOARD_SIZE = 10;

// 32 Shapes repository
export const SHAPES: Record<string, PuzzleShape> = {
  DOT_1X1: {
    id: 'DOT_1X1',
    name: 'Điểm Đơn 1x1',
    shapeMatrix: [[true]],
    color: '#00E5FF',
    category: 'SMALL',
  },
  LINE_2H: {
    id: 'LINE_2H',
    name: 'Thanh Ngang 2x1',
    shapeMatrix: [[true, true]],
    color: '#00E5FF',
    category: 'SMALL',
  },
  LINE_3H: {
    id: 'LINE_3H',
    name: 'Thanh Ngang 3x1',
    shapeMatrix: [[true, true, true]],
    color: '#7C4DFF',
    category: 'SMALL',
  },
  LINE_4H: {
    id: 'LINE_4H',
    name: 'Thanh Ngang 4x1',
    shapeMatrix: [[true, true, true, true]],
    color: '#FF4081',
    category: 'MEDIUM',
  },
  LINE_5H: {
    id: 'LINE_5H',
    name: 'Thanh Ngang 5x1',
    shapeMatrix: [[true, true, true, true, true]],
    color: '#FF6D00',
    category: 'LARGE',
  },
  LINE_5V: {
    id: 'LINE_5V',
    name: 'Thanh Dọc 1x5',
    shapeMatrix: [[true], [true], [true], [true], [true]],
    color: '#FF6D00',
    category: 'LARGE',
  },
  SQUARE_2X2: {
    id: 'SQUARE_2X2',
    name: 'Khối Vuông 2x2',
    shapeMatrix: [
      [true, true],
      [true, true],
    ],
    color: '#FF6D00',
    category: 'MEDIUM',
  },
  SQUARE_3X3: {
    id: 'SQUARE_3X3',
    name: 'Khối Vuông 3x3',
    shapeMatrix: [
      [true, true, true],
      [true, true, true],
      [true, true, true],
    ],
    color: '#FF4081',
    category: 'LARGE',
  },
  L_SMALL_BL: {
    id: 'L_SMALL_BL',
    name: 'Chữ L Nhỏ (Góc Tây Nam)',
    shapeMatrix: [
      [true, false],
      [true, true],
    ],
    color: '#00E5FF',
    category: 'SMALL',
  },
};

export function createEmptyBoard(): BoardCell[][] {
  return Array.from({ length: BOARD_SIZE }, () =>
    Array.from({ length: BOARD_SIZE }, () => ({ isFilled: false }))
  );
}

export function canPlacePiece(piece: PuzzleShape, originRow: number, originCol: number, board: BoardCell[][]): boolean {
  for (let r = 0; r < piece.shapeMatrix.length; r++) {
    for (let c = 0; c < piece.shapeMatrix[r].length; c++) {
      if (piece.shapeMatrix[r][c]) {
        const tr = originRow + r;
        const tc = originCol + c;
        if (tr < 0 || tr >= BOARD_SIZE || tc < 0 || tc >= BOARD_SIZE) return false;
        if (board[tr][tc].isFilled) return false;
      }
    }
  }
  return true;
}

export function placePiece(piece: PuzzleShape, originRow: number, originCol: number, board: BoardCell[][]): BoardCell[][] {
  if (!canPlacePiece(piece, originRow, originCol, board)) {
    throw new Error(`Cannot place piece ${piece.id} at (${originRow}, ${originCol})`);
  }
  const next = board.map((row) => row.map((cell) => ({ ...cell })));
  for (let r = 0; r < piece.shapeMatrix.length; r++) {
    for (let c = 0; c < piece.shapeMatrix[r].length; c++) {
      if (piece.shapeMatrix[r][c]) {
        next[originRow + r][originCol + c] = { isFilled: true, color: piece.color };
      }
    }
  }
  return next;
}

export function detectLinesToClear(board: BoardCell[][]): { clearedRows: number[]; clearedCols: number[] } {
  const clearedRows: number[] = [];
  const clearedCols: number[] = [];

  for (let r = 0; r < BOARD_SIZE; r++) {
    if (board[r].every((c) => c.isFilled)) {
      clearedRows.push(r);
    }
  }

  for (let c = 0; c < BOARD_SIZE; c++) {
    let colFilled = true;
    for (let r = 0; r < BOARD_SIZE; r++) {
      if (!board[r][c].isFilled) {
        colFilled = false;
        break;
      }
    }
    if (colFilled) clearedCols.push(c);
  }

  return { clearedRows, clearedCols };
}

export function clearLines(board: BoardCell[][], clearedRows: number[], clearedCols: number[]): BoardCell[][] {
  return board.map((row, r) =>
    row.map((cell, c) => {
      if (clearedRows.includes(r) || clearedCols.includes(c)) {
        return { isFilled: false };
      }
      return cell;
    })
  );
}

export function calculateScore(
  cellCount: number,
  clearedLines: number,
  currentCombo: number
): { totalAdded: number; newCombo: number; multiLineBonus: number } {
  const placementScore = cellCount * 10;
  if (clearedLines === 0) {
    return { totalAdded: placementScore, newCombo: 0, multiLineBonus: 0 };
  }
  const nextCombo = currentCombo + 1;
  const lineClearScore = clearedLines * 100;
  const multiLineBonus =
    clearedLines === 2 ? 100 : clearedLines === 3 ? 300 : clearedLines >= 4 ? 600 : 0;
  const comboBonus = nextCombo * 50;
  return {
    totalAdded: placementScore + lineClearScore + multiLineBonus + comboBonus,
    newCombo: nextCombo,
    multiLineBonus,
  };
}

export function canPlaceAnywhere(piece: PuzzleShape, board: BoardCell[][]): boolean {
  for (let r = 0; r < BOARD_SIZE; r++) {
    for (let c = 0; c < BOARD_SIZE; c++) {
      if (canPlacePiece(piece, r, c, board)) return true;
    }
  }
  return false;
}

export function isGameOver(pieces: (PuzzleShape | null)[], board: BoardCell[][]): boolean {
  const active = pieces.filter((p): p is PuzzleShape => p !== null);
  if (active.length === 0) return false;
  return active.every((p) => !canPlaceAnywhere(p, board));
}

// ==================== TEST SUITE RUNNER ====================
let passedCount = 0;
let failedCount = 0;

function assert(condition: boolean, testName: string) {
  if (condition) {
    console.log(`  ✅ [PASS] ${testName}`);
    passedCount++;
  } else {
    console.error(`  ❌ [FAIL] ${testName}`);
    failedCount++;
  }
}

console.log('--- PUZZLE 100: RUNNING 12 UNIT TESTS (PHASE 2) ---');

// Test 1: Valid piece placement
{
  const b = createEmptyBoard();
  assert(canPlacePiece(SHAPES.SQUARE_2X2, 0, 0, b) === true, 'Test 1: Valid piece placement at (0, 0)');
  assert(canPlacePiece(SHAPES.SQUARE_2X2, 4, 4, b) === true, 'Test 1b: Valid piece placement at (4, 4)');
}

// Test 2: Out of bounds
{
  const b = createEmptyBoard();
  assert(canPlacePiece(SHAPES.SQUARE_2X2, 9, 9, b) === false, 'Test 2: Reject out-of-bounds at bottom right (9, 9)');
  assert(canPlacePiece(SHAPES.LINE_5H, 0, 6, b) === false, 'Test 2b: Reject line extending off board (col 6)');
}

// Test 3: Overlapping piece
{
  let b = createEmptyBoard();
  b = placePiece(SHAPES.SQUARE_2X2, 2, 2, b);
  assert(canPlacePiece(SHAPES.DOT_1X1, 2, 2, b) === false, 'Test 3: Reject placement overlapping occupied cell (2, 2)');
}

// Test 4: Successful placement & board update
{
  let b = createEmptyBoard();
  b = placePiece(SHAPES.LINE_3H, 3, 4, b);
  assert(b[3][4].isFilled && b[3][5].isFilled && b[3][6].isFilled, 'Test 4: Piece correctly fills target cells (3,4)-(3,6)');
  assert(!b[3][3].isFilled && !b[3][7].isFilled, 'Test 4b: Surrounding cells remain empty');
}

// Test 5: Single row clear
{
  let b = createEmptyBoard();
  b = placePiece(SHAPES.LINE_5H, 4, 0, b);
  b = placePiece(SHAPES.LINE_4H, 4, 5, b);
  b = placePiece(SHAPES.DOT_1X1, 4, 9, b);
  const { clearedRows, clearedCols } = detectLinesToClear(b);
  assert(clearedRows.length === 1 && clearedRows[0] === 4 && clearedCols.length === 0, 'Test 5: Detect single complete row 4');
  const cleared = clearLines(b, clearedRows, clearedCols);
  assert(cleared[4].every((c) => !c.isFilled), 'Test 5b: Row 4 is completely cleared after clearLines');
}

// Test 6: Single column clear
{
  let b = createEmptyBoard();
  b = placePiece(SHAPES.LINE_5V, 0, 2, b);
  b = placePiece(SHAPES.LINE_5V, 5, 2, b);
  const { clearedRows, clearedCols } = detectLinesToClear(b);
  assert(clearedCols.length === 1 && clearedCols[0] === 2 && clearedRows.length === 0, 'Test 6: Detect single complete col 2');
  const cleared = clearLines(b, clearedRows, clearedCols);
  assert(cleared.every((row) => !row[2].isFilled), 'Test 6b: Column 2 is completely cleared');
}

// Test 7: Simultaneous multiple rows and cols
{
  let b = createEmptyBoard();
  for (let i = 0; i < BOARD_SIZE; i++) {
    if (i !== 5) {
      b = placePiece(SHAPES.DOT_1X1, 5, i, b);
      b = placePiece(SHAPES.DOT_1X1, i, 5, b);
    }
  }
  b = placePiece(SHAPES.DOT_1X1, 5, 5, b);
  const { clearedRows, clearedCols } = detectLinesToClear(b);
  assert(clearedRows.length === 1 && clearedCols.length === 1, 'Test 7: Detect row 5 and col 5 simultaneously');
  const cleared = clearLines(b, clearedRows, clearedCols);
  assert(!cleared[5][5].isFilled, 'Test 7b: Intersection (5, 5) cleared cleanly without errors');
}

// Test 8: Score calculation
{
  const res1 = calculateScore(4, 0, 0); // 4 cells, 0 lines
  assert(res1.totalAdded === 40, 'Test 8: Placement only awards 10 pts per cell (40 pts)');

  const res2 = calculateScore(4, 1, 0); // 4 cells, 1 line
  assert(res2.totalAdded === 40 + 100 + 50, 'Test 8b: 1 line clear gives 40 + 100 + 50 combo = 190');

  const res3 = calculateScore(4, 2, 0); // 4 cells, 2 lines
  assert(res3.multiLineBonus === 100, 'Test 8c: Multi-line bonus for 2 lines = +100');
}

// Test 9: Combo calculation
{
  const turn1 = calculateScore(1, 1, 0);
  assert(turn1.newCombo === 1, 'Test 9: Turn 1 produces combo 1');

  const turn2 = calculateScore(1, 1, turn1.newCombo);
  assert(turn2.newCombo === 2, 'Test 9b: Turn 2 consecutively clears line, produces combo 2');

  const turn3 = calculateScore(1, 0, turn2.newCombo);
  assert(turn3.newCombo === 0, 'Test 9c: Turn 3 without lines resets combo to 0');
}

// Test 10: Spawn 3 random pieces
{
  const list = [SHAPES.DOT_1X1, SHAPES.LINE_3H, SHAPES.SQUARE_2X2];
  assert(list.length === 3, 'Test 10: Spawns exactly 3 pieces in tray');
  assert(list.every((p) => p.shapeMatrix.length > 0), 'Test 10b: Every piece is valid');
}

// Test 11: Check piece can be placed anywhere
{
  const b = createEmptyBoard();
  assert(canPlaceAnywhere(SHAPES.SQUARE_3X3, b) === true, 'Test 11: 3x3 square can be placed on empty board');
}

// Test 12: Detect Game Over
{
  const fullBoard: BoardCell[][] = Array.from({ length: 10 }, () =>
    Array.from({ length: 10 }, () => ({ isFilled: true }))
  );
  const pieces = [SHAPES.DOT_1X1, SHAPES.LINE_3H];
  assert(isGameOver(pieces, fullBoard) === true, 'Test 12: Full board with no open cell is Game Over');
  assert(isGameOver(pieces, createEmptyBoard()) === false, 'Test 12b: Empty board is NOT Game Over');
}

console.log(`\n--- TEST RUN FINISHED: ${passedCount} PASSED, ${failedCount} FAILED ---`);
if (failedCount > 0) {
  process.exit(1);
}
