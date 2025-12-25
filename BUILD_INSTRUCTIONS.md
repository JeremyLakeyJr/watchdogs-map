# Build Instructions

## Prerequisites

- **JDK 17** (included with Android Studio)
- **Gradle 8.5** (included via wrapper)
- **Network access to Google's Maven repository** (`dl.google.com`)

## Building the Project

### Standard Build

```bash
./gradlew clean build
```

### Build APK

```bash
./gradlew assembleDebug
```

The APK will be located at: `app/build/outputs/apk/debug/app-debug.apk`

### Build Release APK (Signed)

1. Create a `local.properties` file with your keystore information:
```properties
keystore.file=path/to/your/keystore.jks
keystore.password=your_keystore_password
key.alias=your_key_alias
key.password=your_key_password
```

2. Build the release APK:
```bash
./gradlew assembleRelease
```

## Project Configuration

### Gradle Version
- **Gradle**: 8.5
- **Android Gradle Plugin (AGP)**: 8.1.4
- **Kotlin**: 1.9.22

### Android SDK
- **Compile SDK**: 34 (Android 14)
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 34 (Android 14)
- **Java Version**: 17

## Network Requirements

This Android project requires access to the following repositories:

1. **Google Maven Repository** (`https://dl.google.com/dl/android/maven2/`)
   - Required for: Android Gradle Plugin, AndroidX libraries, Google Play Services
   - **Critical**: The build will fail without access to this repository

2. **Maven Central** (`https://repo1.maven.org/maven2/`)
   - Required for: General Java/Kotlin dependencies

3. **Gradle Plugin Portal** (`https://plugins.gradle.org/`)
   - Required for: Gradle plugins

4. **Spotify Maven** (`https://repo.spotify.com/public`)
   - Required for: Spotify SDK integration

### Known Issues

#### Restricted Network Environments

If you're building in a restricted environment (corporate proxy, firewalls, etc.) where `dl.google.com` is blocked:

**Symptoms:**
```
Could not GET 'https://dl.google.com/dl/android/maven2/...'
> dl.google.com: No address associated with hostname
```

**Solutions:**

1. **Use a VPN or proxy** that allows access to Google domains
2. **Configure a repository mirror** in your `gradle.properties`:
   ```properties
   systemProp.https.proxyHost=your.proxy.host
   systemProp.https.proxyPort=8080
   ```
3. **Build on a different machine/environment** with unrestricted internet access
4. **Use Android Studio** which may have better proxy support

## Alternative Build Environments

If you cannot build in your current environment, try:

### 1. Local Development Machine
```bash
# Clone the repository
git clone https://github.com/JeremyLakeyJr/watchdogs-map.git
cd watchdogs-map

# Build with Android Studio or command line
./gradlew build
```

### 2. GitHub Actions (Recommended for CI/CD)
Create `.github/workflows/android-build.yml`:
```yaml
name: Android Build

on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]

jobs:
  build:
    runs-on: ubuntu-latest
    steps:
    - uses: actions/checkout@v3
    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
    - name: Grant execute permission for gradlew
      run: chmod +x gradlew
    - name: Build with Gradle
      run: ./gradlew build
    - name: Upload APK
      uses: actions/upload-artifact@v3
      with:
        name: app-debug
        path: app/build/outputs/apk/debug/app-debug.apk
```

### 3. Docker Build
```dockerfile
FROM gradle:8.5-jdk17

# Install Android SDK
ENV ANDROID_SDK_ROOT=/opt/android-sdk
RUN mkdir -p $ANDROID_SDK_ROOT

WORKDIR /app
COPY . .

RUN ./gradlew build
```

## Troubleshooting

### Build fails with "Could not resolve"
- **Cause**: Network connectivity issues or blocked repositories
- **Fix**: Verify internet connection and repository accessibility

### "Plugin [id: 'com.android.application'] was not found"
- **Cause**: Google Maven repository is not accessible
- **Fix**: Check network access to `dl.google.com`

### Gradle sync failed
- **Fix**: 
  ```bash
  ./gradlew clean
  rm -rf .gradle
  ./gradlew build --refresh-dependencies
  ```

### Out of memory errors
- **Fix**: Increase Gradle memory in `gradle.properties`:
  ```properties
  org.gradle.jvmargs=-Xmx4096m -Dfile.encoding=UTF-8
  ```

## Build Outputs

After a successful build:
- **Debug APK**: `app/build/outputs/apk/debug/app-debug.apk`
- **Release APK**: `app/build/outputs/apk/release/app-release.apk`
- **Test Results**: `app/build/reports/tests/`
- **Lint Results**: `app/build/reports/lint-results.html`

## Next Steps

After building:
1. Install the APK on an Android device or emulator
2. Grant location permissions when prompted
3. (Optional) Configure Spotify integration in `MainActivity.kt`

## Support

For build issues, check:
1. This document for common solutions
2. [TROUBLESHOOTING.md](TROUBLESHOOTING.md) for app-specific issues
3. [GitHub Issues](https://github.com/JeremyLakeyJr/watchdogs-map/issues) for community help
