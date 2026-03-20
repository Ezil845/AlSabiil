package al.sabil.notifications

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import al.sabil.data.SettingsManager
import al.sabil.utils.LocationService
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
        val latitude = intent.getDoubleExtra("latitude", 0.0)
        val longitude = intent.getDoubleExtra("longitude", 0.0)

        Log.d(TAG, "Notification received: title=$title, id=$id, lat=$latitude, lon=$longitude")

        var fullScreenPendingIntent: PendingIntent? = null
        val isAdhan = channelId == NotificationHelper.ADHAN_CHANNEL_ID
        val isQiyam = id == 202
        val soundEnabledByScheduler = intent.getBooleanExtra("soundEnabled", false)
        val shouldShowStopAction = isAdhan || (isQiyam && soundEnabledByScheduler)

        if (isAdhan || isQiyam) {
            val alertIntent = Intent(context, PrayerAlertActivity::class.java).apply {
                putExtra("title", title)
                putExtra("body", body)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            fullScreenPendingIntent = PendingIntent.getActivity(
                context, id + 2000, alertIntent,
                PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        }

        val helper = NotificationHelper(context)
        helper.showNotification(id, title, body, channelId, fullScreenPendingIntent, showStopAction = shouldShowStopAction)

        // Use goAsync() to keep the receiver alive while coroutines run
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val settingsManager = SettingsManager(context)
                val settings = settingsManager.settingsFlow.first()

                // Sound Logic
                if (channelId == NotificationHelper.ADHAN_CHANNEL_ID && settings.adhanEnabled) {
                    NotificationHelper.playAdhanSound(context, settings.forceAdhanInSilent, settings.useSystemVolume, settings.selectedAdhan)
                } else if (channelId == NotificationHelper.QIYAM_CHANNEL_ID && isQiyam) {
                    // Only play alarm sound for Qiyam (ID 202).
                    val soundEnabledByScheduler = intent.getBooleanExtra("soundEnabled", false)
                    if (soundEnabledByScheduler) {
                        NotificationHelper.playAlarmSound(context, settings.useSystemVolume)
                    }
                }

                // Re-schedule notifications for the next day
                val scheduler = NotificationScheduler(context)
                if (latitude != 0.0 && longitude != 0.0) {
                    scheduler.scheduleNotifications(settings, latitude, longitude)
                    Log.d(TAG, "Re-scheduled notifications using passed location")
                } else {
                    // Use cached location ignoring expiry — a slightly stale location
                    // is far better than missing all remaining prayer alarms
                    val loc = LocationService.getLastKnownLocationForScheduling(context)
                    if (loc != null) {
                        scheduler.scheduleNotifications(settings, loc.latitude, loc.longitude)
                        Log.d(TAG, "Re-scheduled notifications using last known location")
                    } else {
                        Log.e(TAG, "CRITICAL: No location available to reschedule alarms!")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in notification receiver: ${e.message}")
            } finally {
                pendingResult.finish()
            }
        }
    }
}

