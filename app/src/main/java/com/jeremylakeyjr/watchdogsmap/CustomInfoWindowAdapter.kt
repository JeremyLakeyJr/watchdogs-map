package com.jeremylakeyjr.watchdogsmap

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker

class CustomInfoWindowAdapter(private val context: Context) : GoogleMap.InfoWindowAdapter {

    override fun getInfoWindow(marker: Marker): View? {
        return null // Use default frame
    }

    override fun getInfoContents(marker: Marker): View {
        val view = LayoutInflater.from(context).inflate(R.layout.custom_info_window, null)
        
        val titleTextView = view.findViewById<TextView>(R.id.info_window_title)
        val snippetTextView = view.findViewById<TextView>(R.id.info_window_snippet)
        
        titleTextView.text = marker.title
        snippetTextView.text = marker.snippet
        
        return view
    }
}
