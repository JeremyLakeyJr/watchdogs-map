# Troubleshooting Guide

This guide helps you resolve common issues with the Watch Dogs 2 Map App.

## Build Issues

### "Plugin [id: 'com.android.application'] was not found"

**Symptoms**: Build fails immediately with plugin resolution error

**Solutions**:
1. Check internet connection - Gradle needs to download the Android plugin
2. Verify `settings.gradle.kts` has correct repositories:
   ```kotlin
   pluginManagement {
       repositories {
           google()
           mavenCentral()
           gradlePluginPortal()
       }
   }
   ```
3. Try: File > Invalidate Caches / Restart
4. Delete `.gradle` folder and sync again

### "SDK location not found"

**Symptoms**: Build fails with "SDK location not found. Define location with an ANDROID_SDK_ROOT environment variable"

**Solutions**:
1. Android Studio should create `local.properties` automatically
2. If missing, create it manually:
   ```properties
   sdk.dir=/path/to/your/Android/sdk
   ```
3. On Windows: `sdk.dir=C\:\\Users\\YourName\\AppData\\Local\\Android\\Sdk`
4. On Mac: `sdk.dir=/Users/YourName/Library/Android/sdk`
5. On Linux: `sdk.dir=/home/yourname/Android/Sdk`

### "Manifest merger failed"

**Symptoms**: Build fails with manifest merger errors

**Solutions**:
1. Check `AndroidManifest.xml` for syntax errors
2. Verify all activities are properly declared
3. Clean and rebuild: Build > Clean Project, then Build > Rebuild Project
4. Check for conflicting permissions or activities from libraries

### Gradle Sync Failed

**Symptoms**: Red text in Gradle sync, project won't build

**Solutions**:
1. Check Build Output and Event Log for specific errors
2. Verify internet connection (Gradle downloads dependencies)
3. Check if behind firewall/proxy - configure in gradle.properties:
   ```properties
   systemProp.http.proxyHost=proxy.company.com
   systemProp.http.proxyPort=8080
   systemProp.https.proxyHost=proxy.company.com
   systemProp.https.proxyPort=8080
   ```
4. Try: File > Sync Project with Gradle Files
5. Update Gradle: Help > Check for Updates

### Module Not Recognized / "app" Module Missing

**Symptoms**: Android Studio doesn't recognize the app module, project structure is broken

**Solutions**:
1. **Verify settings.gradle.kts** includes the module:
   ```kotlin
   include(":app")
   ```
2. **Check for conflicting build files**: 
   - Only use `build.gradle.kts` (Kotlin DSL)
   - Delete any empty `build.gradle` (Groovy) files
   - Mixing Groovy and Kotlin DSL can cause issues

3. **Invalidate Caches**:
   - File > Invalidate Caches / Restart
   - Select "Invalidate and Restart"

4. **Re-import project**:
   - Close Android Studio
   - Delete `.idea/` folder and `.gradle/` folder
   - Open project again in Android Studio
   - Wait for Gradle sync to complete

5. **Verify Gradle and AGP compatibility**:
   - AGP 8.2.2 requires Gradle 8.2+
   - Current project uses AGP 8.2.2 with Gradle 8.5 ✓
   - Check `gradle/wrapper/gradle-wrapper.properties` has correct Gradle version

### Java Version Mismatch

**Symptoms**: "Unsupported class file major version", "Java version incompatibility"

**Solutions**:
1. **Check Java version** used by Android Studio:
   - File > Settings > Build, Execution, Deployment > Build Tools > Gradle
   - Gradle JDK should be Java 17 or compatible

2. **Verify build configuration**:
   - `app/build.gradle.kts` should have:
   ```kotlin
   compileOptions {
       sourceCompatibility = JavaVersion.VERSION_17
       targetCompatibility = JavaVersion.VERSION_17
   }
   kotlinOptions {
       jvmTarget = "17"
   }
   ```

3. **Update .idea configuration**:
   - `.idea/misc.xml` should reference JDK 17
   - `.idea/compiler.xml` should have bytecodeTargetLevel="17"

4. **Install correct JDK**:
   - Download JDK 17 from [Adoptium](https://adoptium.net/)
   - Configure in Android Studio settings

### AGP (Android Gradle Plugin) Version Issues

**Symptoms**: "AGP version incompatible", "Minimum supported Gradle version"

**Solutions**:
1. **Update AGP** in `build.gradle.kts`:
   ```kotlin
   plugins {
       id("com.android.application") version "8.2.2" apply false
       id("org.jetbrains.kotlin.android") version "1.9.22" apply false
   }
   ```

2. **Match Kotlin version** with AGP:
   - AGP 8.2.2 works with Kotlin 1.9.x
   - Update Kotlin plugin to 1.9.22 for best compatibility

3. **Clear Gradle cache**:
   ```bash
   ./gradlew clean
   rm -rf .gradle/
   ```


## Map Issues

### Map Shows Gray/Blank Screen

**Symptoms**: App loads but map area is gray or blank

**Solutions**:
1. **Check API Key**: 
   - Verify `MAPS_API_KEY` in `local.properties` is correct
   - Ensure no extra spaces or quotes around the key
   
2. **Enable Maps SDK**:
   - Go to [Google Cloud Console](https://console.cloud.google.com/)
   - Select your project
   - APIs & Services > Library
   - Search "Maps SDK for Android"
   - Click Enable

3. **Check API Key Restrictions**:
   - In Cloud Console, go to APIs & Services > Credentials
   - Click your API key
   - Under "Application restrictions", either:
     - Select "None" (for testing)
     - Or add your app's package name and SHA-1 certificate fingerprint

4. **Get SHA-1 Fingerprint**:
   ```bash
   # Debug certificate
   keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
   
   # Release certificate
   keytool -list -v -keystore /path/to/your-release-key.jks
   ```

5. **Check Logcat**:
   - Look for "Authorization failure" or "API key" errors
   - Filter by tag: "Google Maps Android API"

### Map Shows But No Markers

**Symptoms**: Map loads correctly but markers don't appear

**Solutions**:
1. Check if marker icons exist in `app/src/main/res/drawable/`
2. Verify icon names match code (e.g., `R.drawable.ic_police`)
3. Check Logcat for resource loading errors
4. Ensure markers are being added to the map
5. Try using default markers first to test

### Map Style Customization

**Note**: OpenStreetMap uses different tile sources instead of JSON styles like Google Maps.

**Options**:
1. Change tile source in `MainActivity.kt`:
   ```kotlin
   map.setTileSource(TileSourceFactory.MAPNIK)  // Default
   // Or try:
   // map.setTileSource(TileSourceFactory.WIKIMEDIA)
   // map.setTileSource(TileSourceFactory.OpenTopo)
   ```
2. For dark theme, consider third-party tile providers
3. Custom styling requires a tile server setup (advanced)
   ```

## Location Issues

### "Location permission denied"

**Symptoms**: App can't access device location

**Solutions**:
1. In app: Settings > Apps > WatchDogs 2 Map > Permissions > Location > Allow
2. Check `AndroidManifest.xml` has:
   ```xml
   <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
   <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
   ```
3. Runtime permission request should show automatically on first launch
4. On Android 11+, may need background location permission separately

### My Location Not Showing

**Symptoms**: Permission granted but blue dot doesn't appear

**Solutions**:
1. Enable location services on device:
   - Settings > Location > On
2. Check device has GPS/network location available
3. Try outdoors if using real device (GPS signal)
4. On emulator: Use "Set Location" in Extended Controls (three dots)
5. Check Logcat for `SecurityException` errors

### Geocoder Returns No Results

**Symptoms**: Search doesn't find any locations

**Solutions**:
1. Check internet connection (Geocoder requires network)
2. Try more specific searches: "San Francisco City Hall" vs just "Hall"
3. Check Logcat for `IOException` or geocoder errors
4. Verify device language/region settings
5. Try different search terms

## Spotify Issues

### "Connect to Spotify" Does Nothing

**Symptoms**: Button click has no effect

**Solutions**:
1. Check if `CLIENT_ID` is set correctly in `MainActivity.kt`
2. Change from `"YOUR_SPOTIFY_CLIENT_ID"` to actual Client ID
3. Verify format (no extra quotes, spaces, or line breaks)
4. Check Logcat for authentication errors

### "Failed to connect to Spotify"

**Symptoms**: Error message after attempting to connect

**Solutions**:
1. **Install Spotify App**:
   - App requires Spotify to be installed
   - Download from Google Play Store
   
2. **Verify Spotify Developer Settings**:
   - Go to [Spotify Dashboard](https://developer.spotify.com/dashboard)
   - Open your app
   - Under "Edit Settings", add redirect URI: `spotify-sdk://auth`
   - Save changes
   
3. **Check Scopes**:
   - App requests: `app-remote-control` and `streaming`
   - These should be sufficient for playback control
   
4. **Spotify Authentication Issues**:
   - Log out of Spotify app and log back in
   - Clear Spotify app data: Settings > Apps > Spotify > Clear Data
   - Try on different network (not corporate/school network)

### Music Controls Don't Work

**Symptoms**: Connected but play/pause/skip buttons have no effect

**Solutions**:
1. Verify something is playing in Spotify app first
2. Check Spotify app isn't paused or stopped
3. Try playing music manually in Spotify, then use app controls
4. Check Logcat for `PlayerAPI` errors
5. Disconnect and reconnect

## UI/Display Issues

### Text Too Small/Big

**Symptoms**: UI elements are wrong size

**Solutions**:
1. Check device display settings
2. Update dimensions in XML layouts if needed
3. Test on different screen sizes using emulators

### Neon Colors Not Showing

**Symptoms**: UI looks like default Android theme

**Solutions**:
1. Verify `themes.xml` has Watch Dogs 2 theme applied
2. Check `colors.xml` has neon cyan color defined:
   ```xml
   <color name="neon_cyan">#00FFF7</color>
   ```
3. Rebuild project: Build > Rebuild Project
4. Check if running in dark mode (app designed for dark theme)

### Search View Text Invisible

**Symptoms**: Can't see what you're typing in search

**Solutions**:
1. Check `activity_main.xml` has:
   ```xml
   app:queryTextColor="#00FFF7"
   ```
2. Verify search view background has proper contrast
3. Check theme colors aren't conflicting

## Performance Issues

### App Crashes on Launch

**Symptoms**: App closes immediately after opening

**Solutions**:
1. **Check Logcat** for crash stack trace
2. Common causes:
   - Missing Google Play Services
   - Invalid API key
   - Missing permissions in manifest
   - Null pointer exceptions
3. Look for specific error messages and line numbers

### App Runs Slow

**Symptoms**: Lag when panning map or clicking buttons

**Solutions**:
1. Reduce marker count if too many
2. Check device has sufficient RAM (recommend 2GB+)
3. Close other apps running in background
4. Test on newer device/emulator
5. Check for memory leaks in Logcat

### Map Lags When Zooming

**Symptoms**: Choppy animation when zooming map

**Solutions**:
1. Normal on older devices
2. Reduce map complexity (fewer markers)
3. Disable animations: Settings > Developer Options > Animator Duration Scale > Off
4. Test on device with better GPU

## Emulator-Specific Issues

### Map Not Loading on Emulator

**Solutions**:
1. Use emulator with Google Play Services (not Google APIs)
2. Ensure emulator has internet access
3. Try "Cold Boot" instead of snapshot
4. Update emulator system image

### Spotify Won't Work on Emulator

**Solutions**:
1. Spotify SDK may not work on all emulators
2. Test on physical device for Spotify features
3. Some emulators don't support Spotify app

## Still Having Issues?

If you've tried everything above:

1. **Check Logcat** thoroughly - most issues show error messages
2. **Search GitHub Issues** - someone may have had same problem
3. **Create New Issue** with:
   - Clear description of problem
   - Steps to reproduce
   - Logcat output
   - Device/emulator info
   - Screenshots if applicable

## Useful Commands

```bash
# View connected devices
adb devices

# View Logcat
adb logcat

# Filter Logcat for app
adb logcat | grep "WatchDogs"

# Clear app data
adb shell pm clear com.jeremylakeyjr.watchdogsmap

# Install APK
adb install -r app-debug.apk

# Check SHA-1 (debug)
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android
```

## Emergency Reset

If nothing works, try clean slate:

1. Close Android Studio
2. Delete these folders:
   - `.gradle/`
   - `.idea/`
   - `app/build/`
   - `build/`
3. Delete `local.properties`
4. Open Android Studio
5. File > Sync Project with Gradle Files
6. Rebuild project

---

Need more help? Check other docs:
- [SETUP.md](SETUP.md) - Setup instructions
- [QUICK_REFERENCE.md](QUICK_REFERENCE.md) - Common tasks
- [README.md](README.md) - General information
