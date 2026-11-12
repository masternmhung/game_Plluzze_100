package com.puzzle100.game.ui.screens.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.puzzle100.game.data.database.GameDatabase
import com.puzzle100.game.data.preferences.GamePreferencesRepository
import com.puzzle100.game.data.repository.ActiveGameRepository
import com.puzzle100.game.data.repository.PlayerProgressRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val currentLevel: Int = 1,
    val totalStars: Int = 0,
    val maxStars: Int = 300,
    val highestScore: Int = 0,
    val totalLevelsCompleted: Int = 0,
    val hasActiveGame: Boolean = false,
    val activeGameLevel: Int = 1,
    val activeGameScore: Int = 0,
    val isSoundEnabled: Boolean = true,
    val isHapticEnabled: Boolean = true,
    val masterVolume: Float = 1.0f,
    val isSettingsDialogOpen: Boolean = false
)

class HomeViewModel(
    private var playerProgressRepository: PlayerProgressRepository? = null,
    private var activeGameRepository: ActiveGameRepository? = null,
    private var preferencesRepository: GamePreferencesRepository? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun attachContext(context: Context) {
        if (playerProgressRepository == null || activeGameRepository == null || preferencesRepository == null) {
            val db = GameDatabase.getInstance(context)
            playerProgressRepository = playerProgressRepository ?: PlayerProgressRepository(db.playerProgressDao())
            activeGameRepository = activeGameRepository ?: ActiveGameRepository(db.activeGameDao())
            preferencesRepository = preferencesRepository ?: GamePreferencesRepository.getInstance(context)
            observeData()
        }
    }

    private fun observeData() {
        playerProgressRepository?.let { repo ->
            viewModelScope.launch {
                repo.playerProgressFlow.collect { progress ->
                    _uiState.update {
                        it.copy(
                            currentLevel = progress.currentUnlockedLevel,
                            totalStars = progress.totalStars,
                            highestScore = progress.highestScore,
                            totalLevelsCompleted = progress.totalLevelsCompleted
                        )
                    }
                }
            }
        }

        activeGameRepository?.let { repo ->
            viewModelScope.launch {
                repo.latestActiveGameFlow.collect { active ->
                    _uiState.update {
                        it.copy(
                            hasActiveGame = active != null,
                            activeGameLevel = active?.levelId ?: 1,
                            activeGameScore = active?.score ?: 0
                        )
                    }
                }
            }
        }

        preferencesRepository?.let { repo ->
            viewModelScope.launch {
                repo.gamePreferencesFlow.collect { prefs ->
                    _uiState.update {
                        it.copy(
                            isSoundEnabled = prefs.soundEnabled,
                            isHapticEnabled = prefs.hapticEnabled,
                            masterVolume = prefs.masterVolume
                        )
                    }
                }
            }
        }
    }

    fun toggleSound() {
        val next = !_uiState.value.isSoundEnabled
        _uiState.update { it.copy(isSoundEnabled = next) }
        viewModelScope.launch {
            preferencesRepository?.updateSoundEnabled(next)
        }
    }

    fun toggleHaptic() {
        val next = !_uiState.value.isHapticEnabled
        _uiState.update { it.copy(isHapticEnabled = next) }
        viewModelScope.launch {
            preferencesRepository?.updateHapticEnabled(next)
        }
    }

    fun setMasterVolume(volume: Float) {
        val clamped = volume.coerceIn(0f, 1f)
        _uiState.update { it.copy(masterVolume = clamped) }
        viewModelScope.launch {
            preferencesRepository?.updateMasterVolume(clamped)
        }
    }

    fun setSettingsDialog(isOpen: Boolean) {
        _uiState.update { it.copy(isSettingsDialogOpen = isOpen) }
    }
}
