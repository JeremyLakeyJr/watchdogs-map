package com.jeremylakeyjr.watchdogsmap

import android.Manifest
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
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse
import java.io.IOException

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var searchView: SearchView
    private lateinit var connectButton: Button
    private lateinit var musicControls: LinearLayout
    private lateinit var playPauseButton: Button
    private lateinit var skipButton: Button

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
        setContentView(R.layout.activity_main)

        initializeViews()
        setupMapFragment()
        setupSearchView()
        setupMusicControls()
        checkLocationPermission()
    }

    private fun initializeViews() {
        searchView = findViewById(R.id.searchView)
        connectButton = findViewById(R.id.connect_button)
        musicControls = findViewById(R.id.music_controls)
        playPauseButton = findViewById(R.id.play_pause_button)
        skipButton = findViewById(R.id.skip_button)
    }

    private fun setupMapFragment() {
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)
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

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        try {
            // Set the custom map style
            val style = MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style_watchdogs2)
            mMap.setMapStyle(style)
        } catch (e: Exception) {
            Log.e("MainActivity", "Error loading map style", e)
        }

        // Set custom info window adapter
        mMap.setInfoWindowAdapter(CustomInfoWindowAdapter(this))

        // Add custom markers with neon theme
        addCustomMarkers()

        // Enable my location if permission granted
        enableMyLocation()

        // Move camera to a default location (San Francisco - Watch Dogs 2 setting)
        val sanFrancisco = LatLng(37.7749, -122.4194)
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(sanFrancisco, 12f))
    }

    private fun enableMyLocation() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            try {
                mMap.isMyLocationEnabled = true
                mMap.uiSettings.isMyLocationButtonEnabled = true
            } catch (e: SecurityException) {
                Log.e("MainActivity", "Error enabling my location", e)
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
            val latLng = LatLng(address.latitude, address.longitude)
            mMap.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title(location)
                    .snippet("Search Result")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
            )
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
        } else {
            Toast.makeText(this, "Location not found", Toast.LENGTH_SHORT).show()
        }
    }

    private fun addCustomMarkers() {
        val markers = listOf(
            MarkerOptions()
                .position(LatLng(37.7749, -122.4194))
                .title("DedSec HQ")
                .snippet("Main Operations Center")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_home)),
            MarkerOptions()
                .position(LatLng(37.7849, -122.4094))
                .title("Police Station")
                .snippet("SFPD - High Security")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_police)),
            MarkerOptions()
                .position(LatLng(37.7649, -122.4294))
                .title("Blume Corporation")
                .snippet("ctOS Control Center")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_bank)),
            MarkerOptions()
                .position(LatLng(37.7899, -122.4344))
                .title("Hacker Space")
                .snippet("Underground Meeting Point")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_cafe)),
            MarkerOptions()
                .position(LatLng(37.7549, -122.4144))
                .title("Safe House")
                .snippet("Hideout Location")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_hospital)),
            MarkerOptions()
                .position(LatLng(37.7799, -122.4244))
                .title("Data Center")
                .snippet("Server Farm")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_school)),
            MarkerOptions()
                .position(LatLng(37.7699, -122.4394))
                .title("Training Ground")
                .snippet("Skills Development")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_gym)),
            MarkerOptions()
                .position(LatLng(37.7949, -122.4144))
                .title("Supply Point")
                .snippet("Equipment & Resources")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_restaurant)),
            MarkerOptions()
                .position(LatLng(37.7599, -122.4244))
                .title("Charging Station")
                .snippet("Drone & RC Charging")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_fuel)),
            MarkerOptions()
                .position(LatLng(37.7849, -122.4444))
                .title("Black Market")
                .snippet("Illegal Goods")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pharmacy)),
            MarkerOptions()
                .position(LatLng(37.7649, -122.4144))
                .title("Fight Club")
                .snippet("Underground Arena")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_fighting_gym))
        )

        markers.forEach { marker ->
            mMap.addMarker(marker)
        }
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

    override fun onStop() {
        super.onStop()
        spotifyAppRemote?.let {
            SpotifyAppRemote.disconnect(it)
        }
    }
}