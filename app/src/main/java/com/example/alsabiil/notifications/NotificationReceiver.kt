package com.example.alsabiil.notifications

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.alsabiil.data.SettingsManager
import com.example.alsabiil.utils.LocationService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private const val TAG = "NotificationReceiver"

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title") ?: "Prayer Time"
        val body = intent.getStringExtra("body") ?: "It is time for prayer"
        val channelId = intent.getStringExtra("channelId") ?: NotificationHelper.ADHAN_CHANNEL_ID
        val id = intent.getIntExtra("id", 100)

        Log.d(TAG, "Notification received: title=$title, id=$id")

        var fullScreenPendingIntent: PendingIntent? = null
        val isAdhan = channelId == NotificationHelper.ADHAN_CHANNEL_ID
        val isQiyam = id == 202

        if (isAdhan || isQiyam) {
            val alertIntent = Intent(context, PrayerAlertActivity::class.java).apply {
                putExtra("title", title)
                putExtra("body", body)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            fullScreenPendingIntent = PendingIntent.getActivity(
                context, id + 2000, alertIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

        val helper = NotificationHelper(context)
        helper.showNotification(id, title, body, channelId, fullScreenPendingIntent)

        // For Adhan & Qiyam: also directly launch the alert activity
        // fullScreenIntent only works when the phone is locked; this ensures
        // the dialog is shown even when the phone is unlocked.
        if (isAdhan || isQiyam) {
            val directAlertIntent = Intent(context, PrayerAlertActivity::class.java).apply {
                putExtra("title", title)
                putExtra("body", body)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            try {
                context.startActivity(directAlertIntent)
            } catch (e: Exception) {
                Log.e(TAG, "Could not launch PrayerAlertActivity directly: ${e.message}")
            }
        }

        // Use goAsync() to keep the receiver alive while coroutines run
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val settingsManager = SettingsManager(context)
                val settings = settingsManager.settingsFlow.first()

                // Sound Logic
                if (channelId == NotificationHelper.ADHAN_CHANNEL_ID && settings.adhanEnabled) {
                    NotificationHelper.playAdhanSound(context, settings.forceAdhanInSilent, settings.useSystemVolume)
                } else if (channelId == NotificationHelper.QIYAM_CHANNEL_ID && isQiyam) {
                    // Only play alarm sound for Qiyam (ID 202).
                    val soundEnabledByScheduler = intent.getBooleanExtra("soundEnabled", false)
                    if (soundEnabledByScheduler) {
                        NotificationHelper.playAlarmSound(context, settings.useSystemVolume)
                    }
                }

                // Re-schedule notifications for the next day
                val loc = LocationService.getCurrentLocation(context)
                    ?: LocationService.getCachedLocation(context)

                loc?.let {
                    val scheduler = NotificationScheduler(context)
                    scheduler.scheduleNotifications(settings, it.latitude, it.longitude)
                    Log.d(TAG, "Re-scheduled notifications for next day")
                } ?: Log.w(TAG, "Could not get location to reschedule")
            } catch (e: Exception) {
                Log.e(TAG, "Error in notification receiver: ${e.message}")
            } finally {
                pendingResult.finish()
            }
        }
    }
}

