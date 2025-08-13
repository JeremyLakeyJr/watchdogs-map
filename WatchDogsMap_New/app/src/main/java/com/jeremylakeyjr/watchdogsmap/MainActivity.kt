package com.jeremylakeyjr.watchdogsmap

import android.graphics.BitmapFactory
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import org.maplibre.geojson.Feature
import org.maplibre.geojson.FeatureCollection
import org.maplibre.geojson.Point
import org.maplibre.android.MapLibre
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.maps.OnMapReadyCallback
import org.maplibre.android.maps.Style
import org.maplibre.android.style.layers.Property
import org.maplibre.android.style.layers.PropertyFactory
import org.maplibre.android.style.layers.SymbolLayer
import org.maplibre.android.style.sources.GeoJsonSource
import java.io.IOException

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mapView: MapView
    private lateinit var map: MapLibreMap
    private lateinit var searchView: SearchView

    private val sourceId = "marker-source"
    private val layerId = "marker-layer"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapLibre.getInstance(this)
        setContentView(R.layout.activity_main)

        mapView = findViewById(R.id.mapView)
        searchView = findViewById(R.id.searchView)

        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { searchLocation(it) }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })
    }

    override fun onMapReady(maplibreMap: MapLibreMap) {
        this.map = maplibreMap
        map.setStyle(Style.Builder().fromUri("https://demotiles.maplibre.org/style.json")) { style ->
            setupMarkers(style)
        }
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(34.0522, -118.2437), 12.0))
    }

    private fun setupMarkers(style: Style) {
        style.addSource(GeoJsonSource(sourceId))
        val symbolLayer = SymbolLayer(layerId, sourceId)
        symbolLayer.setProperties(
            PropertyFactory.iconImage("{icon}"),
            PropertyFactory.textField("{title}"),
            PropertyFactory.iconAllowOverlap(true),
            PropertyFactory.iconAnchor(Property.ICON_ANCHOR_BOTTOM),
            PropertyFactory.textAnchor(Property.TEXT_ANCHOR_TOP),
            PropertyFactory.textOffset(arrayOf(0f, 1.0f))
        )
        style.addLayer(symbolLayer)

        // Add icons to style
        style.addImage("police", BitmapFactory.decodeResource(this.resources, R.drawable.ic_police))
        style.addImage("hospital", BitmapFactory.decodeResource(this.resources, R.drawable.ic_hospital))
        style.addImage("restaurant", BitmapFactory.decodeResource(this.resources, R.drawable.ic_restaurant))
        style.addImage("gym", BitmapFactory.decodeResource(this.resources, R.drawable.ic_gym))
        style.addImage("bank", BitmapFactory.decodeResource(this.resources, R.drawable.ic_bank))
        style.addImage("cafe", BitmapFactory.decodeResource(this.resources, R.drawable.ic_cafe))
        style.addImage("fuel", BitmapFactory.decodeResource(this.resources, R.drawable.ic_fuel))
        style.addImage("home", BitmapFactory.decodeResource(this.resources, R.drawable.ic_home))
        style.addImage("school", BitmapFactory.decodeResource(this.resources, R.drawable.ic_school))
        style.addImage("pharmacy", BitmapFactory.decodeResource(this.resources, R.drawable.ic_pharmacy))
        style.addImage("fighting_gym", BitmapFactory.decodeResource(this.resources, R.drawable.ic_fighting_gym))


        val features = listOf(
            createFeature(LatLng(34.0522, -118.2437), "Police Station", "police"),
            createFeature(LatLng(34.0542, -118.2457), "Hospital", "hospital"),
            createFeature(LatLng(34.0562, -118.2477), "Restaurant", "restaurant"),
            createFeature(LatLng(34.0582, -118.2497), "Gym", "gym"),
            createFeature(LatLng(34.0602, -118.2517), "Bank", "bank"),
            createFeature(LatLng(34.0622, -118.2537), "Cafe", "cafe"),
            createFeature(LatLng(34.0642, -118.2557), "Gas Station", "fuel"),
            createFeature(LatLng(34.0662, -118.2577), "Home", "home"),
            createFeature(LatLng(34.0682, -118.2597), "School", "school"),
            createFeature(LatLng(34.0702, -118.2617), "Pharmacy", "pharmacy"),
            createFeature(LatLng(34.0722, -118.2637), "Fighting Gym", "fighting_gym")
        )

        style.getSourceAs<GeoJsonSource>(sourceId)?.setGeoJson(FeatureCollection.fromFeatures(features))
    }

    private fun createFeature(latLng: LatLng, title: String, icon: String): Feature {
        val point = Point.fromLngLat(latLng.longitude, latLng.latitude)
        val feature = Feature.fromGeometry(point)
        feature.addStringProperty("title", title)
        feature.addStringProperty("icon", icon)
        return feature
    }

    private fun searchLocation(location: String) {
        val geocoder = Geocoder(this)
        try {
            val addressList = geocoder.getFromLocationName(location, 1)
            if (addressList != null && addressList.isNotEmpty()) {
                val address = addressList[0]
                val latLng = LatLng(address.latitude, address.longitude)
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15.0))
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    //<editor-fold desc="Lifecycle methods">
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
    //</editor-fold>
}
