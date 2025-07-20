package com.example.gtamap.network

import org.maplibre.android.geometry.LatLngBounds
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class OverpassApi {

    private val overpassUrl = "https://overpass-api.de/api/interpreter"

    fun fetchPois(bbox: LatLngBounds): String {
        val query = """
            [out:json];
            (
              node["amenity"="restaurant"](${bbox.latSouth},${bbox.lonWest},${bbox.latNorth},${bbox.lonEast});
              node["amenity"="gym"](${bbox.latSouth},${bbox.lonWest},${bbox.latNorth},${bbox.lonEast});
              node["amenity"="fuel"](${bbox.latSouth},${bbox.lonWest},${bbox.latNorth},${bbox.lonEast});
              node["amenity"="hospital"](${bbox.latSouth},${bbox.lonWest},${bbox.latNorth},${bbox.lonEast});
              node["amenity"="pharmacy"](${bbox.latSouth},${bbox.lonWest},${bbox.latNorth},${bbox.lonEast});
              node["amenity"="police"](${bbox.latSouth},${bbox.lonWest},${bbox.latNorth},${bbox.lonEast});
              node["amenity"="bank"](${bbox.latSouth},${bbox.lonWest},${bbox.latNorth},${bbox.lonEast});
              node["amenity"="atm"](${bbox.latSouth},${bbox.lonWest},${bbox.latNorth},${bbox.lonEast});
              node["amenity"="cafe"](${bbox.latSouth},${bbox.lonWest},${bbox.latNorth},${bbox.lonEast});
              node["amenity"="school"](${bbox.latSouth},${bbox.lonWest},${bbox.latNorth},${bbox.lonEast});
              node["sport"="martial_arts"](${bbox.latSouth},${bbox.lonWest},${bbox.latNorth},${bbox.lonEast});
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
