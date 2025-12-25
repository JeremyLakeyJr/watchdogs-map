// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        // Try different Google Maven mirrors
        maven {
            url = uri("https://maven.google.com")
            // Fallback to dl.google.com if above doesn't work
        }
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.1.4")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.22")
    }
}
