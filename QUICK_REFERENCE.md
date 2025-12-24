# Quick Reference Guide

## Common Tasks

### Changing Map Style Colors

Edit `app/src/main/res/raw/map_style_watchdogs2.json`:

```json
{
  "featureType": "all",
  "elementType": "labels.text.fill",
  "stylers": [
    { "color": "#00fff7" }  // Neon cyan - change this!
  ]
}
```

### Adding New Markers

In `MainActivity.kt`, add to the `addCustomMarkers()` function:

```kotlin
MarkerOptions()
    .position(LatLng(37.7xxx, -122.4xxx))
    .title("Your Location")
    .snippet("Description")
    .icon(BitmapDescriptorFactory.fromResource(R.drawable.your_icon))
```

### Changing Theme Colors

Edit `app/src/main/res/values/colors.xml`:

```xml
<color name="neon_cyan">#00FFF7</color>  <!-- Main accent color -->
<color name="dark_background">#0A0A0A</color>  <!-- Background -->
```

### Customizing UI Text

Edit `app/src/main/res/values/strings.xml`:

```xml
<string name="app_name">Your App Name</string>
```

### Adding Custom Icons

1. Place PNG files in `app/src/main/res/drawable/`
2. Name them like: `ic_your_icon.png`
3. Reference in code: `R.drawable.ic_your_icon`

### Changing Default Location

In `MainActivity.kt`, modify `onMapReady()`:

```kotlin
val yourLocation = LatLng(latitude, longitude)
mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(yourLocation, 12f))
```

## File Locations

| What | Where |
|------|-------|
| Main Logic | `app/src/main/java/.../MainActivity.kt` |
| UI Layout | `app/src/main/res/layout/activity_main.xml` |
| Colors | `app/src/main/res/values/colors.xml` |
| Strings | `app/src/main/res/values/strings.xml` |
| Theme | `app/src/main/res/values/themes.xml` |
| Icons | `app/src/main/res/drawable/` |
| Map Style | `app/src/main/res/raw/map_style_watchdogs2.json` |

## Useful Android Studio Shortcuts

| Action | Windows/Linux | Mac |
|--------|---------------|-----|
| Build Project | Ctrl+F9 | Cmd+F9 |
| Run App | Shift+F10 | Ctrl+R |
| Format Code | Ctrl+Alt+L | Cmd+Option+L |
| Find in Files | Ctrl+Shift+F | Cmd+Shift+F |
| Go to Class | Ctrl+N | Cmd+O |
| Show Logcat | Alt+6 | Cmd+6 |

## Debugging Tips

### Map Not Showing
1. Check API key in `local.properties`
2. Verify internet connection
3. Check Logcat for errors (filter by "MainActivity")

### Location Not Working
1. Grant location permission
2. Enable location on device
3. Check Logcat for permission errors

### Spotify Not Connecting
1. Install Spotify app
2. Verify Client ID in MainActivity.kt
3. Check redirect URI: `spotify-sdk://auth`
4. Review Logcat for connection errors

### Build Errors
1. Clean project: Build > Clean Project
2. Rebuild: Build > Rebuild Project
3. Invalidate Caches: File > Invalidate Caches / Restart
4. Sync Gradle: File > Sync Project with Gradle Files

## Color Scheme Reference

```
Neon Cyan:       #00FFF7  (Primary accent)
Neon Cyan Dark:  #00B8B2  (Primary variant)
Neon Pink:       #FF006E  (Secondary accent)
Dark Background: #0A0A0A  (Main background)
Dark Surface:    #1A1A1A  (Card/button background)
Dark Elevated:   #2A2A2A  (Elevated elements)
```

## Map Zoom Levels

| Level | View |
|-------|------|
| 1-5 | World/Continent |
| 6-10 | City |
| 11-14 | Streets |
| 15-18 | Buildings |
| 19+ | Very close |

Default in app: **12** (good city overview)

## Marker Icon Sizes

Recommended: **48x48dp** for standard markers

Current markers are already optimized for display.

## Version Information

- Minimum SDK: 24 (Android 7.0)
- Target SDK: 34 (Android 14)
- Kotlin: 1.9.0
- Gradle: 8.5
- Android Gradle Plugin: 8.1.1

## Need More Help?

- See [SETUP.md](SETUP.md) for detailed setup
- See [CONTRIBUTING.md](CONTRIBUTING.md) for development guidelines
- Check [README.md](README.md) for features overview
