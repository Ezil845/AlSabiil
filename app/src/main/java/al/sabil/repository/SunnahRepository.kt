package al.sabil.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import al.sabil.R
import al.sabil.data.dataStore
import al.sabil.model.SunnahCategory
import al.sabil.model.SunnahItem
import al.sabil.model.SunnahStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.*

import java.time.chrono.HijrahDate
import java.time.temporal.ChronoField
import java.time.temporal.ChronoUnit

class SunnahRepository(private val context: Context) {

    private val SUNNAH_STATUS_KEY = stringPreferencesKey("sunnah_status")

    fun getSunnahItems(hijriOffset: Int = 0): List<SunnahItem> {
        val now = Calendar.getInstance()
        val isFriday = now.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY
        
        val hijriDate = try {
            HijrahDate.now().plus(hijriOffset.toLong(), ChronoUnit.DAYS)
        } catch (e: Exception) {
            null
        }
        val hijriDay = hijriDate?.get(ChronoField.DAY_OF_MONTH)
        val isWhiteDay = hijriDay in listOf(13, 14, 15)

        val items = mutableListOf(
            SunnahItem("siwak", R.string.sunnah_siwak_title, R.string.sunnah_siwak_desc, SunnahCategory.DAILY),
            SunnahItem("duha", R.string.sunnah_duha_title, R.string.sunnah_duha_desc, SunnahCategory.DAILY),
            SunnahItem("adhkar_after_prayer", R.string.sunnah_adhkar_after_prayer_title, R.string.sunnah_adhkar_after_prayer_desc, SunnahCategory.DAILY),
            SunnahItem("witr", R.string.sunnah_witr_title, R.string.sunnah_witr_desc, SunnahCategory.DAILY)
        )

        if (isFriday) {
            items.add(SunnahItem("kahf", R.string.sunnah_kahf_title, R.string.sunnah_kahf_desc, SunnahCategory.WEEKLY))
        }

        if (isWhiteDay) {
            items.add(SunnahItem("white_days", R.string.sunnah_white_days_title, R.string.sunnah_white_days_desc, SunnahCategory.MONTHLY))
        }

        return items
    }

    fun getSunnahStatusFlow(): Flow<Map<String, Long>> {
        return context.dataStore.data.map { preferences ->
            val json = preferences[SUNNAH_STATUS_KEY] ?: "{}"
            try {
                Json.decodeFromString<Map<String, Long>>(json)
            } catch (e: Exception) {
                emptyMap()
            }
        }
    }

    suspend fun toggleSunnahItem(itemId: String, isChecked: Boolean) {
        context.dataStore.edit { preferences ->
            val json = preferences[SUNNAH_STATUS_KEY] ?: "{}"
            val currentMap = try {
                Json.decodeFromString<Map<String, Long>>(json).toMutableMap()
            } catch (e: Exception) {
                mutableMapOf()
            }

            if (isChecked) {
                currentMap[itemId] = System.currentTimeMillis()
            } else {
                currentMap.remove(itemId)
            }

            preferences[SUNNAH_STATUS_KEY] = Json.encodeToString(currentMap)
        }
    }

    fun isTaskValidForToday(lastUpdatedMillis: Long, category: SunnahCategory): Boolean {
        val lastUpdate = Calendar.getInstance().apply { timeInMillis = lastUpdatedMillis }
        val now = Calendar.getInstance()

        return when (category) {
            SunnahCategory.DAILY -> {
                lastUpdate.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
                lastUpdate.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR)
            }
            SunnahCategory.WEEKLY -> {
                lastUpdate.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
                lastUpdate.get(Calendar.WEEK_OF_YEAR) == now.get(Calendar.WEEK_OF_YEAR)
            }
            SunnahCategory.MONTHLY -> {
                lastUpdate.get(Calendar.YEAR) == now.get(Calendar.YEAR) &&
                lastUpdate.get(Calendar.MONTH) == now.get(Calendar.MONTH)
            }
        }
    }
}
