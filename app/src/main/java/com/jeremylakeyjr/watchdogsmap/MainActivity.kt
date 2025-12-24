package com.jeremylakeyjr.watchdogsmap

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.io.IOException

/**
 * Data class representing a marker on the map
 */
data class MarkerData(
    val lat: Double,
    val lon: Double,
    val title: String,
    val snippet: String,
    val iconRes: Int
)

class MainActivity : AppCompatActivity() {

    private lateinit var map: MapView
    private lateinit var searchView: SearchView
    private lateinit var connectButton: Button
    private lateinit var musicControls: LinearLayout
    private lateinit var playPauseButton: Button
    private lateinit var skipButton: Button
    private var myLocationOverlay: MyLocationNewOverlay? = null

    // IMPORTANT: Replace this with your own Spotify Client ID
    // Get one from https://developer.spotify.com/dashboard
    private val CLIENT_ID = "YOUR_SPOTIFY_CLIENT_ID"
    private val REDIRECT_URI = "spotify-sdk://auth"
    private var spotifyAppRemote: SpotifyAppRemote? = null
    private var isPlaying = false

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private const val SPOTIFY_REQUEST_CODE = 1337
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize osmdroid configuration
        initializeOsmConfig()
        
        setContentView(R.layout.activity_main)

        initializeViews()
        setupMap()
        setupSearchView()
        setupMusicControls()
        checkLocationPermission()
    }

    private fun initializeOsmConfig() {
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this))
        Configuration.getInstance().userAgentValue = packageName
    }

    private fun initializeViews() {
        searchView = findViewById(R.id.searchView)
        connectButton = findViewById(R.id.connect_button)
        musicControls = findViewById(R.id.music_controls)
        playPauseButton = findViewById(R.id.play_pause_button)
        skipButton = findViewById(R.id.skip_button)
        map = findViewById(R.id.map)
    }

    private fun setupMap() {
        // Configure map
        map.setTileSource(TileSourceFactory.MAPNIK)
        map.setMultiTouchControls(true)
        map.controller.setZoom(12.0)
        
        // Set default location to San Francisco (Watch Dogs 2 setting)
        val sanFrancisco = GeoPoint(37.7749, -122.4194)
        map.controller.setCenter(sanFrancisco)
        
        // Add custom markers with Watch Dogs 2 theme
        addCustomMarkers()
        
        // Enable location overlay if permission granted
        enableMyLocation()
    }

    private fun setupSearchView() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    if (it.isNotBlank()) {
                        searchLocation(it)
                        searchView.clearFocus()
                    }
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })
    }

    private fun setupMusicControls() {
        connectButton.setOnClickListener {
            if (CLIENT_ID == "YOUR_SPOTIFY_CLIENT_ID") {
                Toast.makeText(
                    this,
                    "Please set your Spotify Client ID in MainActivity.kt",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                connectToSpotify()
            }
        }

        playPauseButton.setOnClickListener {
            spotifyAppRemote?.playerApi?.let { playerApi ->
                if (isPlaying) {
                    playerApi.pause()
                    playPauseButton.text = "▶ Play"
                } else {
                    playerApi.resume()
                    playPauseButton.text = "⏸ Pause"
                }
                isPlaying = !isPlaying
            }
        }

        skipButton.setOnClickListener {
            spotifyAppRemote?.playerApi?.skipNext()
        }
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation()
            }
        }
    }

    private fun enableMyLocation() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            try {
                myLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(this), map)
                myLocationOverlay?.let { overlay ->
                    overlay.enableMyLocation()
                    overlay.enableFollowLocation()
                    map.overlays.add(overlay)
                }
            } catch (e: SecurityException) {
                Log.e("MainActivity", "Error enabling my location", e)
            } catch (e: Exception) {
                Log.e("MainActivity", "Error initializing location overlay", e)
            }
        }
    }

    private fun searchLocation(location: String) {
        val geocoder = Geocoder(this)

        // Use modern Geocoder API for Android 33+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            geocoder.getFromLocationName(location, 1) { addresses ->
                runOnUiThread {
                    handleSearchResults(addresses, location)
                }
            }
        } else {
            // Fallback for older versions
            try {
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocationName(location, 1)
                handleSearchResults(addresses, location)
            } catch (e: IOException) {
                Log.e("MainActivity", "Geocoder error", e)
                Toast.makeText(this, "Error searching location: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun handleSearchResults(addresses: List<Address>?, location: String) {
        if (addresses != null && addresses.isNotEmpty()) {
            val address = addresses[0]
            val geoPoint = GeoPoint(address.latitude, address.longitude)
            
            // Add marker at search result
            val marker = Marker(map)
            marker.position = geoPoint
            marker.title = location
            marker.snippet = "Search Result"
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            
            map.overlays.add(marker)
            map.controller.animateTo(geoPoint)
            map.controller.setZoom(15.0)
            map.invalidate()
        } else {
            Toast.makeText(this, "Location not found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addCustomMarkers() {
        val markers = listOf(
            MarkerData(37.7749, -122.4194, "DedSec HQ", "Main Operations Center", R.drawable.ic_home),
            MarkerData(37.7849, -122.4094, "Police Station", "SFPD - High Security", R.drawable.ic_police),
            MarkerData(37.7649, -122.4294, "Blume Corporation", "ctOS Control Center", R.drawable.ic_bank),
            MarkerData(37.7899, -122.4344, "Hacker Space", "Underground Meeting Point", R.drawable.ic_cafe),
            MarkerData(37.7549, -122.4144, "Safe House", "Hideout Location", R.drawable.ic_hospital),
            MarkerData(37.7799, -122.4244, "Data Center", "Server Farm", R.drawable.ic_school),
            MarkerData(37.7699, -122.4394, "Training Ground", "Skills Development", R.drawable.ic_gym),
            MarkerData(37.7949, -122.4144, "Supply Point", "Equipment & Resources", R.drawable.ic_restaurant),
            MarkerData(37.7599, -122.4244, "Charging Station", "Drone & RC Charging", R.drawable.ic_fuel),
            MarkerData(37.7849, -122.4444, "Black Market", "Illegal Goods", R.drawable.ic_pharmacy),
            MarkerData(37.7649, -122.4144, "Fight Club", "Underground Arena", R.drawable.ic_fighting_gym)
        )

        markers.forEach { markerData ->
            val marker = Marker(map)
            marker.position = GeoPoint(markerData.lat, markerData.lon)
            marker.title = markerData.title
            marker.snippet = markerData.snippet
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            
            // Set custom icon
            try {
                marker.icon = ContextCompat.getDrawable(this, markerData.iconRes)
            } catch (e: Exception) {
                Log.e("MainActivity", "Error setting marker icon for ${markerData.title}", e)
            }
            
            map.overlays.add(marker)
        }
        
        map.invalidate()
    }

    private fun connectToSpotify() {
        val request = AuthorizationRequest.Builder(
            CLIENT_ID,
            AuthorizationResponse.Type.TOKEN,
            REDIRECT_URI
        )
            .setScopes(arrayOf("app-remote-control", "streaming"))
            .build()

        AuthorizationClient.openLoginActivity(this, SPOTIFY_REQUEST_CODE, request)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SPOTIFY_REQUEST_CODE) {
            val response = AuthorizationClient.getResponse(resultCode, data)
            when (response.type) {
                AuthorizationResponse.Type.TOKEN -> {
                    val connectionParams = ConnectionParams.Builder(CLIENT_ID)
                        .setRedirectUri(REDIRECT_URI)
                        .showAuthView(true)
                        .build()

                    SpotifyAppRemote.connect(
                        this,
                        connectionParams,
                        object : Connector.ConnectionListener {
                            override fun onConnected(appRemote: SpotifyAppRemote) {
                                spotifyAppRemote = appRemote
                                Log.d("MainActivity", "Connected to Spotify!")
                                runOnUiThread {
                                    connectButton.visibility = View.GONE
                                    musicControls.visibility = View.VISIBLE
                                    Toast.makeText(
                                        this@MainActivity,
                                        "🎵 Connected to Spotify!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }

                            override fun onFailure(throwable: Throwable) {
                                Log.e("MainActivity", "Spotify connection failed", throwable)
                                runOnUiThread {
                                    Toast.makeText(
                                        this@MainActivity,
                                        "Failed to connect to Spotify: ${throwable.message}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        })
                }

                AuthorizationResponse.Type.ERROR -> {
                    Log.e("MainActivity", "Auth error: ${response.error}")
                    Toast.makeText(this, "Authentication error: ${response.error}", Toast.LENGTH_LONG)
                        .show()
                }

                else -> {
                    Log.d("MainActivity", "Auth result: ${response.type}")
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        map.onResume()
    }

    override fun onPause() {
        super.onPause()
        map.onPause()
    }

    override fun onStop() {
        super.onStop()
        spotifyAppRemote?.let {
            SpotifyAppRemote.disconnect(it)
        }
    }
}