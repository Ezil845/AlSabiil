package al.sabil.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class StopSoundReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        NotificationHelper.stopAdhanSound()
        
        // Notify the PrayerAlertActivity to close
        context.sendBroadcast(Intent("al.sabil.ACTION_STOP_SOUND"))
        
        // Also cancel the notification if id is provided
        val id = intent.getIntExtra("notification_id", -1)
        if (id != -1) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            notificationManager.cancel(id)
        }
    }
}
