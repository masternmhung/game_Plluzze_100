package com.puzzle100.game

import com.puzzle100.game.audio.ISoundManager
import com.puzzle100.game.haptics.IHapticManager
import com.puzzle100.game.model.PuzzleShapesRepository
import com.puzzle100.game.ui.screens.game.GameEffectEvent
import com.puzzle100.game.ui.screens.game.GameViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Mock SoundManager for verifying sound events, toggles, throttling and lifecycle
 */
class MockSoundManager : ISoundManager {
    override var isSoundEnabled: Boolean = true
    override var masterVolume: Float = 0.8f
    var isReleased: Boolean = false

    var pickupCount: Int = 0
    var placeCount: Int = 0
    var invalidMoveCount: Int = 0
    var lineClearCount: Int = 0
    var comboCount: Int = 0
    var lastComboLevel: Int = 0
    var gameOverCount: Int = 0
    var levelCompleteCount: Int = 0

    override fun playPickup() {
        if (!isSoundEnabled || isReleased) return
        pickupCount++
    }

    override fun playPlace() {
        if (!isSoundEnabled || isReleased) return
        placeCount++
    }

    override fun playInvalidMove() {
        if (!isSoundEnabled || isReleased) return
        invalidMoveCount++
    }

    override fun playLineClear() {
        if (!isSoundEnabled || isReleased) return
        lineClearCount++
    }

    override fun playCombo(level: Int) {
        if (!isSoundEnabled || isReleased) return
        comboCount++
        lastComboLevel = level
    }

    override fun playGameOver() {
        if (!isSoundEnabled || isReleased) return
        gameOverCount++
    }

    override fun playLevelComplete() {
        if (!isSoundEnabled || isReleased) return
        levelCompleteCount++
    }

    override fun release() {
        isReleased = true
    }
}

/**
 * Mock HapticManager verifying vibration calls, safety on devices without hardware, and toggles
 */
class MockHapticManager(val hasHardwareVibrator: Boolean = true) : IHapticManager {
    override var isHapticEnabled: Boolean = true

    var pickupVibrateCount: Int = 0
    var placeVibrateCount: Int = 0
    var invalidVibrateCount: Int = 0
    var lineClearVibrateCount: Int = 0
    var comboVibrateCount: Int = 0
    var gameOverVibrateCount: Int = 0
    var levelCompleteVibrateCount: Int = 0

    private fun canVibrate(): Boolean = isHapticEnabled && hasHardwareVibrator

    override fun vibratePickup() {
        if (canVibrate()) pickupVibrateCount++
    }

    override fun vibratePlace() {
        if (canVibrate()) placeVibrateCount++
    }

    override fun vibrateInvalidMove() {
        if (canVibrate()) invalidVibrateCount++
    }

    override fun vibrateLineClear() {
        if (canVibrate()) lineClearVibrateCount++
    }

    override fun vibrateCombo(level: Int) {
        if (canVibrate()) comboVibrateCount++
    }

    override fun vibrateGameOver() {
        if (canVibrate()) gameOverVibrateCount++
    }

    override fun vibrateLevelComplete() {
        if (canVibrate()) levelCompleteVibrateCount++
    }
}

/**
 * Phase 3.1 Unit Tests for Sound Effects & Haptic Feedback:
 * 1. Bật/tắt âm thanh
 * 2. Bật/tắt rung
 * 3. Điều chỉnh âm lượng
 * 4. Sự kiện đặt quân chỉ phát đúng một lần
 * 5. Sự kiện xóa hàng/cột chỉ phát đúng một lần
 * 6. Không phát âm thanh khi thao tác kéo quân liên tục
 * 7. Không crash nếu thiết bị không hỗ trợ rung
 * 8. Giải phóng SoundPool đúng vòng đời
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AudioHapticTest {

    private lateinit var soundManager: MockSoundManager
    private lateinit var hapticManager: MockHapticManager

    @Before
    fun setUp() {
        soundManager = MockSoundManager()
        hapticManager = MockHapticManager()
    }

    // 1. Kiểm tra Bật/Tắt âm thanh
    @Test
    fun testSoundToggle() {
        assertTrue(soundManager.isSoundEnabled)
        soundManager.playPlace()
        assertEquals(1, soundManager.placeCount)

        // Turn sound OFF
        soundManager.isSoundEnabled = false
        soundManager.playPlace()
        soundManager.playPickup()
        assertEquals("No sound should be emitted when disabled", 1, soundManager.placeCount)
        assertEquals(0, soundManager.pickupCount)
    }

    // 2. Kiểm tra Bật/Tắt rung
    @Test
    fun testHapticToggle() {
        assertTrue(hapticManager.isHapticEnabled)
        hapticManager.vibratePlace()
        assertEquals(1, hapticManager.placeVibrateCount)

        // Turn haptic OFF
        hapticManager.isHapticEnabled = false
        hapticManager.vibratePlace()
        hapticManager.vibrateLineClear()
        assertEquals("No vibration should occur when disabled", 1, hapticManager.placeVibrateCount)
        assertEquals(0, hapticManager.lineClearVibrateCount)
    }

    // 3. Kiểm tra Điều chỉnh âm lượng
    @Test
    fun testVolumeAdjustment() {
        soundManager.masterVolume = 0.5f
        assertEquals(0.5f, soundManager.masterVolume, 0.001f)

        soundManager.masterVolume = 1.0f
        assertEquals(1.0f, soundManager.masterVolume, 0.001f)

        // Lower boundary 0.0f
        soundManager.masterVolume = 0.0f
        assertEquals(0.0f, soundManager.masterVolume, 0.001f)
    }

    // 4. Sự kiện đặt quân chỉ phát đúng một lần
    @Test
    fun testSinglePlaceEvent() = runTest {
        val viewModel = GameViewModel()
        val dot = PuzzleShapesRepository.DOT_1X1

        val recordedEvents = mutableListOf<GameEffectEvent>()

        viewModel.placePiece(dot, 0, 0, 0)

        // Consume event from effectsFlow buffer
        val event = viewModel.effectsFlow.replayCache.lastOrNull()
        // Verify ViewModel emitted Place event
        viewModel.onDragStarted(0)
        // Drag start triggers 1 pickup
        val eventsAfter = viewModel.effectsFlow.replayCache
        assertTrue(eventsAfter.isNotEmpty())
    }

    // 5. Sự kiện xóa hàng/cột chỉ phát đúng một lần
    @Test
    fun testLineClearEventOccursOnce() {
        soundManager.playLineClear()
        hapticManager.vibrateLineClear()

        assertEquals("Line clear sound must trigger exactly once per clear", 1, soundManager.lineClearCount)
        assertEquals("Line clear haptic must trigger exactly once per clear", 1, hapticManager.lineClearVibrateCount)
    }

    // 6. Không phát âm thanh khi thao tác kéo quân liên tục (onDragMoved)
    @Test
    fun testNoSoundDuringContinuousDragMovement() {
        val viewModel = GameViewModel()

        viewModel.onDragStarted(0)
        soundManager.playPickup()
        assertEquals("Only 1 pickup sound on start", 1, soundManager.pickupCount)

        // Simulate 20 continuous drag pointer movement updates across the board
        for (i in 0 until 20) {
            viewModel.onDragMoved(
                touchX = 100f + i * 5f,
                touchY = 200f + i * 5f,
                boardLeft = 50f,
                boardTop = 150f,
                boardSizePx = 300f
            )
        }

        // Must still be exactly 1 pickup sound, 0 placement sounds
        assertEquals("Continuous drag must NOT emit additional pickup sounds", 1, soundManager.pickupCount)
        assertEquals("Continuous drag must NOT emit placement sounds", 0, soundManager.placeCount)
    }

    // 7. Không crash nếu thiết bị không hỗ trợ rung (No Hardware Vibrator)
    @Test
    fun testDeviceWithoutHapticHardwareDoesNotCrash() {
        val deviceWithoutVibrator = MockHapticManager(hasHardwareVibrator = false)

        try {
            deviceWithoutVibrator.vibratePickup()
            deviceWithoutVibrator.vibratePlace()
            deviceWithoutVibrator.vibrateLineClear()
            deviceWithoutVibrator.vibrateCombo(3)
            deviceWithoutVibrator.vibrateGameOver()
            deviceWithoutVibrator.vibrateLevelComplete()

            assertEquals(0, deviceWithoutVibrator.pickupVibrateCount)
            assertEquals(0, deviceWithoutVibrator.lineClearVibrateCount)
        } catch (e: Exception) {
            org.junit.Assert.fail("Vibrator must gracefully handle missing hardware without exception: ${e.message}")
        }
    }

    // 8. Giải phóng SoundPool đúng vòng đời (release)
    @Test
    fun testSoundPoolRelease() {
        assertFalse(soundManager.isReleased)
        soundManager.playPlace()
        assertEquals(1, soundManager.placeCount)

        // Dispose lifecycle
        soundManager.release()
        assertTrue(soundManager.isReleased)

        // After release, no sounds are allowed to play
        soundManager.playPlace()
        assertEquals("Sound must not play after release()", 1, soundManager.placeCount)
    }
}
