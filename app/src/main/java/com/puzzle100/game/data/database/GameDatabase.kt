package com.puzzle100.game.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        PlayerProgressEntity::class,
        LevelResultEntity::class,
        ActiveGameEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class GameDatabase : RoomDatabase() {

    abstract fun playerProgressDao(): PlayerProgressDao
    abstract fun levelResultDao(): LevelResultDao
    abstract fun activeGameDao(): ActiveGameDao

    companion object {
        const val DATABASE_NAME = "puzzle100_database"

        @Volatile
        private var INSTANCE: GameDatabase? = null

        fun getInstance(context: Context): GameDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }
        }

        private fun buildDatabase(context: Context): GameDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                GameDatabase::class.java,
                DATABASE_NAME
            )
            .addCallback(object : Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    // Prepopulate initial progress without destructive migration
                    CoroutineScope(Dispatchers.IO).launch {
                        val instance = getInstance(context)
                        instance.playerProgressDao().insertOrUpdate(
                            PlayerProgressEntity(
                                id = 1,
                                currentUnlockedLevel = 1,
                                currentLevel = 1,
                                totalScore = 0L,
                                totalLevelsCompleted = 0,
                                totalStars = 0,
                                highestScore = 0,
                                totalLinesCleared = 0,
                                highestCombo = 0,
                                lastPlayedTimestamp = System.currentTimeMillis()
                            )
                        )
                        // Seed first level as accessible
                        instance.levelResultDao().insertOrReplace(
                            LevelResultEntity(
                                levelId = 1,
                                bestScore = 0,
                                stars = 0,
                                timesPlayed = 0,
                                completed = false
                            )
                        )
                    }
                }
            })
            .build()
        }
    }
}
