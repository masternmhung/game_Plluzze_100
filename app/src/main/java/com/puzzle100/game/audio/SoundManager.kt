package com.puzzle100.game.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.util.Log

enum class SoundEvent {
    PIECE_PICKUP,
    PIECE_PLACE,
    INVALID_MOVE,
    LINE_CLEAR,
    COMBO,
    GAME_OVER,
    LEVEL_COMPLETE
}

interface ISoundManager {
    var isSoundEnabled: Boolean
    var masterVolume: Float // 0.0f to 1.0f
    fun playPickup()
    fun playPlace()
    fun playInvalidMove()
    fun playLineClear()
    fun playCombo(level: Int)
    fun playGameOver()
    fun playLevelComplete()
    fun release()
}

/**
 * Production-grade SoundManager using Android SoundPool for low-latency game SFX.
 * Features:
 * - Resource preloading from res/raw/
 * - Cooldown throttling to prevent audio clipping during fast gestures
 * - Dynamic pitch variation for combo escalation
 * - Lifecycle-aware resource disposal
 */
class SoundManager(private val context: Context) : ISoundManager {

    companion object {
        private const val TAG = "SoundManager"
        private const val MAX_STREAMS = 6
        private const val THROTTLE_INTERVAL_MS = 60L
    }

    override var isSoundEnabled: Boolean = true
    override var masterVolume: Float = 0.8f
        set(value) {
            field = value.coerceIn(0.0f, 1.0f)
        }

    private val soundPool: SoundPool
    private val soundIdMap = mutableMapOf<SoundEvent, Int>()
    private val loadedSounds = mutableSetOf<Int>()
    private val lastPlayTime = mutableMapOf<SoundEvent, Long>()

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setMaxStreams(MAX_STREAMS)
            .setAudioAttributes(audioAttributes)
            .build()

        soundPool.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0) {
                loadedSounds.add(sampleId)
            } else {
                Log.w(TAG, "Failed to load audio sample: $sampleId")
            }
        }

        loadSounds()
    }

    private fun loadSounds() {
        val packageName = context.packageName

        val soundMapping = listOf(
            SoundEvent.PIECE_PICKUP to "sfx_pickup",
            SoundEvent.PIECE_PLACE to "sfx_place",
            SoundEvent.INVALID_MOVE to "sfx_invalid",
            SoundEvent.LINE_CLEAR to "sfx_line_clear",
            SoundEvent.COMBO to "sfx_combo",
            SoundEvent.GAME_OVER to "sfx_game_over",
            SoundEvent.LEVEL_COMPLETE to "sfx_level_complete"
        )

        for ((event, fileName) in soundMapping) {
            val resId = context.resources.getIdentifier(fileName, "raw", packageName)
            if (resId != 0) {
                try {
                    val soundId = soundPool.load(context, resId, 1)
                    soundIdMap[event] = soundId
                } catch (e: Exception) {
                    Log.e(TAG, "Error loading sound resource: $fileName", e)
                }
            } else {
                Log.d(TAG, "Audio asset '$fileName.wav' not found in res/raw (sound fallback enabled)")
            }
        }
    }

    private fun play(event: SoundEvent, rate: Float = 1.0f) {
        if (!isSoundEnabled || masterVolume <= 0.001f) return

        val now = System.currentTimeMillis()
        val lastTime = lastPlayTime[event] ?: 0L
        if (now - lastTime < THROTTLE_INTERVAL_MS) {
            return // Skip rapid repeated fire to avoid distorted overlapping
        }
        lastPlayTime[event] = now

        val soundId = soundIdMap[event] ?: return
        if (!loadedSounds.contains(soundId)) return

        try {
            soundPool.play(
                soundId,
                masterVolume, // left volume
                masterVolume, // right volume
                1,            // priority
                0,            // loop (0 = no loop)
                rate.coerceIn(0.5f, 2.0f)
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error playing sound: $event", e)
        }
    }

    override fun playPickup() {
        play(SoundEvent.PIECE_PICKUP, rate = 1.1f)
    }

    override fun playPlace() {
        play(SoundEvent.PIECE_PLACE, rate = 1.0f)
    }

    override fun playInvalidMove() {
        play(SoundEvent.INVALID_MOVE, rate = 0.9f)
    }

    override fun playLineClear() {
        play(SoundEvent.LINE_CLEAR, rate = 1.0f)
    }

    override fun playCombo(level: Int) {
        // Escalate pitch with higher combo streaks
        val pitchMultiplier = 1.0f + ((level - 1) * 0.12f).coerceAtMost(0.8f)
        play(SoundEvent.COMBO, rate = pitchMultiplier)
    }

    override fun playGameOver() {
        play(SoundEvent.GAME_OVER, rate = 0.95f)
    }

    override fun playLevelComplete() {
        play(SoundEvent.LEVEL_COMPLETE, rate = 1.0f)
    }

    override fun release() {
        try {
            soundPool.release()
            soundIdMap.clear()
            loadedSounds.clear()
        } catch (e: Exception) {
            Log.e(TAG, "Error releasing SoundPool", e)
        }
    }
}
