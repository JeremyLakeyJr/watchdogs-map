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

        // This correctly creates a string field in BuildConfig and a manifest placeholder.
        val apiKey = localProperties.getProperty("MAPS_API_KEY") ?: ""
        buildConfigField("String", "MAPS_API_KEY", "\"\"$apiKey\"\"")
        manifestPlaceholders["MAPS_API_KEY"] = apiKey
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        buildConfig = true
    }
}

// Repositories have been cleaned up to only include what's necessary for your dependencies.
repositories {
    google()
    mavenCentral()
    maven { url = uri("https://repo.spotify.com/public") }
}

dependencies {
    // Core Android dependencies
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Google Maps dependencies
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.maps.android:maps-ktx:3.4.0")
    implementation("com.google.maps.android:maps-utils-ktx:3.4.0")
    // Updated to the latest stable version for better compatibility and bug fixes.
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // Spotify SDK
    implementation("com.spotify.android:spotify-app-remote:0.7.2")

    // Testing dependencies
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}