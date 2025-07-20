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

        // This correctly creates a string field in BuildConfig.java.
        // The value from local.properties is wrapped in escaped quotes.
        buildConfigField("String", "MAP_API_KEY", "\"${localProperties.getProperty("MAP_API_KEY")}\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    // Updated to Java 17, the modern standard for Android development.
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
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
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // MapLibre dependencies are up-to-date.
    implementation("org.maplibre.gl:android-sdk:11.0.0")
    implementation("org.maplibre.gl:android-plugin-annotation-v9:3.0.0")

    // Testing dependencies updated to their latest stable versions.
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}