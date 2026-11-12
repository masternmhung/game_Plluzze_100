# PUZZLE 100 — Android Native Game Architecture

## 📱 Tech Stack
- **Language**: Kotlin 2.1.0
- **Android Gradle Plugin (AGP)**: 8.8.0
- **Gradle Version**: 8.11.1
- **Kotlin Symbol Processing (KSP)**: 2.1.0-1.0.29
- **Room Database**: 2.6.1 (with KSP Room Compiler `androidx.room:room-compiler:2.6.1`)
- **Jetpack DataStore**: 1.1.2 (`androidx.datastore:datastore-preferences:1.1.2`)
- **JSON Serialization**: Google Gson 2.11.0
- **UI Framework**: Jetpack Compose (BOM 2025.02.00)
- **Design System**: Material 3 (Material Design 3.0)
- **Architecture**: MVVM (Model-View-ViewModel) with StateFlow / SharedFlow & Coroutines
- **Navigation**: Jetpack Navigation Compose 2.8.7
- **Target SDK**: Android 15 (API 35) | **Compile SDK**: 35 | **Min SDK**: API 26
- **JVM Target**: Java 17

---

## 🎨 Design System: Dark Premium
- **Background**: Deep Space Navy (`#090D1A`, `#0F172A`, `#1E293B`)
- **Vibrant Puzzle Blocks**:
  - 💎 **Turquoise (Ngọc lam)**: `#00E5FF`
  - 🍊 **Cam (Orange)**: `#FF6D00`
  - 🔮 **Tím (Purple)**: `#7C4DFF`
  - 🌸 **Hồng (Pink)**: `#FF4081`
- **Scoring & Highlights**: Star Gold (`#FFD600`), Pure White (`#F8FAFC`)

---

## 📂 Complete Android Native Project Tree

```
├── build.gradle.kts                          # Root build config with AGP 8.8.0, Kotlin 2.1.0, KSP 2.1.0-1.0.29
├── settings.gradle.kts                       # Root settings with Google & MavenCentral repositories
├── gradle.properties                         # Gradle JVM args, AndroidX & non-transitive R-class flags
├── gradlew                                   # Gradle Wrapper script (Linux / macOS)
├── gradlew.bat                               # Gradle Wrapper script (Windows CMD / PowerShell)
├── gradle/
│   ├── libs.versions.toml                    # Version Catalog (AGP, Kotlin, KSP, Room, DataStore, Compose)
│   └── wrapper/
│       ├── gradle-wrapper.properties         # Distribution URL: gradle-8.11.1-bin.zip
│       └── gradle-wrapper.jar                # Authentic Gradle 8.11.1 Wrapper Binary (43,583 bytes)
├── app/
│   ├── build.gradle.kts                      # App module with KSP Room Compiler, DataStore, Compose M3
│   ├── proguard-rules.pro                    # R8 / ProGuard rules for Room, Gson, Coroutines, Compose
│   └── src/
│       ├── test/java/com/puzzle100/game/
│       │   ├── GameEngineTest.kt             # 12 Unit Tests covering pure Game Engine (10x10, placement, lines, combo)
│       │   ├── AudioHapticTest.kt            # 8 Unit Tests covering SoundPool & Vibrator logic
│       │   └── PersistenceTest.kt            # 19 Unit Tests covering Room DB, TypeConverters, DataStore, Anti-Downgrade
│       └── main/
│           ├── AndroidManifest.xml           # Android Manifest (VIBRATE permission, portrait orientation, ic_launcher)
│           ├── res/
│           │   ├── drawable/
│           │   │   └── ic_launcher.xml       # 4-Block signature vector launcher icon
│           │   ├── values/
│           │   │   └── strings.xml           # app_name = "PUZZLE 100"
│           │   └── raw/                      # 7 game sound effects in WAV format
│           │       ├── sfx_pickup.wav        # PIECE_PICKUP
│           │       ├── sfx_place.wav         # PIECE_PLACE
│           │       ├── sfx_invalid.wav       # INVALID_MOVE
│           │       ├── sfx_line_clear.wav    # LINE_CLEAR
│           │       ├── sfx_combo.wav         # COMBO
│           │       ├── sfx_game_over.wav     # GAME_OVER
│           │       └── sfx_level_complete.wav# LEVEL_COMPLETE
│           └── java/com/puzzle100/game/
│               ├── MainActivity.kt           # Main Activity & Edge-to-Edge setup
│               ├── audio/
│               │   └── SoundManager.kt       # Android SoundPool engine & throttling
│               ├── haptics/
│               │   └── HapticManager.kt      # Android Vibrator & VibrationEffect engine
│               ├── data/
│               │   ├── preferences/
│               │   │   └── GamePreferencesRepository.kt # Jetpack DataStore (sound, haptic, volume)
│               │   ├── database/
│               │   │   ├── PlayerProgressEntity.kt      # Table: player_progress
│               │   │   ├── PlayerProgressDao.kt
│               │   │   ├── LevelResultEntity.kt         # Table: level_results
│               │   │   ├── LevelResultDao.kt
│               │   │   ├── ActiveGameEntity.kt          # Table: active_game
│               │   │   ├── ActiveGameDao.kt
│               │   │   ├── Converters.kt                # TypeConverters for 10x10 board & tray pieces
│               │   │   └── GameDatabase.kt              # RoomDatabase singleton with safe initial callback
│               │   └── repository/
│               │       ├── PlayerProgressRepository.kt  # Level unlock & stats tracking
│               │       ├── LevelResultRepository.kt     # Star calculation & anti-downgrade protection
│               │       └── ActiveGameRepository.kt      # Discrete session saving & restoration
│               ├── engine/
│               │   └── GameEngine.kt         # Core GameEngine & ScoreEvaluator
│               ├── model/
│               │   ├── Block.kt              # 32 distinct puzzle shapes & color types
│               │   ├── Level.kt              # 100 Levels system (4 Chapters)
│               │   └── GameState.kt          # 10x10 Board, DragState, GhostPreview, Combo
│               ├── navigation/
│               │   ├── Screen.kt             # Sealed class destination routes
│               │   └── NavGraph.kt           # Jetpack Compose Navigation Graph
│               └── ui/
│                   ├── screens/
│                   │   ├── splash/
│                   │   │   └── SplashScreen.kt          # Animated brand intro
│                   │   ├── home/
│                   │   │   ├── HomeScreen.kt            # Continue Game button, stars, settings
│                   │   │   └── HomeViewModel.kt
│                   │   ├── level/
│                   │   │   ├── LevelSelectionScreen.kt  # 100 levels grid (Chapters 1-4)
│                   │   │   └── LevelSelectionViewModel.kt
│                   │   └── game/
│                   │       ├── GameplayScreen.kt        # 10x10 Board, Tray, HUD, Ghost Preview
│                   │       └── GameViewModel.kt         # StateFlow & SharedFlow decoupled pipeline
│                   └── theme/
│                       ├── Color.kt          # Dark Premium Palette
│                       ├── Theme.kt          # Material3 ColorScheme
│                       └── Type.kt           # Typography
```

---

## 🚀 How to Build on Windows in Android Studio

1. **Prerequisites**:
   - Install **Android Studio** (Ladybug 2024.2.1, Iguana, or Hedgehog).
   - JDK: **Java 17** (bundled with Android Studio as `jbr-17`).
   - Android SDK: Platform API 35 with Build Tools 35.0.0.

2. **Open Project**:
   - Launch Android Studio -> **Open** -> Select the extracted folder `PUZZLE_100_Android_Project`.
   - Android Studio will automatically recognize Gradle 8.11.1 and sync dependencies via `gradle-wrapper.jar`.

3. **Build Debug APK**:
   - Via GUI: Menu **Build** -> **Build Bundle(s) / APK(s)** -> **Build APK(s)**.
   - Or via Windows Terminal / PowerShell:
     ```cmd
     gradlew.bat assembleDebug
     ```
   - Generated APK location:
     ```
     app/build/outputs/apk/debug/app-debug.apk
     ```
     (Installs directly onto any Android phone running Android 8.0 Oreo / API 26 or higher).

4. **Run Unit Tests**:
   - Via Windows Terminal / PowerShell:
     ```cmd
     gradlew.bat testDebugUnitTest
     ```
   - Executes all 39 Unit Tests in `GameEngineTest.kt`, `AudioHapticTest.kt`, and `PersistenceTest.kt`.
