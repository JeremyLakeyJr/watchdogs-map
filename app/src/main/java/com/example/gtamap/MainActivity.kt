package com.example.gtamap

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.android.ktx.awaitMap
import com.google.maps.android.ktx.awaitMapLoad
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mapView: MapView
    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false)) {
            enableMyLocation()
        } else {
            Toast.makeText(this, "Location permission is required for GPS features.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(R.layout.activity_main)

        mapView = findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        lifecycleScope.launch {
            map = mapView.awaitMap()
            onMapReady(map)
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.map = googleMap
        setupUI()
        checkLocationPermissionsAndEnableComponent()
    }

    private fun setupUI() {
        val searchView = findViewById<SearchView>(R.id.searchView)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrBlank()) {
                    searchForLocation(query)
                    searchView.clearFocus()
                }
                return true
            }

            override fun onQueryTextChange(newText: String?) = false
        })
    }

    private fun searchForLocation(query: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val encodedQuery = URLEncoder.encode(query, "UTF-8")
                val url = URL("https://maps.googleapis.com/maps/api/geocode/json?address=$encodedQuery&key=${BuildConfig.MAPS_API_KEY}")
                val connection = url.openConnection() as HttpURLConnection
                val jsonResponse = connection.inputStream.bufferedReader().readText()
                val results = JSONObject(jsonResponse).getJSONArray("results")

                if (results.length() > 0) {
                    val firstResult = results.getJSONObject(0)
                    val location = firstResult.getJSONObject("geometry").getJSONObject("location")
                    val lat = location.getDouble("lat")
                    val lon = location.getDouble("lon")
                    withContext(Dispatchers.Main) {
                        val latLng = LatLng(lat, lon)
                        map.addMarker(MarkerOptions().position(latLng).title(query))
                        map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14.0f))
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

    private fun checkLocationPermissionsAndEnableComponent() {
        if (hasLocationPermission()) {
            enableMyLocation()
        } else {
            locationPermissionRequest.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
        }
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    private fun enableMyLocation() {
        map.isMyLocationEnabled = true
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val latLng = LatLng(location.latitude, location.longitude)
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15.0f))
            }
        }
    }

    private fun getDirections(from: LatLng, to: LatLng) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val origin = "${from.latitude},${from.longitude}"
                val destination = "${to.latitude},${to.longitude}"
                val url = URL("https://maps.googleapis.com/maps/api/directions/json?origin=$origin&destination=$destination&key=${BuildConfig.MAPS_API_KEY}")
                val connection = url.openConnection() as HttpURLConnection
                val jsonResponse = connection.inputStream.bufferedReader().readText()
                val routes = JSONObject(jsonResponse).getJSONArray("routes")

                if (routes.length() > 0) {
                    val points = routes.getJSONObject(0).getJSONObject("overview_polyline").getString("points")
                    val decodedPath = decodePoly(points)
                    withContext(Dispatchers.Main) {
                        map.addPolyline(PolylineOptions().addAll(decodedPath).color(Color.BLUE).width(10f))
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@MainActivity, "Directions not found.", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("DirectionsError", "Failed to get directions", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@MainActivity, "Directions failed. Check connection.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun decodePoly(encoded: String): List<LatLng> {
        val poly = ArrayList<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat

            shift = 0
            result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng

            val p = LatLng(lat.toDouble() / 1E5, lng.toDouble() / 1E5)
            poly.add(p)
        }
        return poly
    }

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
