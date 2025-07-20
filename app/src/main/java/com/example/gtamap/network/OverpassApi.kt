package com.example.gtamap.network

import org.maplibre.android.geometry.LatLngBounds
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class OverpassApi {

    private val overpassUrl = "https://overpass-api.de/api/interpreter"

    fun fetchPois(bbox: LatLngBounds): String {
        // Extract bounding box coordinates by calling the getter methods directly.
        // This is a more explicit way to access the values and resolves the
        // "Unresolved reference" error.
        val south = bbox.getLatSouth()
        val west = bbox.getLonWest()
        val north = bbox.getLatNorth()
        val east = bbox.getLonEast()

        // This query is updated to be more efficient. By defining the bounding
        // box once with `[bbox:...]`, you avoid repeating it for every node type,
        // which simplifies the query and is better practice.
        val query = """
            [out:json][bbox:$south,$west,$north,$east];
            (
              node["amenity"="restaurant"];
              node["amenity"="gym"];
              node["amenity"="fuel"];
              node["amenity"="hospital"];
              node["amenity"="pharmacy"];
              node["amenity"="police"];
              node["amenity"="bank"];
              node["amenity"="atm"];
              node["amenity"="cafe"];
              node["amenity"="school"];
              node["sport"="martial_arts"];
            );
            out body;
            >;
            out skel qt;
        """.trimIndent()

        val url = URL(overpassUrl)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.doOutput = true
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")

        val outputStream = connection.outputStream
        outputStream.write(query.toByteArray())
        outputStream.flush()
        outputStream.close()

        val inputStream = connection.inputStream
        val reader = BufferedReader(InputStreamReader(inputStream))
        val response = StringBuilder()
        var line: String?
        while (reader.readLine().also { line = it } != null) {
            response.append(line)
        }
        reader.close()
        inputStream.close()

        return response.toString()
    }
}