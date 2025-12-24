# Watch Dogs 2 Style Map App 🎮🗺️

A cyberpunk-themed Android map application inspired by Watch Dogs 2, featuring a dark neon aesthetic, custom markers, and Spotify integration for the ultimate hacker experience.

## ✨ Features

- **🎨 Custom Dark Map Style**: Neon-accented map inspired by Watch Dogs 2's UI
- **📍 Custom Markers**: Watch Dogs 2 themed location markers (DedSec HQ, Police Stations, etc.)
- **💾 Interactive Info Windows**: Neon-styled info windows with custom layouts
- **🎵 Spotify Integration**: Control your music while exploring the map
- **🔍 Location Search**: Find any location with the integrated search bar
- **📱 Modern UI**: Sleek, cyberpunk-inspired interface with neon cyan accents
- **🗺️ San Francisco Setting**: Default location set to San Francisco (Watch Dogs 2's city)

## 🚀 Quick Start

### Prerequisites

- Android Studio (latest version recommended)
- Android SDK 24 or higher
- Google Maps API Key
- Spotify Developer Account (for music features)

### Installation Steps

1. **Clone the repository**
   ```bash
   git clone https://github.com/JeremyLakeyJr/watchdogs-map.git
   cd watchdogs-map
   ```

2. **Set up Google Maps API Key**
   - Get an API key from [Google Cloud Console](https://console.cloud.google.com/)
   - Create a `local.properties` file in the root directory
   - Add your API key:
     ```properties
     MAPS_API_KEY=YOUR_GOOGLE_MAPS_API_KEY_HERE
     ```

3. **Set up Spotify (Optional)**
   - Create an app at [Spotify Developer Dashboard](https://developer.spotify.com/dashboard)
   - Note your Client ID
   - Add redirect URI: `spotify-sdk://auth`
   - Open `MainActivity.kt` and replace:
     ```kotlin
     private val CLIENT_ID = "YOUR_SPOTIFY_CLIENT_ID"
     ```

4. **Build and Run**
   - Open project in Android Studio
   - Sync Gradle files
   - Run on emulator or physical device

## 🎮 How to Use

### Map Features
- **Explore Locations**: Pan and zoom the map to discover Watch Dogs 2 themed locations
- **Search**: Use the search bar at the bottom to find specific locations
- **View Details**: Tap markers to see location information in neon-styled info windows
- **My Location**: Grant location permission to see your current position

### Music Controls
1. Tap "🎵 Connect to Spotify"
2. Log in to your Spotify account
3. Use Play/Pause and Skip buttons to control playback
4. Music plays in the background while you explore

## 🎨 Theme & Aesthetics

The app features Watch Dogs 2's signature cyberpunk aesthetic:
- **Neon Cyan (#00FFF7)**: Primary accent color
- **Dark Backgrounds**: True black and dark gray surfaces
- **Glowing Effects**: Text shadows for neon glow effect
- **Custom Map Style**: Dark themed map with cyan highlights

## 📱 Screenshots

*(Screenshots will be added here once the app is built)*

## 🛠️ Technical Details

### Built With
- **Kotlin**: Modern Android development
- **Google Maps SDK**: Map rendering and location services
- **Spotify SDK**: Music integration
- **Material Design**: UI components
- **Android Jetpack**: Modern Android libraries

### Architecture
- Single Activity with Fragment-based map
- Custom info window adapter
- Permission handling for location services
- Modern Geocoder API (Android 13+ compatible)

### Key Components
- `MainActivity.kt`: Main activity with map and music controls
- `CustomInfoWindowAdapter.kt`: Custom marker info windows
- `map_style_watchdogs2.json`: Dark neon map styling
- Custom drawables: Neon-themed UI backgrounds

## 🔒 Permissions Required

- `ACCESS_FINE_LOCATION`: Show user location on map
- `ACCESS_COARSE_LOCATION`: Approximate location
- `INTERNET`: Map tiles and Spotify connection

## 🐛 Known Issues

- Spotify integration requires Spotify app installed on device
- Some emulators may not support location services

## 📝 TODO

- [ ] Add more Watch Dogs 2 themed locations
- [ ] Implement mission markers
- [ ] Add augmented reality features
- [ ] Include sound effects for interactions
- [ ] Add night mode toggle
- [ ] Implement offline map caching

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## 📄 License

MIT License - see [LICENSE](LICENSE) file for details

## 👏 Credits

- Inspired by Ubisoft's Watch Dogs 2
- Map icons from various open-source projects
- Color scheme based on Watch Dogs 2 UI design

## 📞 Support

For issues or questions, please open an issue on GitHub or contact the maintainer.

---

**Note**: This is a fan project and is not affiliated with or endorsed by Ubisoft.

Made with ❤️ by Jeremy Lakey Jr.