package al.sabil.data

import android.content.Context
import android.util.Log
import al.sabil.data.entity.AyahEntity
import al.sabil.data.entity.AzkarEntity
import al.sabil.model.AdhkarV2Category
import al.sabil.model.AzkarCategory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream

object DatabaseSeeder {
    private const val TAG = "DatabaseSeeder"
    private const val PREFS_NAME = "db_prefs"
    private const val KEY_IS_SEEDED = "is_seeded_v2" 

    @OptIn(ExperimentalSerializationApi::class)
    fun seedDatabase(context: Context, database: AppDatabase) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (prefs.getBoolean(KEY_IS_SEEDED, false)) {
            Log.d(TAG, "Database already seeded")
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d(TAG, "Starting database seeding...")
                val json = Json { ignoreUnknownKeys = true; isLenient = true }

                // 1. Seed Quran
                val ayahStream = context.assets.open("data/hafs_smart_v8.json")
                val ayahs = json.decodeFromStream<List<AyahEntity>>(ayahStream)
                database.ayahDao().insertAll(ayahs)
                Log.d(TAG, "Seeded ${ayahs.size} ayahs")

                // 2. Seed Azkar
                val azkarList = mutableListOf<AzkarEntity>()

                // Morning
                seedOldAzkar(context, json, "data/azkar_sabah.json", "morning", azkarList)
                // Evening
                seedOldAzkar(context, json, "data/azkar_massa.json", "evening", azkarList)
                // After Prayer
                seedOldAzkar(context, json, "data/PostPrayer_azkar.json", "after_prayer", azkarList)

                // V2 Azkar
                context.assets.open("data/adhkar_v2.json").use { stream ->
                    val categories = json.decodeFromStream<List<AdhkarV2Category>>(stream)
                    categories.forEach { category ->
                        category.array.forEach { item ->
                            azkarList.add(
                                AzkarEntity(
                                    category = category.category,
                                    zekr = item.text,
                                    count = item.count,
                                    description = null,
                                    reference = null
                                )
                            )
                        }
                    }
                }

                database.azkarDao().insertAll(azkarList)
                Log.d(TAG, "Seeded ${azkarList.size} azkar items")

                prefs.edit().putBoolean(KEY_IS_SEEDED, true).apply()
                Log.d(TAG, "Database seeding completed successfully")

            } catch (e: Exception) {
                Log.e(TAG, "Error seeding database", e)
                e.printStackTrace()
            }
        }
    }

    @OptIn(ExperimentalSerializationApi::class)
    private fun seedOldAzkar(context: Context, json: Json, fileName: String, categoryName: String, list: MutableList<AzkarEntity>) {
        try {
            context.assets.open(fileName).use { stream ->
                val categoryData = json.decodeFromStream<AzkarCategory>(stream)
                categoryData.content.forEach { item ->
                    list.add(
                        AzkarEntity(
                            category = categoryName,
                            zekr = item.zekr,
                            count = item.repeat,
                            description = item.bless,
                            reference = null
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error reading $fileName", e)
        }
    }
}
