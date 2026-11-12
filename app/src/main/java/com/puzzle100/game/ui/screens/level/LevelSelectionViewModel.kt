package com.puzzle100.game.ui.screens.level

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.puzzle100.game.data.database.GameDatabase
import com.puzzle100.game.data.repository.LevelResultRepository
import com.puzzle100.game.data.repository.PlayerProgressRepository
import com.puzzle100.game.model.LevelDifficulty
import com.puzzle100.game.model.LevelInfo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class LevelSelectionUiState(
    val selectedChapter: Int = 1,
    val unlockedLevel: Int = 1,
    val levels: List<LevelInfo> = LevelInfo.createAllLevels()
) {
    val filteredLevels: List<LevelInfo>
        get() = levels.filter { it.difficulty.chapter == selectedChapter }
}

class LevelSelectionViewModel(
    private var playerProgressRepository: PlayerProgressRepository? = null,
    private var levelResultRepository: LevelResultRepository? = null
) : ViewModel() {

    private val _uiState = MutableStateFlow(LevelSelectionUiState())
    val uiState: StateFlow<LevelSelectionUiState> = _uiState.asStateFlow()

    fun attachContext(context: Context) {
        if (playerProgressRepository == null || levelResultRepository == null) {
            val db = GameDatabase.getInstance(context)
            playerProgressRepository = playerProgressRepository ?: PlayerProgressRepository(db.playerProgressDao())
            levelResultRepository = levelResultRepository ?: LevelResultRepository(db.levelResultDao())
            observeData()
        }
    }

    private fun observeData() {
        val progressFlow = playerProgressRepository?.playerProgressFlow
        val resultsFlow = levelResultRepository?.allLevelResultsFlow

        if (progressFlow != null && resultsFlow != null) {
            viewModelScope.launch {
                combine(progressFlow, resultsFlow) { progress, results ->
                    val resultMap = results.associateBy { it.levelId }
                    val unlocked = progress.currentUnlockedLevel

                    val updatedLevels = (1..100).map { level ->
                        val diff = when {
                            level <= 25 -> LevelDifficulty.BEGINNER
                            level <= 50 -> LevelDifficulty.APPRENTICE
                            level <= 75 -> LevelDifficulty.EXPERT
                            else -> LevelDifficulty.MASTER
                        }
                        val target = 500 + (level * 150)
                        val res = resultMap[level]
                        val isUnlocked = level <= unlocked
                        val stars = res?.stars ?: 0

                        LevelInfo(
                            levelNumber = level,
                            targetScore = target,
                            stars = stars,
                            isUnlocked = isUnlocked,
                            difficulty = diff
                        )
                    }

                    Pair(unlocked, updatedLevels)
                }.collect { (unlocked, updatedLevels) ->
                    _uiState.value = _uiState.value.copy(
                        unlockedLevel = unlocked,
                        levels = updatedLevels
                    )
                }
            }
        }
    }

    fun selectChapter(chapter: Int) {
        _uiState.value = _uiState.value.copy(selectedChapter = chapter)
    }
}
