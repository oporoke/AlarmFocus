###########################
# Core Android Components
###########################
# Keep Application, Activities, Fragments, Services, BroadcastReceivers
-keep class * extends android.app.Application
-keep class * extends android.app.Activity
-keep class * extends android.app.Service
-keep class * extends android.content.BroadcastReceiver


###########################
# Jetpack Compose
###########################
# Keep Compose classes & functions
-keep class androidx.compose.** { *; }
-keep class androidx.lifecycle.** { *; }
-keepclassmembers class androidx.compose.** { *; }
-keepclassmembers class androidx.lifecycle.** { *; }

# Needed to avoid stripping Compose compiler metadata
-keep class kotlin.Metadata { *; }

###########################
# Dependency Injection (Hilt / Dagger)
###########################
-keep class dagger.** { *; }
-keep interface dagger.** { *; }
-keep class javax.inject.** { *; }
-keep interface javax.inject.** { *; }
-keep class hilt_aggregated_deps.** { *; }
-keep class dagger.hilt.internal.** { *; }

###########################
# Networking (Retrofit + Moshi/Gson)
###########################
# Keep Retrofit interfaces
-keep interface retrofit2.** { *; }

# Keep Moshi-generated adapters
-keep class com.squareup.moshi.** { *; }
-keep @com.squareup.moshi.JsonClass class * { *; }

# Keep Gson model classes
-keep class com.google.gson.annotations.SerializedName
-keep class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

###########################
# Image Loading (Coil)
###########################
-keep class coil.** { *; }

###########################
# Room Database
###########################
-keep class androidx.room.** { *; }
-keep @androidx.room.* class * { *; }
-keep class * extends androidx.room.RoomDatabase

###########################
# Kotlin Coroutines
###########################
-keep class kotlinx.coroutines.** { *; }

###########################
# Miscellaneous
###########################
# Keep all annotations
-keepattributes *Annotation*

# Keep enums
-keepclassmembers enum * { *; }

# Optional: Keep source file & line numbers for crash reports
#-keepattributes SourceFile,LineNumberTable
#-renamesourcefileattribute SourceFile
