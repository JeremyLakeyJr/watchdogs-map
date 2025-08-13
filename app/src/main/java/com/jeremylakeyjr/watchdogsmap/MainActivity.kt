package com.jeremylakeyjr.watchdogsmap

import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
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
import com.spotify.sdk.android.authentication.AuthenticationClient
import com.spotify.sdk.android.authentication.AuthenticationRequest
import com.spotify.sdk.android.authentication.AuthenticationResponse
import java.io.IOException

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var searchView: SearchView
    private lateinit var connectButton: Button
    private lateinit var musicControls: LinearLayout
    private lateinit var playPauseButton: Button
    private lateinit var skipButton: Button

    // IMPORTANT: Replace this with your own Spotify Client ID
    private val CLIENT_ID = "YOUR_SPOTIFY_CLIENT_ID"
    private val REDIRECT_URI = "spotify-sdk://auth"
    private var spotifyAppRemote: SpotifyAppRemote? = null
    private var isPlaying = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        searchView = findViewById(R.id.searchView)
        connectButton = findViewById(R.id.connect_button)
        musicControls = findViewById(R.id.music_controls)
        playPauseButton = findViewById(R.id.play_pause_button)
        skipButton = findViewById(R.id.skip_button)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    searchLocation(it)
                }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })

        connectButton.setOnClickListener {
            connectToSpotify()
        }

        playPauseButton.setOnClickListener {
            spotifyAppRemote?.playerApi?.let {
                if (isPlaying) {
                    it.pause()
                    playPauseButton.text = "Play"
                } else {
                    it.resume()
                    playPauseButton.text = "Pause"
                }
                isPlaying = !isPlaying
            }
        }

        skipButton.setOnClickListener {
            spotifyAppRemote?.playerApi?.skipNext()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Set the custom map style and info window adapter
        val style = MapStyleOptions.loadRawResourceStyle(this, R.raw.map_style_watchdogs2)
        mMap.setMapStyle(style)
        mMap.setInfoWindowAdapter(CustomInfoWindowAdapter(this))

        // Add custom markers
        addCustomMarkers()

        // Move camera to a default location
        val initialLocation = LatLng(34.0522, -118.2437) // Los Angeles
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialLocation, 12f))
    }

    private fun searchLocation(location: String) {
        val geocoder = Geocoder(this)
        var addressList: List<Address>? = null

        try {
            addressList = geocoder.getFromLocationName(location, 1)
        } catch (e: IOException) {
            e.printStackTrace()
        }

        if (addressList != null && addressList.isNotEmpty()) {
            val address = addressList[0]
            val latLng = LatLng(address.latitude, address.longitude)
            mMap.addMarker(MarkerOptions().position(latLng).title(location))
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10f))
        }
    }

    private fun addCustomMarkers() {
        val markers = listOf(
            MarkerOptions()
                .position(LatLng(34.0522, -118.2437))
                .title("Police Station")
                .snippet("LSPD")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_police)),
            MarkerOptions()
                .position(LatLng(34.0542, -118.2457))
                .title("Hospital")
                .snippet("General Hospital")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_hospital)),
            MarkerOptions()
                .position(LatLng(34.0562, -118.2477))
                .title("Restaurant")
                .snippet("Cluckin' Bell")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_restaurant)),
            MarkerOptions()
                .position(LatLng(34.0582, -118.2497))
                .title("Gym")
                .snippet("Golds Gym")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_gym)),
            MarkerOptions()
                .position(LatLng(34.0602, -118.2517))
                .title("Bank")
                .snippet("Fleeca Bank")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_bank)),
            MarkerOptions()
                .position(LatLng(34.0622, -118.2537))
                .title("Cafe")
                .snippet("Cool Beans")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_cafe)),
            MarkerOptions()
                .position(LatLng(34.0642, -118.2557))
                .title("Gas Station")
                .snippet("Xoomer")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_fuel)),
            MarkerOptions()
                .position(LatLng(34.0662, -118.2577))
                .title("Home")
                .snippet("Player's House")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_home)),
            MarkerOptions()
                .position(LatLng(34.0682, -118.2597))
                .title("School")
                .snippet("Los Santos High")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_school)),
            MarkerOptions()
                .position(LatLng(34.0702, -118.2617))
                .title("Pharmacy")
                .snippet("Drug Store")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pharmacy)),
            MarkerOptions()
                .position(LatLng(34.0722, -118.2637))
                .title("Fighting Gym")
                .snippet("Brawler's Gym")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_fighting_gym))
        )

        markers.forEach { mMap.addMarker(it) }
    }

    private fun connectToSpotify() {
        val request = AuthenticationRequest.Builder(CLIENT_ID, AuthenticationResponse.Type.TOKEN, REDIRECT_URI)
            .setScopes(arrayOf("app-remote-control", "playlist-read-private", "playlist-read-collaborative"))
            .build()
        AuthenticationClient.openLoginActivity(this, 1337, request)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1337) {
            val response = AuthenticationClient.getResponse(resultCode, data)
            when (response.type) {
                AuthenticationResponse.Type.TOKEN -> {
                    val connectionParams = ConnectionParams.Builder(CLIENT_ID)
                        .setRedirectUri(REDIRECT_URI)
                        .build()
                    SpotifyAppRemote.connect(this, connectionParams, object : Connector.ConnectionListener {
                        override fun onConnected(appRemote: SpotifyAppRemote) {
                            spotifyAppRemote = appRemote
                            Log.d("MainActivity", "Connected to Spotify!")
                            connectButton.visibility = View.GONE
                            musicControls.visibility = View.VISIBLE
                        }

                        override fun onFailure(throwable: Throwable) {
                            Log.e("MainActivity", throwable.message, throwable)
                        }
                    })
                }
                AuthenticationResponse.Type.ERROR -> Log.e("MainActivity", "Auth error: " + response.error)
                else -> {}
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