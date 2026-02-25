package com.example.alsabiil.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.alsabiil.data.SettingsManager
import com.example.alsabiil.utils.LocationService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val settingsManager = SettingsManager(context)
                    val settings = settingsManager.settingsFlow.first()
                    val loc = LocationService.getCurrentLocation(context) 
                        ?: LocationService.getCachedLocation(context)
                    
                    loc?.let {
                        val scheduler = NotificationScheduler(context)
                        scheduler.scheduleNotifications(settings, it.latitude, it.longitude)
                    }
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
