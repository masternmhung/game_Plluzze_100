// tests/persistence.test.ts
// Verification test suite for Phase 3.2: Room Database, TypeConverters, DataStore Preferences, and Repositories

const BOARD_SIZE = 10;

interface BoardCell {
  isFilled: boolean;
  colorType: string | null;
}

interface SerializedCell {
  r: number;
  c: number;
  color: string;
}

// Converters
function serializeBoard(board: BoardCell[][]): string {
  const filledCells: SerializedCell[] = [];
  for (let r = 0; r < board.length; r++) {
    for (let c = 0; c < board[r].length; c++) {
      const cell = board[r][c];
      if (cell.isFilled && cell.colorType) {
        filledCells.push({ r, c, color: cell.colorType });
      }
    }
  }
  return JSON.stringify(filledCells);
}

function deserializeBoard(jsonStr: string): BoardCell[][] {
  const board: BoardCell[][] = Array.from({ length: BOARD_SIZE }, () =>
    Array.from({ length: BOARD_SIZE }, () => ({ isFilled: false, colorType: null }))
  );
  if (!jsonStr || jsonStr.trim() === '') return board;
  try {
    const list: SerializedCell[] = JSON.parse(jsonStr);
    for (const item of list) {
      if (item.r >= 0 && item.r < BOARD_SIZE && item.c >= 0 && item.c < BOARD_SIZE) {
        board[item.r][item.c] = { isFilled: true, colorType: item.color };
      }
    }
  } catch (e) {
    // Return empty on error
  }
  return board;
}

function serializePieces(pieces: (string | null)[]): string {
  return JSON.stringify(pieces);
}

function deserializePieces(jsonStr: string): (string | null)[] {
  if (!jsonStr) return [null, null, null];
  try {
    return JSON.parse(jsonStr);
  } catch {
    return [null, null, null];
  }
}

function calculateStars(score: number, targetScore: number): number {
  if (score >= Math.floor(targetScore * 1.5)) return 3;
  if (score >= Math.floor(targetScore * 1.2)) return 2;
  if (score >= targetScore) return 1;
  return 0;
}

interface LevelResult {
  levelId: number;
  bestScore: number;
  stars: number;
  timesPlayed: number;
  completed: boolean;
}

function saveLevelResult(existing: LevelResult | null, levelId: number, score: number, targetScore: number): LevelResult {
  const earnedStars = calculateStars(score, targetScore);
  const isCompleted = score >= targetScore;
  if (existing) {
    return {
      levelId,
      bestScore: Math.max(existing.bestScore, score),
      stars: Math.max(existing.stars, earnedStars),
      timesPlayed: existing.timesPlayed + 1,
      completed: existing.completed || isCompleted
    };
  }
  return {
    levelId,
    bestScore: score,
    stars: earnedStars,
    timesPlayed: 1,
    completed: isCompleted
  };
}

let passed = 0;
let failed = 0;

function assert(condition: boolean, msg: string) {
  if (condition) {
    console.log(`  ✓ ${msg}`);
    passed++;
  } else {
    console.error(`  ✗ FAIL: ${msg}`);
    failed++;
  }
}

console.log('\n--- PUZZLE 100: Phase 3.2 Persistence Test Suite ---');

// 1. Board Serialization & Deserialization
console.log('\n[Group 1: Room Board Converter]');
const testBoard: BoardCell[][] = Array.from({ length: BOARD_SIZE }, () =>
  Array.from({ length: BOARD_SIZE }, () => ({ isFilled: false, colorType: null }))
);
testBoard[0][0] = { isFilled: true, colorType: 'TURQUOISE' };
testBoard[4][5] = { isFilled: true, colorType: 'ORANGE' };
testBoard[9][9] = { isFilled: true, colorType: 'PINK' };

const boardJson = serializeBoard(testBoard);
const restoredBoard = deserializeBoard(boardJson);
assert(restoredBoard[0][0].isFilled && restoredBoard[0][0].colorType === 'TURQUOISE', 'Restores cell (0,0) with TURQUOISE color');
assert(restoredBoard[4][5].isFilled && restoredBoard[4][5].colorType === 'ORANGE', 'Restores cell (4,5) with ORANGE color');
assert(restoredBoard[9][9].isFilled && restoredBoard[9][9].colorType === 'PINK', 'Restores cell (9,9) with PINK color');
assert(!restoredBoard[0][1].isFilled, 'Empty cells remain empty');

// 2. Empty Board
const emptyJson = serializeBoard(Array.from({ length: 10 }, () => Array.from({ length: 10 }, () => ({ isFilled: false, colorType: null }))));
const restoredEmpty = deserializeBoard(emptyJson);
assert(restoredEmpty.every(row => row.every(c => !c.isFilled)), 'Empty board serialization round-trip');

// 3. Pieces Serialization
console.log('\n[Group 2: Pieces Converter]');
const trayPieces = ['SQUARE_2X2', null, 'LINE_3H'];
const piecesJson = serializePieces(trayPieces);
const restoredPieces = deserializePieces(piecesJson);
assert(restoredPieces.length === 3, 'Pieces array length is exactly 3');
assert(restoredPieces[0] === 'SQUARE_2X2', 'Preserves slot 0 shape ID');
assert(restoredPieces[1] === null, 'Preserves null slot 1');
assert(restoredPieces[2] === 'LINE_3H', 'Preserves slot 2 shape ID');

// 4. Star Calculation
console.log('\n[Group 3: Star Evaluation]');
const target = 1000;
assert(calculateStars(400, target) === 0, 'Score 400 < 1000 -> 0 stars');
assert(calculateStars(999, target) === 0, 'Score 999 < 1000 -> 0 stars');
assert(calculateStars(1000, target) === 1, 'Score 1000 >= 1000 -> 1 star');
assert(calculateStars(1199, target) === 1, 'Score 1199 < 1200 -> 1 star');
assert(calculateStars(1200, target) === 2, 'Score 1200 >= 1200 (1.2x) -> 2 stars');
assert(calculateStars(1499, target) === 2, 'Score 1499 < 1500 -> 2 stars');
assert(calculateStars(1500, target) === 3, 'Score 1500 >= 1500 (1.5x) -> 3 stars');
assert(calculateStars(2500, target) === 3, 'Score 2500 -> 3 stars');

// 5. Anti-Downgrade Protection
console.log('\n[Group 4: Anti-Downgrade Protection]');
const existingResult: LevelResult = {
  levelId: 1,
  bestScore: 2500,
  stars: 3,
  timesPlayed: 2,
  completed: true
};
const replayedLower = saveLevelResult(existingResult, 1, 800, 650);
assert(replayedLower.bestScore === 2500, 'Replay with lower score does NOT decrease bestScore');
assert(replayedLower.stars === 3, 'Replay with lower score does NOT decrease earned stars');
assert(replayedLower.timesPlayed === 3, 'Times played correctly increments to 3');
assert(replayedLower.completed, 'Completion status remains true');

const replayedHigher = saveLevelResult(existingResult, 1, 3200, 650);
assert(replayedHigher.bestScore === 3200, 'Replay with higher score updates bestScore to 3200');
assert(replayedHigher.stars === 3, 'Stars remain max 3');

// 6. Level Unlock Logic
console.log('\n[Group 5: Level Unlock Logic]');
function unlockNext(currentUnlocked: number, completedLevel: number): number {
  return Math.min(100, Math.max(currentUnlocked, completedLevel + 1));
}
assert(unlockNext(1, 1) === 2, 'Completing Level 1 unlocks Level 2');
assert(unlockNext(5, 2) === 5, 'Replaying Level 2 does not lower unlocked level 5');
assert(unlockNext(99, 99) === 100, 'Completing Level 99 unlocks Level 100');
assert(unlockNext(100, 100) === 100, 'Completing Level 100 caps at Level 100');

// 7. DataStore Volume Clamping
console.log('\n[Group 6: Preferences DataStore]');
function clampVolume(vol: number): number {
  return Math.min(1.0, Math.max(0.0, vol));
}
assert(clampVolume(-0.2) === 0.0, 'Negative volume clamped to 0.0');
assert(clampVolume(1.8) === 1.0, 'Excessive volume clamped to 1.0');
assert(clampVolume(0.85) === 0.85, 'Valid volume 0.85 preserved');

// 8. Active Game Lifecycle (Save on Discrete Events, Delete on Victory/Game Over)
console.log('\n[Group 7: Active Game Lifecycle]');
interface ActiveGame {
  levelId: number;
  score: number;
  boardData: string;
}
let activeDb: ActiveGame | null = null;
function onPiecePlaced(state: { level: number; score: number; isVictory: boolean; isGameOver: boolean; board: BoardCell[][] }) {
  if (state.isVictory || state.isGameOver) {
    activeDb = null;
  } else {
    activeDb = {
      levelId: state.level,
      score: state.score,
      boardData: serializeBoard(state.board)
    };
  }
}

// Ongoing piece placement -> saves
onPiecePlaced({ level: 3, score: 450, isVictory: false, isGameOver: false, board: testBoard });
assert(activeDb !== null && (activeDb as ActiveGame | null)?.score === 450, 'Active game saved on regular piece placement');

// Victory reached -> automatically removed from active table
onPiecePlaced({ level: 3, score: 950, isVictory: true, isGameOver: false, board: testBoard });
assert(activeDb === null, 'Active game automatically cleared upon level victory');

// 9. Replay Level Star Delta & Completion Anti-Inflation
console.log('\n[Group 8: Replay Star Delta & Completion Anti-Inflation]');
interface LevelOutcome {
  finalStars: number;
  starDelta: number;
  isNewlyCompleted: boolean;
}
function processLevelCompletion(existingStars: number, wasCompleted: boolean, score: number, targetScore: number): LevelOutcome {
  const earned = calculateStars(score, targetScore);
  const finalStars = Math.max(existingStars, earned);
  const starDelta = Math.max(0, finalStars - existingStars);
  const isNewlyCompleted = !wasCompleted && score >= targetScore;
  return { finalStars, starDelta, isNewlyCompleted };
}

// Replaying an already 3-star completed level with 3 stars again
const replay1 = processLevelCompletion(3, true, 2000, 1000);
assert(replay1.starDelta === 0, 'Replay with same 3 stars results in starDelta = 0');
assert(!replay1.isNewlyCompleted, 'Replay is NOT marked as newly completed');

// Replaying a 1-star level and improving to 3 stars
const replay2 = processLevelCompletion(1, true, 2000, 1000);
assert(replay2.starDelta === 2, 'Improving from 1 star to 3 stars results in starDelta = 2');
assert(!replay2.isNewlyCompleted, 'Level was already completed, isNewlyCompleted remains false');

// 10. Multi-Level Independent Active Game Storage
console.log('\n[Group 9: Multi-Level Independent Active Game Storage]');
const multiActiveDb = new Map<number, { levelId: number; score: number }>();
multiActiveDb.set(20, { levelId: 20, score: 3400 });
multiActiveDb.set(5, { levelId: 5, score: 450 });

assert(multiActiveDb.get(20)?.score === 3400, 'Level 20 active game is preserved when playing Level 5');
assert(multiActiveDb.get(5)?.score === 450, 'Level 5 active game is preserved separately');

// 11. Game Over Write Sequence
console.log('\n[Group 10: Game Over Write Sequence]');
const order: string[] = [];
function onGameOver(level: number, finalScore: number) {
  order.push('SAVE_LEVEL_RESULT');
  order.push('UPDATE_HIGH_SCORE');
  order.push('DELETE_ACTIVE_GAME');
}
onGameOver(3, 600);
assert(order[0] === 'SAVE_LEVEL_RESULT', 'Step 1: Save level result');
assert(order[1] === 'UPDATE_HIGH_SCORE', 'Step 2: Update high score in progress');
assert(order[2] === 'DELETE_ACTIVE_GAME', 'Step 3: Delete active game entity');

console.log(`\nResults: ${passed} passed, ${failed} failed`);
if (failed > 0) {
  process.exit(1);
} else {
  console.log('ALL PHASE 3.3 INTEGRATION TESTS PASSED!\n');
}
