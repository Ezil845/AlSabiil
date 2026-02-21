package com.example.alsabiil.utils

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.location.Location
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Tasks
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object LocationService {
    private const val PREFS_NAME = "alsabiil_location_cache"
    private const val KEY_LAT = "cached_lat"
    private const val KEY_LON = "cached_lon"
    private const val KEY_CITY = "cached_city"
    private const val KEY_CITY_LAT = "city_lat"
    private const val KEY_CITY_LON = "city_lon"
    private const val CACHE_EXPIRY_MS = 24 * 60 * 60 * 1000L

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(context: Context): Location? = withContext(Dispatchers.IO) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
        try {
            Tasks.await(fusedLocationClient.lastLocation)
        } catch (e: Exception) {
            null
        }
    }

    fun getCachedLocation(context: Context): Location? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lat = prefs.getFloat(KEY_LAT, Float.MAX_VALUE).toDouble()
        val lon = prefs.getFloat(KEY_LON, Float.MAX_VALUE).toDouble()
        
        if (lat == Double.MAX_VALUE || lon == Double.MAX_VALUE) return null
        
        val cachedTime = prefs.getLong("cached_time", 0)
        if (System.currentTimeMillis() - cachedTime > CACHE_EXPIRY_MS) return null
        
        return Location("cached").apply {
            latitude = lat
            longitude = lon
        }
    }

    fun cacheLocation(context: Context, location: Location) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().apply {
            putFloat(KEY_LAT, location.latitude.toFloat())
            putFloat(KEY_LON, location.longitude.toFloat())
            putLong("cached_time", System.currentTimeMillis())
            apply()
        }
    }

    suspend fun getCityNameWithCache(context: Context, lat: Double, lon: Double): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val cachedCity = prefs.getString(KEY_CITY, null)
        val cachedLat = prefs.getFloat(KEY_CITY_LAT, Float.MAX_VALUE).toDouble()
        val cachedLon = prefs.getFloat(KEY_CITY_LON, Float.MAX_VALUE).toDouble()
        
        val isCacheValid = cachedCity != null && 
            kotlin.math.abs(cachedLat - lat) < 0.1 && 
            kotlin.math.abs(cachedLon - lon) < 0.1
        
        if (isCacheValid) return cachedCity!!
        
        val cityName = getCityName(lat, lon) ?: "Unknown Location"
        prefs.edit().apply {
            putString(KEY_CITY, cityName)
            putFloat(KEY_CITY_LAT, lat.toFloat())
            putFloat(KEY_CITY_LON, lon.toFloat())
            apply()
        }
        return cityName
    }

    private suspend fun getCityName(latitude: Double, longitude: Double): String? = withContext(Dispatchers.IO) {
        try {
            val url = URL("https://nominatim.openstreetmap.org/reverse?format=json&lat=$latitude&lon=$longitude&zoom=10&addressdetails=1&namedetails=1")
            val connection = url.openConnection() as HttpURLConnection
            connection.setRequestProperty("User-Agent", "AlSabiilApp/1.0")
            
            val content = connection.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(content)
            val namedetails = json.optJSONObject("namedetails")
            val address = json.optJSONObject("address")

            val nameAr = namedetails?.optString("name:ar")
            val nameFr = namedetails?.optString("name:fr")

            if (!nameAr.isNullOrEmpty() || !nameFr.isNullOrEmpty()) {
                listOfNotNull(nameAr, nameFr).joinToString(" ")
            } else {
                address?.optString("city") ?: address?.optString("town") ?: address?.optString("village") ?: "Unknown Location"
            }
        } catch (e: Exception) {
            null
        }
    }
}
