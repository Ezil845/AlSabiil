package com.example.alsabiil.viewmodel

import android.app.Application
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.alsabiil.data.SettingsManager
import com.example.alsabiil.data.UserSettings
import com.example.alsabiil.model.Bookmark
import com.example.alsabiil.notifications.NotificationScheduler
import com.example.alsabiil.notifications.NotificationHelper
import com.example.alsabiil.utils.LocationService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    private val settingsManager = SettingsManager(application)
    private val scheduler = NotificationScheduler(application)
    private val notificationHelper = NotificationHelper(application)

    val settings: StateFlow<UserSettings?> = settingsManager.settingsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    private val _bookmarks = MutableStateFlow<List<Bookmark>>(emptyList())
    val bookmarks: StateFlow<List<Bookmark>> = _bookmarks

    init {
        loadBookmarks()
        // Schedule notifications on app launch
        scheduleInitialNotifications()
    }

    fun playTestAdhan() {
        viewModelScope.launch {
            val currentSettings = settingsManager.settingsFlow.first()
            NotificationHelper.playAdhanSound(getApplication(), currentSettings.forceAdhanInSilent, currentSettings.useSystemVolume)
        }
    }

    fun playTestAlarm() {
        viewModelScope.launch {
            val currentSettings = settingsManager.settingsFlow.first()
            NotificationHelper.playAlarmSound(getApplication(), currentSettings.useSystemVolume)
        }
    }

    fun testQiyamAlert() {
        val intent = android.content.Intent(getApplication(), com.example.alsabiil.notifications.PrayerAlertActivity::class.java).apply {
            flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
        }
        getApplication<android.app.Application>().startActivity(intent)
        
        viewModelScope.launch {
            val currentSettings = settingsManager.settingsFlow.first()
            NotificationHelper.playAlarmSound(getApplication(), currentSettings.useSystemVolume)
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

    private fun loadBookmarks() {
        viewModelScope.launch {
            _bookmarks.value = settingsManager.getBookmarks()
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

    fun updateHijriOffset(offset: Int) {
        viewModelScope.launch {
            settingsManager.updateHijriOffset(offset)
        }
    }

    fun saveLastReadPage(page: Int) {
        viewModelScope.launch {
            settingsManager.saveLastReadPage(page)
        }
    }

    fun saveBookmark(bookmark: Bookmark) {
        viewModelScope.launch {
            settingsManager.saveBookmark(bookmark)
            loadBookmarks() // Refresh the bookmarks after saving
        }
    }

    fun removeBookmark(surahNumber: Int, ayahNumber: Int) {
        viewModelScope.launch {
            settingsManager.removeBookmark(surahNumber, ayahNumber)
            loadBookmarks() // Refresh the bookmarks after removing
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
