# Migration Guide: Google Maps → OpenStreetMap

This guide helps you understand the changes made in migrating from Google Maps API to OpenStreetMap (osmdroid).

## 🎯 What Changed?

### Before (Google Maps)
- Required Google Maps API Key
- Paid service with usage limits
- Required Google Cloud Console setup
- Billing setup needed for production

### After (OpenStreetMap)
- ✅ **No API key required**
- ✅ **Completely free**
- ✅ **No usage limits**
- ✅ **No billing setup**
- ✅ **Open source**

## 📦 Dependency Changes

### Removed
```kotlin
// Google Maps dependencies (REMOVED)
implementation("com.google.android.gms:play-services-maps:18.2.0")
implementation("com.google.maps.android:maps-ktx:3.4.0")
implementation("com.google.maps.android:maps-utils-ktx:3.4.0")
```

### Added
```kotlin
// OpenStreetMap dependencies (FREE)
implementation("org.osmdroid:osmdroid-android:6.1.18")
```

**Note:** We still use Google Play Services Location (free) for device location features.

## 🔧 Configuration Changes

### Before: Google Maps API Key
```properties
# local.properties (NO LONGER NEEDED)
MAPS_API_KEY=AIzaSyXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX
```

### After: No Configuration Needed!
```kotlin
// Just initialize osmdroid
Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this))
Configuration.getInstance().userAgentValue = packageName
```

## 🗺️ Code Changes

### Map Initialization

**Before (Google Maps):**
```kotlin
private lateinit var mMap: GoogleMap

// In onCreate
val mapFragment = supportFragmentManager
    .findFragmentById(R.id.map) as? SupportMapFragment
mapFragment?.getMapAsync(this)

// In onMapReady
override fun onMapReady(googleMap: GoogleMap) {
    mMap = googleMap
    // Setup map...
}
```

**After (OpenStreetMap):**
```kotlin
private lateinit var map: MapView

// In onCreate
map = findViewById(R.id.map)
map.setTileSource(TileSourceFactory.MAPNIK)
map.setMultiTouchControls(true)
map.controller.setZoom(12.0)
```

### Markers

**Before (Google Maps):**
```kotlin
val marker = MarkerOptions()
    .position(LatLng(37.7749, -122.4194))
    .title("DedSec HQ")
    .snippet("Main Operations Center")
    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_home))
mMap.addMarker(marker)
```

**After (OpenStreetMap):**
```kotlin
val marker = Marker(map)
marker.position = GeoPoint(37.7749, -122.4194)
marker.title = "DedSec HQ"
marker.snippet = "Main Operations Center"
marker.icon = ContextCompat.getDrawable(this, R.drawable.ic_home)
marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
map.overlays.add(marker)
map.invalidate()
```

### Camera/View Control

**Before (Google Maps):**
```kotlin
val location = LatLng(37.7749, -122.4194)
mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 12f))
```

**After (OpenStreetMap):**
```kotlin
val location = GeoPoint(37.7749, -122.4194)
map.controller.setCenter(location)
map.controller.setZoom(12.0)
// Or animate
map.controller.animateTo(location)
```

### User Location

**Before (Google Maps):**
```kotlin
mMap.isMyLocationEnabled = true
mMap.uiSettings.isMyLocationButtonEnabled = true
```

**After (OpenStreetMap):**
```kotlin
myLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(this), map)
myLocationOverlay?.enableMyLocation()
myLocationOverlay?.enableFollowLocation()
map.overlays.add(myLocationOverlay)
```

## 🎨 Map Styling

### Before (Google Maps)
Used JSON style files:
```kotlin
val style = MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style_watchdogs2)
mMap.setMapStyle(style)
```

### After (OpenStreetMap)
Use different tile sources:
```kotlin
// Default (Mapnik)
map.setTileSource(TileSourceFactory.MAPNIK)

// Other options:
map.setTileSource(TileSourceFactory.WIKIMEDIA)
map.setTileSource(TileSourceFactory.OpenTopo)
map.setTileSource(TileSourceFactory.USGS_TOPO)
```

**Note:** Custom dark themes require external tile providers or custom tile servers.

## 📱 Layout Changes

### Before (Google Maps)
```xml
<fragment
    android:id="@+id/map"
    android:name="com.google.android.gms.maps.SupportMapFragment"
    android:layout_width="match_parent"
    android:layout_height="match_parent" />
```

### After (OpenStreetMap)
```xml
<org.osmdroid.views.MapView
    android:id="@+id/map"
    android:layout_width="match_parent"
    android:layout_height="match_parent" />
```

## 🔒 Permission Changes

### Added Permissions
```xml
<!-- Required for osmdroid to cache map tiles -->
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"
    android:maxSdkVersion="32" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

## 🔄 Lifecycle Methods

### Added for osmdroid
```kotlin
override fun onResume() {
    super.onResume()
    map.onResume()  // Resume tile loading
}

override fun onPause() {
    super.onPause()
    map.onPause()  // Pause tile loading
}
```

## 📊 Feature Comparison

| Feature | Google Maps | OpenStreetMap (osmdroid) |
|---------|-------------|--------------------------|
| Cost | Paid (after free tier) | Free |
| API Key | Required | Not required |
| Usage Limits | Yes (100k/day free) | No limits |
| Setup Complexity | High | Low |
| Offline Mode | Limited | Full support |
| Custom Tiles | Difficult | Easy |
| Community | Large | Very large |
| Open Source | No | Yes |

## ✅ What Still Works

All core functionality remains the same:
- ✅ Map display and navigation
- ✅ Custom markers with icons
- ✅ User location tracking
- ✅ Location search (Geocoder)
- ✅ Zoom and pan controls
- ✅ Info windows (click markers)
- ✅ Multi-touch gestures

## 🎓 Learning Resources

- **osmdroid Wiki**: https://github.com/osmdroid/osmdroid/wiki
- **OpenStreetMap**: https://www.openstreetmap.org/
- **Tile Sources**: https://wiki.openstreetmap.org/wiki/Tile_servers
- **API Documentation**: https://osmdroid.github.io/osmdroid/

## ❓ FAQ

### Q: Will my existing marker positions work?
**A:** Yes! Coordinates (latitude/longitude) are the same. Just use `GeoPoint` instead of `LatLng`.

### Q: Can I still use custom map styles?
**A:** Not JSON styles. Use different tile sources or set up a custom tile server for advanced styling.

### Q: Is OpenStreetMap reliable for production?
**A:** Yes! It's used by major companies like Facebook, Apple (iPhoto), Foursquare, and Craigslist.

### Q: What about offline maps?
**A:** osmdroid has better offline support than Google Maps! You can pre-download tiles.

### Q: Can I go back to Google Maps?
**A:** Yes, but you'll need to restore the old dependencies and code. Keep this PR for reference.

## 🚀 Next Steps

1. ✅ Pull the latest changes
2. ✅ Remove `local.properties` API key (no longer needed)
3. ✅ Run the app - it works without setup!
4. 🎉 Enjoy free, unlimited maps!

## 💬 Support

If you encounter issues:
- Check [TROUBLESHOOTING.md](TROUBLESHOOTING.md)
- Review [osmdroid issues](https://github.com/osmdroid/osmdroid/issues)
- Open an issue on this repository

---

**Made with ❤️ for the open-source community**
