package al.sabil.repository

import android.content.Context
import al.sabil.data.AppDatabase
import al.sabil.data.entity.AzkarEntity
import al.sabil.model.AzkarItem

class AzkarRepository(private val context: Context) {
    private val database = AppDatabase.getInstance(context)
    private val azkarDao = database.azkarDao()

    // In-memory cache: reused on subsequent calls
    private val cachedAzkar = mutableMapOf<String, List<AzkarItem>>()
    
    companion object {
        const val MORNING = "morning"
        const val EVENING = "evening"
        const val AFTER_PRAYER = "after_prayer"
    }

    private fun AzkarEntity.toDomain(): AzkarItem = AzkarItem(
        zekr = zekr,
        repeat = count,
        bless = description ?: ""
    )

    suspend fun getCategories(): List<String> {
        val fixed = listOf(MORNING, EVENING, AFTER_PRAYER)
        val allCategories = azkarDao.getAllCategories()
        
        // Ensure fixed ones are at the top if they exist in DB
        val dynamic = allCategories.filter { it !in fixed }
        return fixed.filter { it in allCategories } + dynamic
    }

    suspend fun getAzkarByCategory(category: String): List<AzkarItem> {
        cachedAzkar[category]?.let { return it }

        val items = azkarDao.getAzkarByCategory(category).map { it.toDomain() }
        if (items.isNotEmpty()) {
            cachedAzkar[category] = items
        }
        return items
    }
}
