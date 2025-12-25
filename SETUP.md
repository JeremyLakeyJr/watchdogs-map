# Setup Guide for Watch Dogs 2 Map App

This guide will help you set up and run the Watch Dogs 2 Map App on your local machine.

## Prerequisites

- **Android Studio**: Hedgehog (2023.1.1) or newer recommended
- **JDK**: Java 17 (included with Android Studio)
- **Android SDK**: API Level 34 (Android 14)
- **Gradle**: 8.5 (included via wrapper)
- **Minimum Android Version**: API 24 (Android 7.0)

## Step 1: Install Android Studio

1. Download Android Studio from [https://developer.android.com/studio](https://developer.android.com/studio)
2. Install Android Studio following the setup wizard
3. Install Android SDK Platform 34 (or the latest version)
4. Install Android SDK Build-Tools
5. Set up an Android emulator or connect a physical device

## Step 2: Set Up Spotify Integration (Optional)

If you want to use the Spotify music controls:

1. Go to [Spotify Developer Dashboard](https://developer.spotify.com/dashboard)
2. Log in with your Spotify account
3. Click "Create App"
4. Fill in:
   - App Name: "Watch Dogs 2 Map" (or your choice)
   - App Description: Your description
   - Redirect URI: `spotify-sdk://auth`
5. Save and note your **Client ID**
6. Open `app/src/main/java/com/jeremylakeyjr/watchdogsmap/MainActivity.kt`
7. Replace the CLIENT_ID:
   ```kotlin
   private val CLIENT_ID = "your_actual_client_id_here"
   ```

**Note**: The app now uses OpenStreetMap which is completely free and requires no API keys!

## Step 3: Build the Project

1. Open Android Studio
2. Click "File" > "Open" and select the project folder
3. Wait for Gradle sync to complete (this may take a few minutes on first run)
   - Android Studio will automatically download Gradle 8.5 and dependencies
   - The app module should be recognized automatically
   - If you see "app" module not recognized, see [TROUBLESHOOTING.md](TROUBLESHOOTING.md)
4. If prompted, accept any SDK installations
5. Verify the project structure shows the "app" module in the Project panel
6. Click "Build" > "Make Project" or press Ctrl+F9 (Cmd+F9 on Mac)

**Note**: This project uses:
- AGP (Android Gradle Plugin) 8.2.2
- Gradle 8.5 via wrapper
- Kotlin 1.9.22
- Java 17 for compilation
- All modules are properly configured in `settings.gradle.kts`

## Step 4: Run the App

### On Emulator:
1. Click "Tools" > "AVD Manager"
2. Create a new virtual device (recommended: Pixel 5 with Android 11+)
3. Click the green "Run" button or press Shift+F10
4. Select your emulator

### On Physical Device:
1. Enable Developer Options on your Android device:
   - Go to Settings > About Phone
   - Tap "Build Number" 7 times
2. Enable USB Debugging in Developer Options
3. Connect device via USB
4. Click the green "Run" button
5. Select your device

## Step 5: Grant Permissions

When the app first runs:
1. Grant location permission when prompted
2. Search for locations using the search bar
3. (Optional) Tap "Connect to Spotify" to link your Spotify account

## Troubleshooting

### "Map not loading"
- Check your internet connection (required for downloading map tiles)
- Map tiles are downloaded from OpenStreetMap servers
- Try zooming in/out or panning the map to trigger tile loading

### "Spotify won't connect"
- Install the Spotify app on your device
- Verify your Client ID is correct
- Check that the redirect URI is exactly: `spotify-sdk://auth`

### Build Errors
- File > Invalidate Caches / Restart
- Delete `.gradle` folder and sync again
- Make sure you have Java 11 or newer installed

### Gradle Sync Failed
- Check your internet connection
- Try using a VPN if repositories are blocked
- Update Gradle to the latest version

## Project Structure

```
watchdogs-map/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/com/jeremylakeyjr/watchdogsmap/
│   │       │   └── MainActivity.kt
│   │       ├── res/
│   │       │   ├── layout/
│   │       │   │   ├── activity_main.xml
│   │       │   │   └── custom_info_window.xml
│   │       │   ├── drawable/
│   │       │   │   └── (custom icons)
│   │       │   └── values/
│   │       │       ├── colors.xml
│   │       │       ├── themes.xml
│   │       │       └── strings.xml
│   │       └── AndroidManifest.xml
│   └── build.gradle.kts
├── build.gradle.kts
├── settings.gradle.kts
└── local.properties (optional, only if using release signing)
```

## Next Steps

- Customize marker locations in `MainActivity.kt`
- Modify colors in `res/values/colors.xml`
- Add your own custom icons to `res/drawable/`
- Explore OpenStreetMap tile sources (satellite, terrain, etc.)

## Need Help?

- Check the [README.md](README.md) for more information
- Open an issue on GitHub
- Review Android Studio's build logs for specific errors

Enjoy your Watch Dogs 2 themed map experience! 🎮🗺️
