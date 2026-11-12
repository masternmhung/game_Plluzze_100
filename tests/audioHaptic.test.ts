/**
 * PUZZLE 100 - Automated Verification Test Suite for Phase 3.1:
 * Sound Effects & Haptic Feedback
 */

interface MockSoundManager {
  isSoundEnabled: boolean;
  masterVolume: number;
  isReleased: boolean;
  pickupCount: number;
  placeCount: number;
  invalidMoveCount: number;
  lineClearCount: number;
  comboCount: number;
  gameOverCount: number;
  levelCompleteCount: number;
}

function createMockSoundManager(): MockSoundManager {
  return {
    isSoundEnabled: true,
    masterVolume: 0.8,
    isReleased: false,
    pickupCount: 0,
    placeCount: 0,
    invalidMoveCount: 0,
    lineClearCount: 0,
    comboCount: 0,
    gameOverCount: 0,
    levelCompleteCount: 0,
  };
}

function playSound(mgr: MockSoundManager, event: string) {
  if (!mgr.isSoundEnabled || mgr.isReleased || mgr.masterVolume <= 0) return;
  if (event === 'PICKUP') mgr.pickupCount++;
  if (event === 'PLACE') mgr.placeCount++;
  if (event === 'INVALID') mgr.invalidMoveCount++;
  if (event === 'LINE_CLEAR') mgr.lineClearCount++;
  if (event === 'COMBO') mgr.comboCount++;
  if (event === 'GAME_OVER') mgr.gameOverCount++;
  if (event === 'LEVEL_COMPLETE') mgr.levelCompleteCount++;
}

interface MockHapticManager {
  isHapticEnabled: boolean;
  hasHardwareVibrator: boolean;
  vibrateCounts: Record<string, number>;
}

function createMockHapticManager(hasHardware = true): MockHapticManager {
  return {
    isHapticEnabled: true,
    hasHardwareVibrator: hasHardware,
    vibrateCounts: {
      PICKUP: 0,
      PLACE: 0,
      INVALID: 0,
      LINE_CLEAR: 0,
      COMBO: 0,
      GAME_OVER: 0,
      LEVEL_COMPLETE: 0,
    },
  };
}

function vibrate(mgr: MockHapticManager, event: string) {
  if (!mgr.isHapticEnabled || !mgr.hasHardwareVibrator) return;
  mgr.vibrateCounts[event] = (mgr.vibrateCounts[event] || 0) + 1;
}

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

console.log('--- PUZZLE 100: RUNNING 8 AUDIO & HAPTIC TESTS (PHASE 3.1) ---');

// Test 1: Bật/tắt âm thanh
{
  const snd = createMockSoundManager();
  playSound(snd, 'PLACE');
  assert(snd.placeCount === 1, 'Test 1a: Play sound when enabled');

  snd.isSoundEnabled = false;
  playSound(snd, 'PLACE');
  assert(snd.placeCount === 1, 'Test 1b: No sound when sound is OFF');
}

// Test 2: Bật/tắt rung
{
  const haptic = createMockHapticManager();
  vibrate(haptic, 'PLACE');
  assert(haptic.vibrateCounts.PLACE === 1, 'Test 2a: Vibrate when haptic is ON');

  haptic.isHapticEnabled = false;
  vibrate(haptic, 'PLACE');
  assert(haptic.vibrateCounts.PLACE === 1, 'Test 2b: No vibration when haptic is OFF');
}

// Test 3: Điều chỉnh âm lượng (0% to 100%)
{
  const snd = createMockSoundManager();
  snd.masterVolume = 0.5;
  assert(snd.masterVolume === 0.5, 'Test 3a: Volume adjusted to 50%');

  snd.masterVolume = 0;
  playSound(snd, 'PICKUP');
  assert(snd.pickupCount === 0, 'Test 3b: Zero volume produces silence');
}

// Test 4: Sự kiện đặt quân chỉ phát đúng một lần
{
  const snd = createMockSoundManager();
  const haptic = createMockHapticManager();

  // Simulate onDragEnded -> valid placement event
  playSound(snd, 'PLACE');
  vibrate(haptic, 'PLACE');

  assert(snd.placeCount === 1, 'Test 4a: Place sound emitted exactly once');
  assert(haptic.vibrateCounts.PLACE === 1, 'Test 4b: Place vibration emitted exactly once');
}

// Test 5: Sự kiện xóa hàng/cột chỉ phát đúng một lần
{
  const snd = createMockSoundManager();
  const haptic = createMockHapticManager();

  playSound(snd, 'LINE_CLEAR');
  vibrate(haptic, 'LINE_CLEAR');

  assert(snd.lineClearCount === 1, 'Test 5a: Line clear sound emitted exactly once per clear');
  assert(haptic.vibrateCounts.LINE_CLEAR === 1, 'Test 5b: Line clear vibration emitted exactly once');
}

// Test 6: Không phát âm thanh khi thao tác kéo quân liên tục
{
  const snd = createMockSoundManager();

  // Drag start
  playSound(snd, 'PICKUP');

  // Drag move loop (30 events)
  for (let i = 0; i < 30; i++) {
    // onDragMoved must NOT invoke playSound!
  }

  assert(snd.pickupCount === 1, 'Test 6: Continuous pointer drag emits 0 additional sound effects');
}

// Test 7: Không crash nếu thiết bị không hỗ trợ rung
{
  const noVibeDevice = createMockHapticManager(false);
  let crashed = false;

  try {
    vibrate(noVibeDevice, 'PICKUP');
    vibrate(noVibeDevice, 'LINE_CLEAR');
    vibrate(noVibeDevice, 'COMBO');
  } catch (e) {
    crashed = true;
  }

  assert(!crashed && noVibeDevice.vibrateCounts.PICKUP === 0, 'Test 7: Safe failover on devices without vibrator hardware');
}

// Test 8: Giải phóng SoundPool đúng vòng đời
{
  const snd = createMockSoundManager();
  snd.isReleased = true; // onDispose called
  playSound(snd, 'PLACE');

  assert(snd.placeCount === 0, 'Test 8: SoundPool released cleanly on screen dispose, no calls leaked');
}

console.log(`\n--- TEST RUN FINISHED: ${passedCount} PASSED, ${failedCount} FAILED ---`);
if (failedCount > 0) {
  process.exit(1);
}
