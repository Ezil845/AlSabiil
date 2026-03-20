package al.sabil.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

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
        val WIDGET_ENABLED = booleanPreferencesKey("widget_enabled")
        val QURAN_DARK_MODE = booleanPreferencesKey("quran_dark_mode")

        val LAST_READ_PAGE = intPreferencesKey("last_read_page")
        val FIRST_LAUNCH_COMPLETED = booleanPreferencesKey("first_launch_completed")

        // Khatmah Planner
        val KHATMAH_TARGET_DAYS = intPreferencesKey("khatmah_target_days")
        val KHATMAH_START_MILLIS = longPreferencesKey("khatmah_start_millis")
        val KHATMAH_CURRENT_PAGE = intPreferencesKey("khatmah_current_page")
        val KHATMAH_ACTIVE = booleanPreferencesKey("khatmah_active")
        val KHATMAH_REMINDER = booleanPreferencesKey("khatmah_reminder")


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
            widgetEnabled = preferences[WIDGET_ENABLED] ?: true,
            quranDarkMode = preferences[QURAN_DARK_MODE] ?: false,
            lastReadPage = preferences[LAST_READ_PAGE] ?: 1,
            firstLaunchCompleted = preferences[FIRST_LAUNCH_COMPLETED] ?: false,
            khatmahTargetDays = preferences[KHATMAH_TARGET_DAYS] ?: 30,
            khatmahStartMillis = preferences[KHATMAH_START_MILLIS] ?: 0L,
            khatmahCurrentPage = preferences[KHATMAH_CURRENT_PAGE] ?: 1,
            khatmahActive = preferences[KHATMAH_ACTIVE] ?: false,
            khatmahReminderEnabled = preferences[KHATMAH_REMINDER] ?: false
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

    suspend fun updateWidgetEnabled(enabled: Boolean) {
        context.dataStore.edit { settings ->
            settings[WIDGET_ENABLED] = enabled
        }
    }

    suspend fun updateQuranDarkMode(enabled: Boolean) {
        context.dataStore.edit { settings ->
            settings[QURAN_DARK_MODE] = enabled
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

    suspend fun startKhatmah(targetDays: Int, startMillis: Long, currentPage: Int) {
        context.dataStore.edit { settings ->
            settings[KHATMAH_ACTIVE] = true
            settings[KHATMAH_TARGET_DAYS] = targetDays
            settings[KHATMAH_START_MILLIS] = startMillis
            settings[KHATMAH_CURRENT_PAGE] = currentPage
        }
    }

    suspend fun stopKhatmah() {
        context.dataStore.edit { settings ->
            settings[KHATMAH_ACTIVE] = false
        }
    }

    suspend fun updateKhatmahReminder(enabled: Boolean) {
        context.dataStore.edit { settings ->
            settings[KHATMAH_REMINDER] = enabled
        }
    }

    suspend fun updateKhatmahCurrentPage(page: Int) {
        context.dataStore.edit { settings ->
            settings[KHATMAH_CURRENT_PAGE] = page
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
    val widgetEnabled: Boolean,
    val quranDarkMode: Boolean,
    val lastReadPage: Int,
    val firstLaunchCompleted: Boolean,
    val khatmahTargetDays: Int,
    val khatmahStartMillis: Long,
    val khatmahCurrentPage: Int,
    val khatmahActive: Boolean,
    val khatmahReminderEnabled: Boolean
)
