package com.example.alsabiil.repository

import android.content.Context
import com.example.alsabiil.model.AdhkarV2Category
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
        const val SLEEPING = "sleeping"
        const val WAKING_UP = "waking_up"
    }

    fun getCategories(): List<String> {
        val essentialCategories = listOf(
            "أذكار النوم",
            "أذكار الاستيقاظ من النوم",
            "الذكر عند دخول المنزل",
            "الذكر عند الخروج من المنزل",
            "دعاء دخول المسجد",
            "دعاء الخروج من المسجد",
            "الدعاء قبل الطعام",
            "الدعاء عند الفراغ من الطعام",
            "الدعاء إذا نزل المطر",
            "الاستغفار و التوبة",
            "دعاء السفر",
            "دعاء صلاة الاستخارة"
        )
        
        // Return fixed ones + filtered dynamic ones
        val fixed = listOf(MORNING, EVENING, AFTER_PRAYER)
        return fixed + essentialCategories
    }

    fun getAzkarByCategory(category: String): List<AzkarItem> {
        // Return cached data if available
        cachedAzkar[category]?.let { return it }

        return when (category) {
            MORNING -> loadOldStyleAzkar("data/azkar_sabah.json", category)
            EVENING -> loadOldStyleAzkar("data/azkar_massa.json", category)
            AFTER_PRAYER -> loadOldStyleAzkar("data/PostPrayer_azkar.json", category)
            else -> loadV2Azkar(category, category)
        }
    }

    private fun loadOldStyleAzkar(fileName: String, cacheKey: String): List<AzkarItem> {
        return try {
            context.assets.open(fileName).use { inputStream ->
                val categoryData = json.decodeFromStream<AzkarCategory>(inputStream)
                categoryData.content.also { cachedAzkar[cacheKey] = it }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun loadV2Azkar(v2CategoryName: String, cacheKey: String): List<AzkarItem> {
        return try {
            context.assets.open("data/adhkar_v2.json").use { inputStream ->
                val allCategories = json.decodeFromStream<List<AdhkarV2Category>>(inputStream)
                val target = allCategories.find { it.category == v2CategoryName }
                val items = target?.array?.map { 
                    AzkarItem(
                        zekr = it.text,
                        repeat = it.count,
                        bless = ""
                    )
                } ?: emptyList()
                items.also { cachedAzkar[cacheKey] = it }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
