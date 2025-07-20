package com.example.gtamap

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.example.gtamap.network.OverpassApi
// import com.google.android.material.floatingactionbutton.FloatingActionButton // This import is no longer needed
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.location.LocationComponentActivationOptions
import org.maplibre.android.location.modes.CameraMode
import org.maplibre.android.location.modes.RenderMode
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.OnMapReadyCallback
import org.maplibre.android.maps.Style
import org.maplibre.android.plugins.annotation.SymbolManager
import org.maplibre.android.plugins.annotation.SymbolOptions
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

// Data class to hold POI information
data class Poi(val lat: Double, val lon: Double, val tags: Map<String, String>)

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mapView: MapView
    private lateinit var map: MapLibreMap
    private var symbolManager: SymbolManager? = null
    private val overpassApi = OverpassApi()

    // Handles the result of the location permission request.
    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false)) {
            // Permission granted. Enable the location layer.
            map.style?.let { enableLocationComponent(it) }
        } else {
            // Permission denied. Show a message to the user.
            Toast.makeText(this, "Location permission is required for GPS features.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // This line makes the app content draw behind the system bars (status and navigation).
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Correctly initialize MapLibre with just the context.
        MapLibre.getInstance(this)
        setContentView(R.layout.activity_main)

        mapView = findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
    }

    override fun onMapReady(maplibreMap: MapLibreMap) {
        this.map = maplibreMap

        // STEP 1: SET YOUR CUSTOM MAP STYLE
        val styleUrl = "https://api.maptiler.com/maps/019824ee-d438-75ae-bacf-ff51fee65a7d/style.json?key=${BuildConfig.MAP_API_KEY}"

        maplibreMap.setStyle(Style.Builder().fromUri(styleUrl)) { style ->
            // Map style is loaded. Now we can add components.
            symbolManager = SymbolManager(mapView, map, style)

            // Add a custom icon to the map style. This is needed for POIs.
            style.addImage("poi-icon-id", ContextCompat.getDrawable(this, R.drawable.ic_launcher_foreground)!!)

            setupUI(maplibreMap)
            checkLocationPermissionsAndEnableComponent(style)
            fetchAndDisplayPois()
        }
    }

    private fun setupUI(map: MapLibreMap) {
        // Setup Search Bar
        val searchView = findViewById<SearchView>(R.id.searchView)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrBlank()) {
                    searchForLocation(query, map)
                    searchView.clearFocus() // Hide keyboard
                }
                return true
            }
            override fun onQueryTextChange(newText: String?) = false
        })

        // THIS ENTIRE BLOCK HAS BEEN REMOVED TO FIX THE BUILD ERROR
        /*
        findViewById<FloatingActionButton>(R.id.fab_location).setOnClickListener {
            val locationComponent = map.locationComponent
            if (locationComponent.isLocationComponentActivated && locationComponent.lastKnownLocation != null) {
                map.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(locationComponent.lastKnownLocation!!),
                        15.0
                    )
                )
            } else {
                Toast.makeText(this, "Location not available yet.", Toast.LENGTH_SHORT).show()
            }
        }
        */
    }

    private fun searchForLocation(query: String, map: MapLibreMap) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val encodedQuery = URLEncoder.encode(query, "UTF-8")
                val url = URL("https://nominatim.openstreetmap.org/search?q=$encodedQuery&format=json&limit=1")
                val connection = url.openConnection() as HttpURLConnection
                val jsonResponse = connection.inputStream.bufferedReader().readText()
                val results = JSONArray(jsonResponse)

                if (results.length() > 0) {
                    val firstResult = results.getJSONObject(0)
                    val lat = firstResult.getDouble("lat")
                    val lon = firstResult.getDouble("lon")
                    withContext(Dispatchers.Main) {
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(lat, lon), 14.0))
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, "Location not found.", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("SearchError", "Failed to geocode location", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Search failed. Check connection.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun checkLocationPermissionsAndEnableComponent(style: Style) {
        if (hasLocationPermission()) {
            enableLocationComponent(style)
        } else {
            locationPermissionRequest.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
        }
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    private fun enableLocationComponent(loadedMapStyle: Style) {
        val locationComponent = map.locationComponent
        locationComponent.activateLocationComponent(
            LocationComponentActivationOptions.builder(this, loadedMapStyle).build()
        )
        locationComponent.isLocationComponentEnabled = true
        // Configure the location puck
        locationComponent.cameraMode = CameraMode.TRACKING_GPS
        locationComponent.renderMode = RenderMode.COMPASS // A good mode for the hacker aesthetic
    }

    private fun fetchAndDisplayPois() {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val bbox = map.projection.visibleRegion.latLngBounds
                val jsonResponse = overpassApi.fetchPois(bbox)
                val pois = parsePois(jsonResponse)
                withContext(Dispatchers.Main) {
                    addPoisToMap(pois)
                }
            } catch (e: Exception) {
                Log.e("OverpassError", "Failed to fetch or display POIs", e)
            }
        }
    }

    private fun parsePois(json: String): List<Poi> {
        val pois = mutableListOf<Poi>()
        val elements = JSONObject(json).getJSONArray("elements")
        for (i in 0 until elements.length()) {
            val element = elements.getJSONObject(i)
            if (element.getString("type") == "node") {
                val tags = mutableMapOf<String, String>()
                val tagsObject = element.optJSONObject("tags")
                tagsObject?.keys()?.forEach { key ->
                    tags[key] = tagsObject.getString(key)
                }
                pois.add(Poi(element.getDouble("lat"), element.getDouble("lon"), tags))
            }
        }
        return pois
    }

    private fun addPoisToMap(pois: List<Poi>) {
        symbolManager?.let { manager ->
            val symbolOptionsList = pois.map { poi ->
                SymbolOptions()
                    .withLatLng(LatLng(poi.lat, poi.lon))
                    .withIconImage("poi-icon-id")
                    .withIconSize(0.5f) // STEP 2: MAKE ICONS SMALLER (50% of original size)
                    .withTextField(poi.tags["name"] ?: "POI")
                    .withTextOffset(arrayOf(0f, 1.5f))
            }
            manager.create(symbolOptionsList)
        }
    }

    // Standard MapView lifecycle methods
    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }
}