package com.example.gtamap

import android.graphics.BitmapFactory
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.gtamap.network.OverpassApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.OnMapReadyCallback
import org.maplibre.android.maps.Style
import org.maplibre.android.plugins.annotation.Symbol
import org.maplibre.android.plugins.annotation.SymbolManager
import org.maplibre.android.plugins.annotation.SymbolOptions

data class Poi(val name: String, val type: String, val latLng: LatLng)

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mapView: MapView
    private lateinit var maplibreMap: MapLibreMap
    private val overpassApi = OverpassApi()
    private var symbolManager: SymbolManager? = null
    private var homeSymbol: Symbol? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapLibre.getInstance(this)
        setContentView(R.layout.activity_main)

        mapView = findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
    }

    override fun onMapReady(maplibreMap: MapLibreMap) {
        this.maplibreMap = maplibreMap
        val styleUrl = "https://api.maptiler.com/maps/streets/style.json?key=${BuildConfig.MAP_API_KEY}"

        maplibreMap.setStyle(styleUrl) { style ->
            val cameraPosition = CameraPosition.Builder()
                .target(LatLng(40.7128, -74.0060))
                .zoom(12.0)
                .build()
            maplibreMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))

            symbolManager = SymbolManager(mapView, maplibreMap, style)
            addIconsToStyle(style)
            addMapIdleListener()
            addHomeLongClickListener()
        }
    }

    private fun addIconsToStyle(style: Style) {
        // TODO: Add your icon files to res/drawable and uncomment these lines
         style.addImage("ic_gym", BitmapFactory.decodeResource(this.resources, R.drawable.ic_gym))
         style.addImage("ic_restaurant", BitmapFactory.decodeResource(this.resources, R.drawable.ic_restaurant))
         style.addImage("ic_fuel", BitmapFactory.decodeResource(this.resources, R.drawable.ic_fuel))
         style.addImage("ic_hospital", BitmapFactory.decodeResource(this.resources, R.drawable.ic_hospital))
         style.addImage("ic_pharmacy", BitmapFactory.decodeResource(this.resources, R.drawable.ic_pharmacy))
         style.addImage("ic_police", BitmapFactory.decodeResource(this.resources, R.drawable.ic_police))
         style.addImage("ic_bank", BitmapFactory.decodeResource(this.resources, R.drawable.ic_bank))
         style.addImage("ic_cafe", BitmapFactory.decodeResource(this.resources, R.drawable.ic_cafe))
         style.addImage("ic_school", BitmapFactory.decodeResource(this.resources, R.drawable.ic_school))
         style.addImage("ic_fighting_gym", BitmapFactory.decodeResource(this.resources, R.drawable.ic_fighting_gym))
         style.addImage("ic_home", BitmapFactory.decodeResource(this.resources, R.drawable.ic_home))
    }

    private fun addMapIdleListener() {
        maplibreMap.addOnCameraIdleListener {
            fetchPois()
        }
    }

    private fun addHomeLongClickListener() {
        maplibreMap.addOnMapLongClickListener { latLng ->
            setHomeLocation(latLng)
            true
        }
    }

    private fun setHomeLocation(latLng: LatLng) {
        homeSymbol?.let { symbolManager?.delete(it) }
        val homeOptions = SymbolOptions()
            .withLatLng(latLng)
            .withIconImage("ic_home") // This will use the default icon until you add the file
            .withIconSize(2.0f)
            .withTextField("Home")
        homeSymbol = symbolManager?.create(homeOptions)
        Toast.makeText(this, "Home location set!", Toast.LENGTH_SHORT).show()
    }

    private fun fetchPois() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val boundingBox = maplibreMap.projection.visibleRegion.latLngBounds
                val response = overpassApi.fetchPois(boundingBox)
                val pois = parsePois(response)
                withContext(Dispatchers.Main) {
                    addPoisToMap(pois)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun parsePois(response: String): List<Poi> {
        val pois = mutableListOf<Poi>()
        val json = JSONObject(response)
        val elements = json.getJSONArray("elements")
        for (i in 0 until elements.length()) {
            val element = elements.getJSONObject(i)
            val tags = element.getJSONObject("tags")
            val name = tags.optString("name", "N/A")
            val lat = element.getDouble("lat")
            val lon = element.getDouble("lon")
            val type = when {
                tags.has("amenity") -> tags.getString("amenity")
                tags.has("sport") && tags.getString("sport") == "martial_arts" -> "fighting_gym"
                else -> ""
            }
            if (type.isNotEmpty()) {
                pois.add(Poi(name, type, LatLng(lat, lon)))
            }
        }
        return pois
    }

    private fun addPoisToMap(pois: List<Poi>) {
        symbolManager?.deleteAll()
        homeSymbol?.let {
            val homeOptions = SymbolOptions().withLatLng(it.latLng).withIconImage("ic_home").withIconSize(2.0f).withTextField("Home")
            homeSymbol = symbolManager?.create(homeOptions)
        }
        val symbolOptions = pois.map { poi ->
            SymbolOptions()
                .withLatLng(poi.latLng)
                .withTextField(poi.name)
                .withIconImage(getIconForType(poi.type))
                .withIconSize(1.5f)
        }
        symbolManager?.create(symbolOptions)
    }

    private fun getIconForType(type: String): String {
        return when (type) {
            "gym" -> "ic_gym"
            "restaurant" -> "ic_restaurant"
            "fuel" -> "ic_fuel"
            "hospital" -> "ic_hospital"
            "pharmacy" -> "ic_pharmacy"
            "police" -> "ic_police"
            "bank", "atm" -> "ic_bank"
            "cafe" -> "ic_cafe"
            "school" -> "ic_school"
            "fighting_gym" -> "ic_fighting_gym"
            else -> "default_marker" // Use a default marker ID
        }
    }

    override fun onStart() { super.onStart(); mapView.onStart() }
    override fun onResume() { super.onResume(); mapView.onResume() }
    override fun onPause() { super.onPause(); mapView.onPause() }
    override fun onStop() { super.onStop(); mapView.onStop() }
    override fun onSaveInstanceState(outState: Bundle) { super.onSaveInstanceState(outState); mapView.onSaveInstanceState(outState) }
    override fun onLowMemory() { super.onLowMemory(); mapView.onLowMemory() }
    override fun onDestroy() { super.onDestroy(); mapView.onDestroy() }
}
