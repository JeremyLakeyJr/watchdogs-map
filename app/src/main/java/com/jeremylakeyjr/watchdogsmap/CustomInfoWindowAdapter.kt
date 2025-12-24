package com.jeremylakeyjr.watchdogsmap

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker

/**
 * Custom adapter for displaying marker info windows with Watch Dogs 2 neon styling.
 * 
 * This adapter provides a cyberpunk-themed info window with:
 * - Dark background with transparency
 * - Neon cyan colored title with glow effect
 * - White text for snippets
 * 
 * @param context The application context for inflating layouts
 */
class CustomInfoWindowAdapter(private val context: Context) : GoogleMap.InfoWindowAdapter {

    /**
     * Returns null to use the default info window frame with our custom content.
     */
    override fun getInfoWindow(marker: Marker): View? {
        return null // Use default frame with custom content
    }

    /**
     * Provides custom content for the info window.
     * 
     * @param marker The marker for which to display the info window
     * @return A view containing the customized info window content
     */
    override fun getInfoContents(marker: Marker): View {
        val view = LayoutInflater.from(context).inflate(R.layout.custom_info_window, null)
        
        val titleTextView = view.findViewById<TextView>(R.id.info_window_title)
        val snippetTextView = view.findViewById<TextView>(R.id.info_window_snippet)
        
        // Set marker title and snippet with Watch Dogs 2 styling
        titleTextView.text = marker.title
        snippetTextView.text = marker.snippet
        
        return view
    }
}
