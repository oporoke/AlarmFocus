plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("kotlin-parcelize")

    id("com.google.devtools.ksp") version "2.0.21-1.0.25"
}

android {
    namespace = "com.omondit.alarmfocus"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.omondit.alarmfocus"
        minSdk = 29
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    implementation(libs.androidx.foundation)
    implementation(libs.ui)
    // Material 3 components
    implementation(libs.material3)
    implementation(libs.androidx.material3)
    // Core Material icons (basic set)
    implementation(libs.androidx.material.icons.core)
    // Extended Material icons (needed for Alarm)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
    // Navigation
    implementation(libs.androidx.navigation.compose)
    // Room Database
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    // ViewModel
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.barcode.scanning)
    implementation(libs.androidx.work.runtime.ktx)
    // Coroutines
    implementation(libs.kotlinx.coroutines.android)
    // Work Manager (for background tasks)
    implementation(libs.androidx.work.runtime.ktx)
    // Media handling
    implementation(libs.androidx.media)
    // Work Manager
    implementation(libs.androidx.work.runtime.ktx)
    // Permissions
    implementation(libs.accompanist.permissions)
    // JSON handling
    implementation(libs.kotlinx.serialization.json)

    // handle permissions Compose UI:
    implementation(libs.accompanist.permissions)

    // Camera and barcode scanning
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2.v150)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view.v150)

    // ML Kit Vision for barcode scanning
    implementation(libs.barcode.scanning)

    // ML Kit Image Labeling for photo comparison
    implementation("com.google.mlkit:image-labeling:17.0.9")

    // Additional camera extensions if needed
    implementation(libs.androidx.camera.extensions)

    // AndroidX Security for encryption
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // Testing libraries
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    testImplementation("io.mockk:mockk:1.13.8")
    testImplementation("org.robolectric:robolectric:4.11.1")
    testImplementation("androidx.test:core:1.5.0")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.5.4")
    androidTestImplementation("io.mockk:mockk-android:1.13.8")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test:rules:1.5.0")
}
