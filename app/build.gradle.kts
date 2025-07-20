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
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.gtamap"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

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
    // Core Android dependencies, updated to more recent versions.
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // MapLibre dependencies. The duplicate annotation plugin has been removed.
    implementation("org.maplibre.gl:android-sdk:11.0.0")
    implementation("org.maplibre.gl:android-plugin-annotation-v9:3.0.0")

    // Testing dependencies
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}