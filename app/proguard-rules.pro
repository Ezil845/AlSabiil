# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /home/ph1nex/.nvm/versions/node/v20.19.5/lib/node_modules/@google/gemini-cli/node_modules/@google/gemini-cli-core/dist/src/skills/builtin/skill-creator/SKILL.md
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html#manualediting

# Protect all data classes in the model package from being renamed
# This is crucial for JSON serialization (Azkar, Tafseer, etc.)
-keep class al.sabil.model.** { *; }

# Keep members of data classes that are used for JSON mapping
-keepclassmembers class al.sabil.model.** {
    <fields>;
    <methods>;
}

# Kotlin Serialization support
-keepattributes RuntimeVisibleAnnotations, AnnotationDefault
-keepclassmembers class ** {
    @kotlinx.serialization.Serializable *;
}
-keep class kotlinx.serialization.** { *; }

# Prevent shrinking of Compose-related classes that might be accessed via reflection
-keep class androidx.compose.material.icons.** { *; }
-keep class com.airbnb.lottie.** { *; }

# Standard optimization and shrinking settings
-optimizationpasses 5
-allowaccessmodification
-dontpreverify

# Room Database
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *
