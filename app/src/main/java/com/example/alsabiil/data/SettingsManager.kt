package com.example.alsabiil.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.alsabiil.model.Bookmark
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsManager(private val context: Context) {

    companion object {
        val FAJR_NOTIF = booleanPreferencesKey("notif_fajr")
        val SUNRISE_NOTIF = booleanPreferencesKey("notif_sunrise")
        val DHUHR_NOTIF = booleanPreferencesKey("notif_dhuhr")
        val ASR_NOTIF = booleanPreferencesKey("notif_asr")
        val MAGHRIB_NOTIF = booleanPreferencesKey("notif_maghrib")
        val ISHA_NOTIF = booleanPreferencesKey("notif_isha")

        val MORNING_ADHKAR = booleanPreferencesKey("notif_morning_adhkar")
        val EVENING_ADHKAR = booleanPreferencesKey("notif_evening_adhkar")
        val QIYAM_ADHKAR = booleanPreferencesKey("notif_qiyam_adhkar")

        val SELECTED_PALETTE = stringPreferencesKey("selected_palette")
        val CALCULATION_METHOD = stringPreferencesKey("calculation_method")
        val SELECTED_TAFSEER = stringPreferencesKey("selected_tafseer")
        val ADHAN_ENABLED = booleanPreferencesKey("adhan_enabled")
        val FORCE_ADHAN_SILENT = booleanPreferencesKey("force_adhan_silent")
        val USE_SYSTEM_VOLUME = booleanPreferencesKey("use_system_volume")
        val ADHKAR_SOUND_ENABLED = booleanPreferencesKey("adhkar_sound_enabled")
        val QIYAM_TIME = stringPreferencesKey("qiyam_time")
        val HIJRI_OFFSET = intPreferencesKey("hijri_offset")
        val SELECTED_ADHAN = stringPreferencesKey("selected_adhan")

        val LAST_READ_PAGE = intPreferencesKey("last_read_page")
        val FIRST_LAUNCH_COMPLETED = booleanPreferencesKey("first_launch_completed")

        // Additional bookmark keys for specific surahs/ayahs
        val BOOKMARKS_KEY = stringPreferencesKey("bookmarks")
    }

    val settingsFlow: Flow<UserSettings> = context.dataStore.data.map { preferences ->
        UserSettings(
            fajrNotif = preferences[FAJR_NOTIF] ?: true,
            sunriseNotif = preferences[SUNRISE_NOTIF] ?: false,
            dhuhrNotif = preferences[DHUHR_NOTIF] ?: true,
            asrNotif = preferences[ASR_NOTIF] ?: true,
            maghribNotif = preferences[MAGHRIB_NOTIF] ?: true,
            ishaNotif = preferences[ISHA_NOTIF] ?: true,
            morningAdhkar = preferences[MORNING_ADHKAR] ?: true,
            eveningAdhkar = preferences[EVENING_ADHKAR] ?: true,
            qiyamAdhkar = preferences[QIYAM_ADHKAR] ?: false,
            selectedPalette = preferences[SELECTED_PALETTE] ?: "emerald",
            calculationMethod = preferences[CALCULATION_METHOD] ?: "MWL",
            selectedTafseer = preferences[SELECTED_TAFSEER] ?: "saddi",
            adhanEnabled = preferences[ADHAN_ENABLED] ?: true,
            forceAdhanInSilent = preferences[FORCE_ADHAN_SILENT] ?: true,
            useSystemVolume = preferences[USE_SYSTEM_VOLUME] ?: false,
            adhkarSoundEnabled = preferences[ADHKAR_SOUND_ENABLED] ?: false,
            qiyamTime = preferences[QIYAM_TIME] ?: "DEFAULT",
            hijriOffset = preferences[HIJRI_OFFSET] ?: 0,
            selectedAdhan = preferences[SELECTED_ADHAN] ?: "adhan_ahmed_kourdi",
            lastReadPage = preferences[LAST_READ_PAGE] ?: 1,
            firstLaunchCompleted = preferences[FIRST_LAUNCH_COMPLETED] ?: false
        )
    }

    suspend fun updateSelectedAdhan(adhan: String) {
        context.dataStore.edit { settings ->
            settings[SELECTED_ADHAN] = adhan
        }
    }

    suspend fun updateHijriOffset(offset: Int) {
        context.dataStore.edit { settings ->
            settings[HIJRI_OFFSET] = offset
        }
    }

    suspend fun updateAdhkarSoundEnabled(enabled: Boolean) {
        context.dataStore.edit { settings ->
            settings[ADHKAR_SOUND_ENABLED] = enabled
        }
    }

    suspend fun updateQiyamTime(time: String) {
        context.dataStore.edit { settings ->
            settings[QIYAM_TIME] = time
        }
    }

    suspend fun updateAdhanEnabled(enabled: Boolean) {
        context.dataStore.edit { settings ->
            settings[ADHAN_ENABLED] = enabled
        }
    }

    suspend fun updateForceAdhanInSilent(enabled: Boolean) {
        context.dataStore.edit { settings ->
            settings[FORCE_ADHAN_SILENT] = enabled
        }
    }

    suspend fun updateUseSystemVolume(enabled: Boolean) {
        context.dataStore.edit { settings ->
            settings[USE_SYSTEM_VOLUME] = enabled
        }
    }

    suspend fun updatePrayerNotif(key: Preferences.Key<Boolean>, value: Boolean) {
        context.dataStore.edit { settings ->
            settings[key] = value
        }
    }

    suspend fun updateAdhkarNotif(key: Preferences.Key<Boolean>, value: Boolean) {
        context.dataStore.edit { settings ->
            settings[key] = value
        }
    }

    suspend fun updatePalette(palette: String) {
        context.dataStore.edit { settings ->
            settings[SELECTED_PALETTE] = palette
        }
    }

    suspend fun updateCalculationMethod(method: String) {
        context.dataStore.edit { settings ->
            settings[CALCULATION_METHOD] = method
        }
    }

    suspend fun updateTafseer(tafseer: String) {
        context.dataStore.edit { settings ->
            settings[SELECTED_TAFSEER] = tafseer
        }
    }

    suspend fun saveLastReadPage(page: Int) {
        context.dataStore.edit { settings ->
            settings[LAST_READ_PAGE] = page
        }
    }

    suspend fun updateFirstLaunchCompleted(completed: Boolean) {
        context.dataStore.edit { settings ->
            settings[FIRST_LAUNCH_COMPLETED] = completed
        }
    }

    // Function to save bookmarks for specific surahs/ayahs
    suspend fun saveBookmark(bookmark: Bookmark) {
        context.dataStore.edit { settings ->
            val bookmarksJson = settings[BOOKMARKS_KEY] ?: "[]"
            val currentBookmarks = try {
                Json.decodeFromString<List<Bookmark>>(bookmarksJson)
            } catch (e: Exception) {
                emptyList()
            }

            val updatedBookmarks = currentBookmarks.filter {
                !(it.surahNumber == bookmark.surahNumber && it.ayahNumber == bookmark.ayahNumber)
            } + listOf(bookmark)

            settings[BOOKMARKS_KEY] = Json.encodeToString(updatedBookmarks)
        }
    }

    suspend fun getBookmarks(): List<Bookmark> {
        val preferences = context.dataStore.data.first()
        val bookmarksJson = preferences[BOOKMARKS_KEY] ?: "[]"
        return try {
            Json.decodeFromString<List<Bookmark>>(bookmarksJson)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun removeBookmark(surahNumber: Int, ayahNumber: Int) {
        context.dataStore.edit { settings ->
            val bookmarksJson = settings[BOOKMARKS_KEY] ?: "[]"
            val currentBookmarks = try {
                Json.decodeFromString<List<Bookmark>>(bookmarksJson)
            } catch (e: Exception) {
                emptyList()
            }

            val updatedBookmarks = currentBookmarks.filter {
                !(it.surahNumber == surahNumber && it.ayahNumber == ayahNumber)
            }

            settings[BOOKMARKS_KEY] = Json.encodeToString(updatedBookmarks)
        }
    }
}

data class UserSettings(
    val fajrNotif: Boolean,
    val sunriseNotif: Boolean,
    val dhuhrNotif: Boolean,
    val asrNotif: Boolean,
    val maghribNotif: Boolean,
    val ishaNotif: Boolean,
    val morningAdhkar: Boolean,
    val eveningAdhkar: Boolean,
    val qiyamAdhkar: Boolean,
    val selectedPalette: String,
    val calculationMethod: String,
    val selectedTafseer: String,
    val adhanEnabled: Boolean,
    val forceAdhanInSilent: Boolean,
    val useSystemVolume: Boolean,
    val adhkarSoundEnabled: Boolean,
    val qiyamTime: String,
    val hijriOffset: Int,
    val selectedAdhan: String,
    val lastReadPage: Int,
    val firstLaunchCompleted: Boolean
)
