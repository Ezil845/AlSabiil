package al.sabil.viewmodel

import android.app.Application
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import al.sabil.data.SettingsManager
import al.sabil.data.AppDatabase
import al.sabil.data.BookmarkEntity
import al.sabil.data.UserSettings
import al.sabil.model.Bookmark
import al.sabil.model.toBookmark
import al.sabil.model.toEntity
import al.sabil.notifications.NotificationScheduler
import al.sabil.notifications.NotificationHelper
import al.sabil.utils.LocationService
import al.sabil.widget.PrayerWidget
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val settingsManager = SettingsManager(application)
    private val scheduler = NotificationScheduler(application)


    val settings: StateFlow<UserSettings?> = settingsManager.settingsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    private val bookmarkDao = AppDatabase.getInstance(application).bookmarkDao()

    val bookmarks: StateFlow<List<Bookmark>> = bookmarkDao.getAllBookmarks()
        .map { entities -> entities.map { it.toBookmark() } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    init {
        // Schedule notifications on app launch
        scheduleInitialNotifications()
    }

    fun playTestAdhan() {
        viewModelScope.launch {
            val currentSettings = settingsManager.settingsFlow.first()
            NotificationHelper.playAdhanSound(getApplication(), currentSettings.forceAdhanInSilent, currentSettings.useSystemVolume, currentSettings.selectedAdhan)
        }
    }


    fun stopAdhan() {
        NotificationHelper.stopAdhanSound()
    }


    /**
     * Schedule notifications when the app starts up, ensuring alarms are set
     * even if the user hasn't toggled any settings in this session.
     */
    private fun scheduleInitialNotifications() {
        viewModelScope.launch {
            // Wait for settings to be available
            val currentSettings = settingsManager.settingsFlow.first()
            val loc = LocationService.getCurrentLocation(getApplication())
                ?: LocationService.getCachedLocation(getApplication())

            loc?.let {
                // Cache for future use
                LocationService.cacheLocation(getApplication(), it)
                scheduler.scheduleNotifications(currentSettings, it.latitude, it.longitude)
            }
        }
    }



    fun togglePrayerNotif(key: Preferences.Key<Boolean>, value: Boolean) {
        viewModelScope.launch {
            settingsManager.updatePrayerNotif(key, value)
            rescheduleWithUpdatedSettings()
        }
    }

    fun toggleAdhkarNotif(key: Preferences.Key<Boolean>, value: Boolean) {
        viewModelScope.launch {
            settingsManager.updateAdhkarNotif(key, value)
            rescheduleWithUpdatedSettings()
        }
    }

    fun updatePalette(palette: String) {
        viewModelScope.launch {
            settingsManager.updatePalette(palette)
        }
    }

    fun updateCalculationMethod(method: String) {
        viewModelScope.launch {
            settingsManager.updateCalculationMethod(method)
            rescheduleWithUpdatedSettings()
        }
    }

    fun updateTafseer(tafseer: String) {
        viewModelScope.launch {
            settingsManager.updateTafseer(tafseer)
        }
    }

    fun toggleAdhanEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsManager.updateAdhanEnabled(enabled)
        }
    }

    fun toggleForceAdhanInSilent(enabled: Boolean) {
        viewModelScope.launch {
            settingsManager.updateForceAdhanInSilent(enabled)
        }
    }

    fun toggleUseSystemVolume(enabled: Boolean) {
        viewModelScope.launch {
            settingsManager.updateUseSystemVolume(enabled)
        }
    }

    fun toggleAdhkarSoundEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsManager.updateAdhkarSoundEnabled(enabled)
            rescheduleWithUpdatedSettings()
        }
    }

    fun updateQiyamTime(time: String) {
        viewModelScope.launch {
            settingsManager.updateQiyamTime(time)
            rescheduleWithUpdatedSettings()
        }
    }

    fun updateSelectedAdhan(adhan: String) {
        viewModelScope.launch {
            settingsManager.updateSelectedAdhan(adhan)
        }
    }

    fun updateWidgetEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsManager.updateWidgetEnabled(enabled)
            // Update the widget UI immediately
            try {
                PrayerWidget().updateAll(getApplication())
            } catch (e: Exception) {
                // Handle possible Glance initialization issues
            }
        }
    }

    fun updateQuranDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            settingsManager.updateQuranDarkMode(enabled)
        }
    }



    fun updateHijriOffset(offset: Int) {
        viewModelScope.launch {
            settingsManager.updateHijriOffset(offset)
            // Refresh widget if offset changes
            try {
                PrayerWidget().updateAll(getApplication())
            } catch (e: Exception) { }
        }
    }

    fun saveLastReadPage(page: Int) {
        viewModelScope.launch {
            settingsManager.saveLastReadPage(page)
        }
    }

    fun updateFirstLaunchCompleted() {
        viewModelScope.launch {
            settingsManager.updateFirstLaunchCompleted(true)
        }
    }

    fun startKhatmah(targetDays: Int, startMillis: Long, currentPage: Int) {
        viewModelScope.launch {
            settingsManager.startKhatmah(targetDays, startMillis, currentPage)
        }
    }

    fun stopKhatmah() {
        viewModelScope.launch {
            settingsManager.stopKhatmah()
        }
    }

    fun updateKhatmahCurrentPage(page: Int) {
        viewModelScope.launch {
            settingsManager.updateKhatmahCurrentPage(page)
        }
    }

    fun toggleKhatmahReminder(enabled: Boolean) {
        viewModelScope.launch {
            settingsManager.updateKhatmahReminder(enabled)
            rescheduleWithUpdatedSettings()
        }
    }

    fun saveBookmark(bookmark: Bookmark) {
        viewModelScope.launch {
            bookmarkDao.insertBookmark(bookmark.toEntity())
        }
    }

    fun removeBookmark(surahNumber: Int, ayahNumber: Int) {
        viewModelScope.launch {
            bookmarkDao.deleteBookmark(surahNumber, ayahNumber)
        }
    }

    fun updateQuranFontSizeMultiplier(multiplier: Float) {
        viewModelScope.launch {
            settingsManager.updateQuranFontSizeMultiplier(multiplier)
        }
    }

    private fun rescheduleWithUpdatedSettings() {
        viewModelScope.launch {
            val currentSettings = settingsManager.settingsFlow.first()
            val loc = LocationService.getCurrentLocation(getApplication())
                ?: LocationService.getCachedLocation(getApplication())

            loc?.let {
                scheduler.scheduleNotifications(currentSettings, it.latitude, it.longitude)
            }
        }
    }
}
