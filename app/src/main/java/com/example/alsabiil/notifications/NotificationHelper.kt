package com.example.alsabiil.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.alsabiil.MainActivity
import com.example.alsabiil.R

private const val TAG = "NotificationHelper"

class NotificationHelper(private val context: Context) {

    companion object {
        const val ADHAN_CHANNEL_ID = "adhan_channel_v2"
        const val ADHKAR_CHANNEL_ID = "adhkar_channel"
        const val QIYAM_CHANNEL_ID = "qiyam_channel"
        
        private var mediaPlayer: MediaPlayer? = null

        fun stopAdhanSound() {
            try {
                mediaPlayer?.stop()
                mediaPlayer?.release()
                mediaPlayer = null
            } catch (e: Exception) {
                mediaPlayer = null
            }
        }

        fun playAdhanSound(context: Context, forceInSilent: Boolean, useSystemVolume: Boolean, selectedAdhan: String = "adhan_ahmed_kourdi") {
            try {
                stopAdhanSound()
                val resId = context.resources.getIdentifier(selectedAdhan, "raw", context.packageName)
                val adhanSoundUri = if (resId != 0) {
                    Uri.parse("android.resource://${context.packageName}/$resId")
                } else {
                    Uri.parse("android.resource://${context.packageName}/${R.raw.adhan_ahmed_kourdi}")
                }
                
                val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                val streamType = if (forceInSilent) AudioManager.STREAM_ALARM else AudioManager.STREAM_NOTIFICATION
                val usageType = if (forceInSilent) AudioAttributes.USAGE_ALARM else AudioAttributes.USAGE_NOTIFICATION

                if (!useSystemVolume) {
                    val maxVolume = audioManager.getStreamMaxVolume(streamType)
                    val targetVolume = (maxVolume * 0.75).toInt()
                    audioManager.setStreamVolume(streamType, targetVolume, 0)
                }

                mediaPlayer = MediaPlayer().apply {
                    setDataSource(context, adhanSoundUri)
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(usageType)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .setLegacyStreamType(streamType)
                            .build()
                    )
                    prepare()
                    start()
                }
            } catch (e: Exception) {
                Log.e("NotificationHelper", "Error playing adhan sound: ${e.message}")
            }
        }

        fun playAlarmSound(context: Context, useSystemVolume: Boolean) {
            try {
                stopAdhanSound()
                val alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                    ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                
                val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
                
                if (!useSystemVolume) {
                    val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)
                    val targetVolume = (maxVolume * 0.75).toInt()
                    audioManager.setStreamVolume(AudioManager.STREAM_ALARM, targetVolume, 0)
                }

                mediaPlayer = MediaPlayer().apply {
                    setDataSource(context, alarmUri)
                    setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .setLegacyStreamType(AudioManager.STREAM_ALARM)
                            .build()
                    )
                    prepare()
                    start()
                }
            } catch (e: Exception) {
                Log.e("NotificationHelper", "Error playing alarm sound: ${e.message}")
            }
        }
    }

    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Adhan Channel (High Importance + Silent because MediaPlayer handles sound)
            val adhanChannel = NotificationChannel(
                ADHAN_CHANNEL_ID,
                "Adhan Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notification for prayer times"
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                setSound(null, null)
            }
            
            // Qiyam Channel (High Importance + Silent because MediaPlayer handles sound)
            val qiyamChannel = NotificationChannel(
                QIYAM_CHANNEL_ID,
                "Qiyam Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders for Qiyam"
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
                setSound(null, null)
            }

            // Adhkar Channel (High Importance + Default Sound)
            val adhkarChannel = NotificationChannel(
                ADHKAR_CHANNEL_ID,
                "Adhkar Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Reminders for Adhkar sessions"
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }

            notificationManager.createNotificationChannel(adhanChannel)
            notificationManager.createNotificationChannel(qiyamChannel)
            notificationManager.createNotificationChannel(adhkarChannel)
        }
    }

    fun showNotification(id: Int, title: String, body: String, channelId: String, fullScreenIntent: PendingIntent? = null, showStopAction: Boolean = true) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_app_icon)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(pendingIntent)
            .setFullScreenIntent(fullScreenIntent, true)
            .setAutoCancel(!showStopAction) // Auto-cancel if it's just a regular notification
            .setOngoing(showStopAction) // Only ongoing if it has a sound that needs stopping

        if (showStopAction) {
            // Intent to stop the sound
            val stopIntent = Intent(context, StopSoundReceiver::class.java).apply {
                putExtra("notification_id", id)
            }
            val stopPendingIntent = PendingIntent.getBroadcast(
                context, id + 1000, stopIntent,
                PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
            builder.addAction(android.R.drawable.ic_menu_close_clear_cancel, context.getString(R.string.stop_sound), stopPendingIntent)
        }

        notificationManager.notify(id, builder.build())
    }
}
