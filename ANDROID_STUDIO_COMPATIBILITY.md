# Android Studio Compatibility Guide

This document ensures the Watch Dogs 2 Map App can be compiled and run in Android Studio without issues.

## ✅ Verified Compatibility

This project has been configured and tested for compatibility with:

- **Android Studio**: Hedgehog (2023.1.1) and newer
- **AGP (Android Gradle Plugin)**: 8.2.2
- **Gradle**: 8.5 (via wrapper)
- **Kotlin**: 1.9.22
- **Java/JDK**: 17
- **Compile SDK**: 34 (Android 14)
- **Target SDK**: 34 (Android 14)
- **Min SDK**: 24 (Android 7.0)

## 🏗️ Project Structure

The project uses a standard single-module Android app structure:

```
watchdogs-map/
├── app/                          # Main application module
│   ├── build.gradle.kts         # Module-level build configuration (Kotlin DSL)
│   ├── proguard-rules.pro       # ProGuard configuration for release builds
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── java/com/jeremylakeyjr/watchdogsmap/
│       │   └── res/
│       └── test/
├── build.gradle.kts             # Root-level build configuration
├── settings.gradle.kts          # Project settings and module includes
├── gradle.properties            # Gradle build properties
└── gradle/
    └── wrapper/                 # Gradle wrapper (version 8.5)
```

## 📋 Module Configuration

### All Modules Included

The project has one application module that is properly configured:

**In `settings.gradle.kts`:**
```kotlin
include(":app")
```

This ensures Android Studio recognizes and can compile the app module.

### Build Configuration

**Root `build.gradle.kts`:**
- Declares plugins: `com.android.application` and `org.jetbrains.kotlin.android`
- Uses version 8.2.2 for AGP and 1.9.22 for Kotlin
- Configured with proper repositories (Google, Maven Central)

**Module `app/build.gradle.kts`:**
- Namespace: `com.jeremylakeyjr.watchdogsmap`
- Build features enabled: `buildConfig = true`, `viewBinding = true`
- Java version: 17 (source and target)
- Kotlin JVM target: 17
- ProGuard rules included for release builds

## 🔧 Key Configuration Files

### 1. gradle.properties

Essential properties set for Android Studio:
```properties
android.useAndroidX=true                # Use AndroidX libraries
android.enableJetifier=true             # Auto-convert legacy libraries
android.nonTransitiveRClass=true        # Optimize R class generation
kotlin.code.style=official              # Use official Kotlin code style
org.gradle.jvmargs=-Xmx2048m           # Allocate 2GB RAM for Gradle
```

### 2. gradle-wrapper.properties

Locked to Gradle 8.5 for consistency:
```properties
distributionUrl=https\://services.gradle.org/distributions/gradle-8.5-bin.zip
```

### 3. .idea Configuration

Android Studio project settings are properly configured:
- **misc.xml**: JDK 17 (languageLevel="JDK_17")
- **compiler.xml**: Bytecode target level 17
- **vcs.xml**: Git version control configured
- **modules**: Generated automatically by Gradle (not committed)

## 🎯 Dependency Management

### Repository Configuration

All dependencies are resolved from these repositories:
```kotlin
repositories {
    google()                           # Android and Google libraries
    mavenCentral()                     # Standard Maven packages
    maven { url = uri("https://repo.spotify.com/public") }  # Spotify SDK
}
```

### Key Dependencies

| Dependency | Version | Purpose |
|------------|---------|---------|
| androidx.core:core-ktx | 1.13.1 | AndroidX core functionality |
| androidx.appcompat:appcompat | 1.7.0 | Backward compatibility |
| material | 1.12.0 | Material Design components |
| osmdroid-android | 6.1.18 | OpenStreetMap rendering |
| play-services-location | 21.3.0 | Device location services |
| kotlinx-coroutines-android | 1.8.1 | Kotlin coroutines |
| spotify-app-remote | 0.7.2 | Spotify integration |
| spotify-auth | 1.2.5 | Spotify authentication |

## ✔️ Pre-Flight Checklist

Before opening in Android Studio, verify:

- [ ] `settings.gradle.kts` includes `:app` module
- [ ] `build.gradle.kts` files use `.kts` extension (Kotlin DSL)
- [ ] No conflicting `build.gradle` (Groovy) files exist
- [ ] `gradle-wrapper.properties` specifies Gradle 8.5
- [ ] `proguard-rules.pro` exists in app directory
- [ ] Java 17 or newer is installed

## 🚀 First-Time Setup in Android Studio

1. **Open Project**
   - File > Open
   - Select the `watchdogs-map` directory
   - Click OK

2. **Wait for Gradle Sync**
   - Android Studio will automatically:
     - Download Gradle 8.5 if needed
     - Download all dependencies
     - Configure the app module
     - Index the project
   - This may take 2-5 minutes on first run

3. **Verify Module Recognition**
   - Check Project panel (left side)
   - Should see `app` module with folders:
     - `manifests`
     - `java`
     - `res`
   - If module is not showing, see [TROUBLESHOOTING.md](TROUBLESHOOTING.md)

4. **Configure JDK (if needed)**
   - File > Project Structure > SDK Location
   - Gradle JDK should be "JDK 17" or newer
   - Or use "Embedded JDK" (Android Studio includes JDK 17)

5. **Build Project**
   - Build > Make Project (Ctrl+F9 / Cmd+F9)
   - Should complete without errors

## 🐛 Common Issues and Solutions

### Module Not Recognized

**Problem**: Android Studio doesn't show the app module

**Solution**:
1. File > Invalidate Caches / Restart
2. Wait for re-indexing
3. File > Sync Project with Gradle Files

### Gradle Sync Failed

**Problem**: Red errors in Gradle sync output

**Solution**:
1. Check internet connection (dependencies need to download)
2. Verify `settings.gradle.kts` has correct repository URLs
3. Delete `.gradle` folder and retry
4. Check [TROUBLESHOOTING.md](TROUBLESHOOTING.md) for detailed solutions

### Java Version Mismatch

**Problem**: "Unsupported class file major version" error

**Solution**:
1. File > Settings > Build, Execution, Deployment > Build Tools > Gradle
2. Set "Gradle JDK" to JDK 17 or newer
3. Sync project again

### Kotlin Plugin Version Mismatch

**Problem**: "Kotlin plugin version mismatch" warning

**Solution**:
1. Help > Check for Updates
2. Update Kotlin plugin to 1.9.22 or newer
3. Restart Android Studio

## 📱 Building and Running

### Debug Build

```bash
# Command line
./gradlew assembleDebug

# Output: app/build/outputs/apk/debug/app-debug.apk
```

### Release Build (requires signing config)

```bash
# Command line
./gradlew assembleRelease

# Output: app/build/outputs/apk/release/app-release.apk
```

### Install on Device

```bash
# Via ADB
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Or use Android Studio "Run" button (Shift+F10)
```

## 🔐 Release Signing (Optional)

To create a signed release build:

1. Create `local.properties` from `local.properties.template`
2. Add signing configuration:
   ```properties
   keystore.file=path/to/release.keystore
   keystore.password=your_keystore_password
   key.alias=your_key_alias
   key.password=your_key_password
   ```
3. Build release: `./gradlew assembleRelease`

## 📚 Additional Resources

- [Android Studio User Guide](https://developer.android.com/studio/intro)
- [Gradle Build Guide](https://docs.gradle.org/current/userguide/userguide.html)
- [AGP Release Notes](https://developer.android.com/studio/releases/gradle-plugin)
- [Kotlin DSL Primer](https://docs.gradle.org/current/userguide/kotlin_dsl.html)

## 🆘 Need Help?

If you encounter issues:

1. Check [TROUBLESHOOTING.md](TROUBLESHOOTING.md)
2. Check [SETUP.md](SETUP.md)
3. Review Android Studio's "Build" output panel for errors
4. Check Logcat for runtime errors
5. Open an issue on GitHub with:
   - Android Studio version
   - Error messages
   - Build output
   - Screenshots

---

**Last Updated**: Compatible with Android Studio Hedgehog (2023.1.1) and newer

**Tested On**:
- Windows 11 with Android Studio Hedgehog
- macOS 13+ with Android Studio Hedgehog
- Linux (Ubuntu 22.04) with Android Studio Hedgehog
