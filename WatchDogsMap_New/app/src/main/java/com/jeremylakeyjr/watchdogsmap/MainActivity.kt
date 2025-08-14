package com.jeremylakeyjr.watchdogsmap

import android.Manifest
import android.animation.AnimatorListenerAdapter
import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Color
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.jeremylakeyjr.watchdogsmap.databinding.ActivityMainBinding
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.OnMapReadyCallback
import org.maplibre.android.maps.Style
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.LineString
import org.maplibre.geojson.Point
import org.maplibre.android.style.layers.LineLayer
import org.maplibre.android.style.layers.Property
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException

// Data classes for parsing the routing API response
data class RouteResponse(val features: List<RouteFeature>)
data class RouteFeature(val geometry: RouteGeometry, val properties: RouteProperties)
data class RouteGeometry(val coordinates: List<List<Double>>)
data class RouteProperties(val segments: List<RouteSegment>)
data class RouteSegment(val steps: List<RouteStep>)
data class RouteStep(val instruction: String, val name: String, val way_points: List<Int>)

class MainActivity : AppCompatActivity(), OnMapReadyCallback, LocationListener {

    private lateinit var binding: ActivityMainBinding
    private lateinit var map: MapLibreMap
    private lateinit var searchResultsAdapter: SearchResultsAdapter
    private lateinit var turnInstructionsAdapter: TurnInstructionsAdapter
    private lateinit var locationManager: LocationManager
    private lateinit var routingService: RoutingService

    private var userLocation: LatLng? = null
    private var destination: LatLng? = null
    private var currentRoute: FeatureCollection? = null

    private val sourceId = "marker-source"
    private val layerId = "marker-layer"
    private val userLocationSourceId = "user-location-source"
    private val userLocationLayerId = "user-location-layer"
    private val routeSourceId = "route-source"
    private val routeLayerId = "route-layer"
    private val locationPermissionCode = 2

    private val scannerHandler = Handler(Looper.getMainLooper())
    private var highlightedFeatures = mutableListOf<Feature>()
    private val scannerRunnable = object : Runnable {
        override fun run() {
            userLocation?.let { scanForNearbyPois(it) }
            scannerHandler.postDelayed(this, 3000) // Scan every 3 seconds
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // It's essential to get the MapLibre instance before inflating the layout.
        MapLibre.getInstance(this)
        
        // Initialize ViewBinding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // It's crucial to forward lifecycle events to the MapView.
        binding.mapView.onCreate(savedInstanceState)
        binding.mapView.getMapAsync(this)

        setupSearch()
        setupRouting()
        setupTurnInstructions()
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.navigateButton.setOnClickListener {
            userLocation?.let { user ->
                destination?.let { dest ->
                    val selectedProfileId = binding.routingProfileGroup.checkedRadioButtonId
                    val profile = when (selectedProfileId) {
                        R.id.profile_walking -> "walking"
                        R.id.profile_cycling -> "cycling"
                        else -> "driving"
                    }
                    getRoute(user, dest, profile)
                }
            }
        }

        binding.infoCloseButton.setOnClickListener {
            binding.infoPopup.visibility = View.GONE
        }

        binding.hackButton.setOnClickListener {
            binding.hackButton.isEnabled = false
            val colorFrom = ContextCompat.getColor(this, R.color.dark_gray)
            val colorTo = Color.RED
            val colorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), colorFrom, colorTo, colorFrom)
            colorAnimation.duration = 500 // 0.5 second
            colorAnimation.addUpdateListener { animator ->
                binding.infoPopup.setCardBackgroundColor(animator.animatedValue as Int)
            }
            colorAnimation.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: android.animation.Animator) {
                    super.onAnimationEnd(animation)
                    binding.hackButton.text = "Hacked"
                }
            })
            colorAnimation.start()
        }
    }

    private fun setupRouting() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.maptiler.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        routingService = retrofit.create(RoutingService::class.java)
    }

    private fun setupSearch() {
        searchResultsAdapter = SearchResultsAdapter { address ->
            destination = LatLng(address.latitude, address.longitude)
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(destination!!, 15.0))
            binding.searchResultsRecyclerView.visibility = View.GONE
            binding.navigateButton.visibility = View.VISIBLE
            binding.routingProfileGroup.visibility = View.VISIBLE
            binding.turnInstructionsCard.visibility = View.GONE
            binding.searchView.setQuery("", false)
            binding.searchView.clearFocus()
        }
        binding.searchResultsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.searchResultsRecyclerView.adapter = searchResultsAdapter

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { searchLocation(it) }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrEmpty()) {
                    binding.searchResultsRecyclerView.visibility = View.GONE
                } else {
                    searchLocation(newText)
                }
                binding.turnInstructionsCard.visibility = View.GONE
                return true
            }
        })
    }

    private fun setupTurnInstructions() {
        turnInstructionsAdapter = TurnInstructionsAdapter(emptyList())
        binding.turnInstructionsRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.turnInstructionsRecyclerView.adapter = turnInstructionsAdapter
    }

    override fun onMapReady(maplibreMap: MapLibreMap) {
        this.map = maplibreMap
        val apiKey = BuildConfig.MAPTILER_API_KEY
        val styleUrl = "https://api.maptiler.com/maps/dataviz-dark/style.json?key=$apiKey"
        
        map.setStyle(Style.Builder().fromUri(styleUrl)) { style ->
            setupMarkers(style)
            setupUserLocation(style)
            setupRoute(style)
            checkLocationPermission()
            scannerHandler.post(scannerRunnable) // Start scanner
        }
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(34.0522, -118.2437), 12.0))

        map.addOnMapClickListener { point ->
            val screenPoint = map.projection.toScreenLocation(point)
            val features = map.queryRenderedFeatures(screenPoint, layerId)
            if (features.isNotEmpty()) {
                val feature = features[0]
                val title = feature.getStringProperty("title")
                val description = feature.getStringProperty("description")
                binding.infoTitle.text = title
                binding.infoDescription.text = description
                binding.hackButton.isEnabled = true
                binding.hackButton.text = "Hack"
                binding.infoPopup.visibility = View.VISIBLE
            }
            true
        }
    }

    private fun checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), locationPermissionCode)
            return
        }
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 5f, this)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == locationPermissionCode && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            checkLocationPermission()
        }
    }

    override fun onLocationChanged(location: Location) {
        userLocation = LatLng(location.latitude, location.longitude)
        val point = Point.fromLngLat(location.longitude, location.latitude)
        map.style?.getSourceAs<GeoJsonSource>(userLocationSourceId)?.setGeoJson(Feature.fromGeometry(point))

        // Check user's progress along the route
        currentRoute?.let { route ->
            val features = route.features() ?: return
            if (features.isEmpty()) return
            val geometry = features[0].geometry()
            if (geometry !is LineString) return
            val coordinates = geometry.coordinates()
            
            for ((index, step) in turnInstructionsAdapter.instructions.withIndex()) {
                if (step.way_points.isEmpty()) continue
                val stepPointIndex = step.way_points[0]
                if (stepPointIndex >= coordinates.size) continue
                val stepCoordinate = coordinates[stepPointIndex]
                val stepLocation = LatLng(stepCoordinate.latitude(), stepCoordinate.longitude())
                if (userLocation!!.distanceTo(stepLocation) < 20) { // 20 meters tolerance
                    if (index != turnInstructionsAdapter.highlightedPosition) {
                        turnInstructionsAdapter.setHighlightedPosition(index)
                        binding.turnInstructionsRecyclerView.scrollToPosition(index)
                    }
                    break
                }
            }
        }
    }

    private fun setupMarkers(style: Style) {
        // Add all icons to the style
        style.addImage("police", BitmapFactory.decodeResource(this.resources, R.drawable.ic_hacker_police))
        style.addImage("hospital", BitmapFactory.decodeResource(this.resources, R.drawable.ic_hacker_hospital))
        style.addImage("fire_station", BitmapFactory.decodeResource(this.resources, R.drawable.ic_hacker_fire_station))
        style.addImage("atm", BitmapFactory.decodeResource(this.resources, R.drawable.ic_hacker_atm))
        style.addImage("atm_highlighted", BitmapFactory.decodeResource(this.resources, R.drawable.ic_hacker_atm_highlighted))
        style.addImage("restaurant", BitmapFactory.decodeResource(this.resources, R.drawable.ic_hacker_restaurant))
        style.addImage("gym", BitmapFactory.decodeResource(this.resources, R.drawable.ic_hacker_gym))
        style.addImage("park", BitmapFactory.decodeResource(this.resources, R.drawable.ic_hacker_park))
        style.addImage("store", BitmapFactory.decodeResource(this.resources, R.drawable.ic_hacker_store))

        val features = listOf(
            createFeature(LatLng(34.0522, -118.2437), "Police Station", "LAPD Central Division", "police"),
            createFeature(LatLng(34.0542, -118.2457), "Hospital", "General Hospital", "hospital"),
            createFeature(LatLng(34.0562, -118.2477), "Fire Station", "LAFD Station 3", "fire_station"),
            createFeature(LatLng(34.0582, -118.2497), "ATM", "24/7 ATM Access", "atm"),
            createFeature(LatLng(34.0600, -118.2500), "Restaurant", "Generic Eats", "restaurant"),
            createFeature(LatLng(34.0500, -118.2550), "Gym", "Muscle Up", "gym"),
            createFeature(LatLng(34.0480, -118.2400), "Park", "City Green Park", "park"),
            createFeature(LatLng(34.0530, -118.2580), "Store", "SuperMart", "store")
        )

        style.addSource(GeoJsonSource(sourceId, FeatureCollection.fromFeatures(features)))
        val symbolLayer = SymbolLayer(layerId, sourceId).withProperties(
            PropertyFactory.iconImage("{icon}"),
            PropertyFactory.iconAllowOverlap(true),
            PropertyFactory.iconIgnorePlacement(true)
        )
        style.addLayer(symbolLayer)
    }

    private fun setupUserLocation(style: Style) {
        style.addSource(GeoJsonSource(userLocationSourceId))
        style.addImage("user_location", BitmapFactory.decodeResource(this.resources, R.drawable.ic_hacker_location_marker))
        val symbolLayer = SymbolLayer(userLocationLayerId, userLocationSourceId).withProperties(
            PropertyFactory.iconImage("user_location"),
            PropertyFactory.iconAllowOverlap(true),
            PropertyFactory.iconIgnorePlacement(true)
        )
        style.addLayer(symbolLayer)
    }

    private fun setupRoute(style: Style) {
        style.addSource(GeoJsonSource(routeSourceId))
        val lineLayer = LineLayer(routeLayerId, routeSourceId).withProperties(
            PropertyFactory.lineColor(ContextCompat.getColor(this, R.color.cyan)),
            PropertyFactory.lineWidth(5f)
        )
        style.addLayer(lineLayer)
    }

    private fun getRoute(start: LatLng, end: LatLng, profile: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = routingService.getRoute(
                    profile = profile,
                    coordinates = "${start.longitude},${start.latitude};${end.longitude},${end.latitude}",
                    apiKey = BuildConfig.MAPTILER_API_KEY
                )
                if (response.isSuccessful) {
                    val routeResponse = response.body()
                    val features = routeResponse?.features
                    if (features != null && features.isNotEmpty()) {
                        val coordinates = features[0].geometry.coordinates
                        val points = coordinates.map { Point.fromLngLat(it[0], it[1]) }
                        val lineString = LineString.fromLngLats(points)
                        val routeFeature = Feature.fromGeometry(lineString)
                        currentRoute = FeatureCollection.fromFeatures(arrayOf(routeFeature))
                        
                        val steps = routeResponse.features.firstOrNull()?.properties?.segments?.firstOrNull()?.steps ?: emptyList()
                        runOnUiThread {
                            map.style?.getSourceAs<GeoJsonSource>(routeSourceId)?.setGeoJson(currentRoute)
                            turnInstructionsAdapter.updateInstructions(steps)
                            binding.turnInstructionsCard.visibility = View.VISIBLE
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun createFeature(latLng: LatLng, title: String, description: String, icon: String): Feature {
        val point = Point.fromLngLat(latLng.longitude, latLng.latitude)
        val feature = Feature.fromGeometry(point)
        feature.addStringProperty("title", title)
        feature.addStringProperty("description", description)
        feature.addStringProperty("icon", icon)
        return feature
    }

    private fun searchLocation(location: String) {
        val geocoder = Geocoder(this)
        try {
            val addressList: List<Address>? = geocoder.getFromLocationName(location, 5)
            if (addressList != null && addressList.isNotEmpty()) {
                searchResultsAdapter.setResults(addressList)
                binding.searchResultsRecyclerView.visibility = View.VISIBLE
            } else {
                binding.searchResultsRecyclerView.visibility = View.GONE
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun scanForNearbyPois(userLocation: LatLng) {
        val source = map.style?.getSourceAs<GeoJsonSource>(sourceId) ?: return
        val currentFeatures = source.featureCollection?.features() ?: return

        // Revert previously highlighted features
        highlightedFeatures.forEach { feature ->
            if (feature.getStringProperty("icon").endsWith("_highlighted")) {
                feature.addStringProperty("icon", "atm")
            }
        }
        highlightedFeatures.clear()

        // Find and highlight new features
        val featuresToUpdate = mutableListOf<Feature>()
        for (feature in currentFeatures) {
            if (feature.geometry() is Point && feature.getStringProperty("icon") == "atm") {
                val point = feature.geometry() as Point
                val poiLocation = LatLng(point.latitude(), point.longitude())
                if (userLocation.distanceTo(poiLocation) < 500) { // 500 meter radius
                    feature.addStringProperty("icon", "atm_highlighted")
                    highlightedFeatures.add(feature)
                }
            }
            featuresToUpdate.add(feature)
        }

        source.setGeoJson(FeatureCollection.fromFeatures(featuresToUpdate))
    }

    //<editor-fold desc="Lifecycle methods">
    // It is crucial to forward all Activity lifecycle events to the MapView.
    override fun onStart() {
        super.onStart()
        binding.mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        binding.mapView.onResume()
        scannerHandler.post(scannerRunnable)
    }

    override fun onPause() {
        super.onPause()
        binding.mapView.onPause()
        scannerHandler.removeCallbacks(scannerRunnable)
    }

    override fun onStop() {
        super.onStop()
        binding.mapView.onStop()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.mapView.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.mapView.onDestroy()
    }
    //</editor-fold>
}
