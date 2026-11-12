# ProGuard/R8 Rules for PUZZLE 100

# Keep Room database and entity classes from being stripped or renamed
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**
-keep class com.puzzle100.game.data.database.** { *; }
-keep class com.puzzle100.game.data.preferences.** { *; }

# Keep data models used with Gson serialization
-keepattributes Signature
-keepattributes *Annotation*
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keep class com.puzzle100.game.data.database.SerializedCell { *; }
-keep class com.puzzle100.game.model.** { *; }

# Jetpack Compose runtime
-keep class androidx.compose.** { *; }

# Coroutines & StateFlow
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}
