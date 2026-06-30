plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.vali.pilot"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.vali.pilot"
        minSdk = 24          // Android 7.0 — installs on modern Android (15+ requires >= 24)
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        // Debug builds are automatically signed with the debug keystore,
        // which is exactly the "debug-signed APK" you can sideload.
        getByName("debug") {
            isMinifyEnabled = false
        }
        getByName("release") {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
}
