# Setup Guide for Watch Dogs 2 Map App

This guide will help you set up and run the Watch Dogs 2 Map App on your local machine.

## Step 1: Install Android Studio

1. Download Android Studio from [https://developer.android.com/studio](https://developer.android.com/studio)
2. Install Android Studio following the setup wizard
3. Install Android SDK Platform 34 (or the latest version)
4. Install Android SDK Build-Tools
5. Set up an Android emulator or connect a physical device

## Step 2: Get Google Maps API Key

1. Go to [Google Cloud Console](https://console.cloud.google.com/)
2. Create a new project or select an existing one
3. Enable "Maps SDK for Android" API
4. Go to "Credentials" and create an API key
5. Restrict the key to Android apps (optional but recommended)
6. Note your API key

## Step 3: Configure API Key

Create a file named `local.properties` in the root directory of the project (same level as `build.gradle.kts`):

```properties
# Google Maps API Key
MAPS_API_KEY=AIzaSyXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX

# Optional: Release signing configuration
# keystore.file=path/to/your/keystore.jks
# keystore.password=your_keystore_password
# key.alias=your_key_alias
# key.password=your_key_password
```

**Important**: Never commit `local.properties` to version control! It's already in `.gitignore`.

## Step 4: Set Up Spotify Integration (Optional)

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

## Step 5: Build the Project

1. Open Android Studio
2. Click "File" > "Open" and select the project folder
3. Wait for Gradle sync to complete
4. If prompted, accept any SDK installations
5. Click "Build" > "Make Project" or press Ctrl+F9 (Cmd+F9 on Mac)

## Step 6: Run the App

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

## Step 7: Grant Permissions

When the app first runs:
1. Grant location permission when prompted
2. Search for locations using the search bar
3. (Optional) Tap "Connect to Spotify" to link your Spotify account

## Troubleshooting

### "Map not loading" or "Authorization failure"
- Check that your API key is correct in `local.properties`
- Verify the Maps SDK for Android is enabled in Google Cloud Console
- Make sure your API key has the correct restrictions

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
│   │       │   ├── MainActivity.kt
│   │       │   └── CustomInfoWindowAdapter.kt
│   │       ├── res/
│   │       │   ├── layout/
│   │       │   │   ├── activity_main.xml
│   │       │   │   └── custom_info_window.xml
│   │       │   ├── drawable/
│   │       │   │   └── (custom icons)
│   │       │   ├── raw/
│   │       │   │   └── map_style_watchdogs2.json
│   │       │   └── values/
│   │       │       ├── colors.xml
│   │       │       ├── themes.xml
│   │       │       └── strings.xml
│   │       └── AndroidManifest.xml
│   └── build.gradle.kts
├── build.gradle.kts
├── settings.gradle.kts
└── local.properties (you create this)
```

## Next Steps

- Customize marker locations in `MainActivity.kt`
- Modify colors in `res/values/colors.xml`
- Update map style in `res/raw/map_style_watchdogs2.json`
- Add your own custom icons to `res/drawable/`

## Need Help?

- Check the [README.md](README.md) for more information
- Open an issue on GitHub
- Review Android Studio's build logs for specific errors

Enjoy your Watch Dogs 2 themed map experience! 🎮🗺️
