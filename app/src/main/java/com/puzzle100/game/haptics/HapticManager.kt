package com.puzzle100.game.haptics

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log

enum class HapticEvent {
    PICKUP,
    PLACE,
    INVALID_MOVE,
    LINE_CLEAR,
    COMBO,
    GAME_OVER,
    LEVEL_COMPLETE
}

interface IHapticManager {
    var isHapticEnabled: Boolean
    fun vibratePickup()
    fun vibratePlace()
    fun vibrateInvalidMove()
    fun vibrateLineClear()
    fun vibrateCombo(level: Int)
    fun vibrateGameOver()
    fun vibrateLevelComplete()
}

/**
 * Production-grade HapticManager for Android native tactile feedback.
 * Features:
 * - API 31+ VibratorManager and API 26+ VibrationEffect support
 * - Hardware capability verification (hasVibrator check)
 * - Safe failover without crashing if hardware or permission is missing
 * - Distinct tactile signatures for game actions
 */
class HapticManager(private val context: Context) : IHapticManager {

    companion object {
        private const val TAG = "HapticManager"
        private const val THROTTLE_INTERVAL_MS = 50L
    }

    override var isHapticEnabled: Boolean = true

    private val vibrator: Vibrator? by lazy {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to initialize Vibrator service", e)
            null
        }
    }

    private var lastVibrateTime: Long = 0L

    private fun vibrate(effect: VibrationEffect) {
        if (!isHapticEnabled) return

        val now = System.currentTimeMillis()
        if (now - lastVibrateTime < THROTTLE_INTERVAL_MS) {
            return // Skip rapid repeated fire to avoid hardware buzzing fatigue
        }
        lastVibrateTime = now

        try {
            val v = vibrator
            if (v != null && v.hasVibrator()) {
                v.vibrate(effect)
            }
        } catch (e: SecurityException) {
            Log.w(TAG, "VIBRATE permission not granted", e)
        } catch (e: Exception) {
            Log.e(TAG, "Error executing vibration effect", e)
        }
    }

    override fun vibratePickup() {
        // Very light tick (15ms, light amplitude)
        val effect = VibrationEffect.createOneShot(15L, VibrationEffect.DEFAULT_AMPLITUDE / 3)
        vibrate(effect)
    }

    override fun vibratePlace() {
        // Crisp tactile snap (25ms, medium amplitude)
        val effect = VibrationEffect.createOneShot(25L, VibrationEffect.DEFAULT_AMPLITUDE / 2)
        vibrate(effect)
    }

    override fun vibrateInvalidMove() {
        // Double warning tap: 30ms pulse, 40ms pause, 30ms pulse
        val timings = longArrayOf(0L, 30L, 40L, 30L)
        val amplitudes = intArrayOf(0, 180, 0, 180)
        val effect = VibrationEffect.createWaveform(timings, amplitudes, -1)
        vibrate(effect)
    }

    override fun vibrateLineClear() {
        // Strong crisp impulse (65ms, high amplitude)
        val effect = VibrationEffect.createOneShot(65L, VibrationEffect.DEFAULT_AMPLITUDE)
        vibrate(effect)
    }

    override fun vibrateCombo(level: Int) {
        // Progressive impulse scaled with combo intensity
        val duration = (40L + (level * 15L)).coerceAtMost(140L)
        val effect = VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE)
        vibrate(effect)
    }

    override fun vibrateGameOver() {
        // Long, low-frequency ending rumble
        val timings = longArrayOf(0L, 70L, 50L, 110L)
        val amplitudes = intArrayOf(0, 120, 0, 200)
        val effect = VibrationEffect.createWaveform(timings, amplitudes, -1)
        vibrate(effect)
    }

    override fun vibrateLevelComplete() {
        // Celebratory triple-pulse fanfare
        val timings = longArrayOf(0L, 40L, 40L, 50L, 40L, 80L)
        val amplitudes = intArrayOf(0, 150, 0, 200, 0, 255)
        val effect = VibrationEffect.createWaveform(timings, amplitudes, -1)
        vibrate(effect)
    }
}
