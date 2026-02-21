package com.example.alsabiil.repository

import android.content.Context
import com.example.alsabiil.model.AzkarCategory
import com.example.alsabiil.model.AzkarItem
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.*

@OptIn(ExperimentalSerializationApi::class)
class AzkarRepository(private val context: Context) {
    private val json = Json { 
        ignoreUnknownKeys = true
        coerceInputValues = true
        isLenient = true
    }

    // In-memory cache: parsed once per category, reused on subsequent calls
    private val cachedAzkar = mutableMapOf<String, List<AzkarItem>>()
    
    companion object {
        const val MORNING = "morning"
        const val EVENING = "evening"
        const val AFTER_PRAYER = "after_prayer"
    }

    fun getCategories(): List<String> {
        return listOf(MORNING, EVENING, AFTER_PRAYER)
    }

    fun getAzkarByCategory(category: String): List<AzkarItem> {
        // Return cached data if available
        cachedAzkar[category]?.let { return it }

        val fileName = when (category) {
            MORNING -> "data/azkar_sabah.json"
            EVENING -> "data/azkar_massa.json"
            AFTER_PRAYER -> "data/PostPrayer_azkar.json"
            else -> return emptyList()
        }
        
        return try {
            context.assets.open(fileName).use { inputStream ->
                val categoryData = json.decodeFromStream<AzkarCategory>(inputStream)
                categoryData.content.also { cachedAzkar[category] = it }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
