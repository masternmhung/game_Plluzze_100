package com.puzzle100.game.model

enum class LevelDifficulty(val displayName: String, val chapter: Int) {
    BEGINNER("Tân thủ", 1),       // Levels 1-25
    APPRENTICE("Tập sự", 2),     // Levels 26-50
    EXPERT("Chuyên gia", 3),      // Levels 51-75
    MASTER("Bậc thầy", 4)        // Levels 76-100
}

data class LevelInfo(
    val levelNumber: Int,
    val targetScore: Int,
    val stars: Int = 0,         // 0 to 3
    val isUnlocked: Boolean = false,
    val difficulty: LevelDifficulty
) {
    companion object {
        fun createAllLevels(): List<LevelInfo> {
            return (1..100).map { level ->
                val diff = when {
                    level <= 25 -> LevelDifficulty.BEGINNER
                    level <= 50 -> LevelDifficulty.APPRENTICE
                    level <= 75 -> LevelDifficulty.EXPERT
                    else -> LevelDifficulty.MASTER
                }
                val target = 500 + (level * 150)
                // Seed unlocked state for demo preview: first 5 levels unlocked
                val isUnlocked = level <= 5
                val initialStars = when (level) {
                    1 -> 3
                    2 -> 3
                    3 -> 2
                    4 -> 1
                    else -> 0
                }
                LevelInfo(
                    levelNumber = level,
                    targetScore = target,
                    stars = initialStars,
                    isUnlocked = isUnlocked,
                    difficulty = diff
                )
            }
        }
    }
}
