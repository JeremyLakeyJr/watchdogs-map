import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

// This block safely reads properties from your local.properties file.
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
}

android {
    namespace = "com.jeremylakeyjr.watchdogsmap"
    // Using the latest stable SDK is recommended over preview versions for stability.
    compileSdk = 34

    defaultConfig {
        applicationId = "com.jeremylakeyjr.watchdogsmap"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Specifies the CPU architectures to build for.
        ndk {
            abiFilters.addAll(listOf("arm64-v8a", "x86_64"))
        }

        // OpenStreetMap doesn't require an API key
        // Removed Google Maps API key configuration
    }

    // This block now checks if keystore properties exist before creating the signing config.
    // This prevents Gradle sync errors if you haven't set up a release keystore.
    val keystoreFile = localProperties.getProperty("keystore.file")
    if (keystoreFile != null && rootProject.file(keystoreFile).exists()) {
        signingConfigs {
            create("release") {
                storeFile = rootProject.file(keystoreFile)
                storePassword = localProperties.getProperty("keystore.password")
                keyAlias = localProperties.getProperty("key.alias")
                keyPassword = localProperties.getProperty("key.password")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            // Only apply the signing config if it was successfully created.
            if (signingConfigs.findByName("release") != null) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        buildConfig = true
        viewBinding = true
    }
}

dependencies {
    // Core Android dependencies
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // OpenStreetMap dependencies (free alternative to Google Maps)
    implementation("org.osmdroid:osmdroid-android:6.1.18")
    // Google Play Services location for device location (still free)
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // Spotify SDK
    implementation("com.spotify.android:spotify-app-remote:0.7.2")
    implementation("com.spotify.android:spotify-auth:1.2.5")

    // Testing dependencies
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}