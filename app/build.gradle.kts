import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

// This block safely reads properties from your local.properties file.
// Make sure your API key in that file does NOT have quotes around it.
// It should look like this: MAP_API_KEY=your_actual_key_here
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(localPropertiesFile.inputStream())
}

android {
    namespace = "com.example.gtamap"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.gtamap"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Specifies the CPU architectures to build for. 'arm64-v8a' is for modern
        // physical devices like your Pixel 6, and 'x86_64' is for modern emulators.
        // This resolves the "app not compatible" installation error.
        ndk {
            abiFilters.addAll(listOf("arm64-v8a", "x86_64"))
        }

        // This correctly creates a string field in BuildConfig.java and a manifest placeholder.
        val apiKey = localProperties.getProperty("MAPS_API_KEY") ?: ""
        buildConfigField("String", "MAPS_API_KEY", "\"$apiKey\"")
        manifestPlaceholders["MAPS_API_KEY"] = apiKey
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    kotlin {
        jvmToolchain(8)
    }

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    // Core Android dependencies are up-to-date.
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    // Google Maps dependencies
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.maps.android:maps-ktx:3.4.0")
    implementation("com.google.maps.android:maps-utils-ktx:3.4.0")
    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // Testing dependencies updated to their latest stable versions.
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}