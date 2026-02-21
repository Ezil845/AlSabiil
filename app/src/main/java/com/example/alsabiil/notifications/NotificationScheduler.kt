package com.example.alsabiil.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.alsabiil.data.UserSettings
import com.example.alsabiil.utils.PrayerTimeCalculator
import com.example.alsabiil.R
import java.util.*

private const val TAG = "NotificationScheduler"

class NotificationScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleNotifications(settings: UserSettings, latitude: Double, longitude: Double) {
        Log.d(TAG, "scheduleNotifications called. lat=$latitude, lon=$longitude")
        
        val calcMethod = when (settings.calculationMethod) {
            "ISNA" -> com.example.alsabiil.utils.CalculationMethod.ISNA
            "MAKKAH" -> com.example.alsabiil.utils.CalculationMethod.MAKKAH
            "EGYPT" -> com.example.alsabiil.utils.CalculationMethod.EGYPT
            "KARACHI" -> com.example.alsabiil.utils.CalculationMethod.KARACHI
            else -> com.example.alsabiil.utils.CalculationMethod.MWL
        }
        val calculator = PrayerTimeCalculator(latitude, longitude, method = calcMethod)
        val prayerTimes = calculator.calculateDetailedPrayerTimes(latitude, longitude)
        
        Log.d(TAG, "Prayer times: Fajr=${prayerTimes.fajr}, Dhuhr=${prayerTimes.dhuhr}, Asr=${prayerTimes.asr}, Maghrib=${prayerTimes.maghrib}, Isha=${prayerTimes.isha}")

        // 1. Prayer Notifications
        schedulePrayerAlarm(context.getString(R.string.fajr), prayerTimes.fajr, settings.fajrNotif, 100)
        schedulePrayerAlarm(context.getString(R.string.sunrise), prayerTimes.sunrise, settings.sunriseNotif, 101)
        schedulePrayerAlarm(context.getString(R.string.dhuhr), prayerTimes.dhuhr, settings.dhuhrNotif, 102)
        schedulePrayerAlarm(context.getString(R.string.asr), prayerTimes.asr, settings.asrNotif, 103)
        schedulePrayerAlarm(context.getString(R.string.maghrib), prayerTimes.maghrib, settings.maghribNotif, 104)
        schedulePrayerAlarm(context.getString(R.string.isha), prayerTimes.isha, settings.ishaNotif, 105)

        // 2. Adhkar Notifications (relative to prayers)
        // Morning: Fajr + 30 mins
        scheduleAdhkarAlarm(context.getString(R.string.morning_azkar), prayerTimes.fajr, 30, settings.morningAdhkar, settings.adhkarSoundEnabled, 200)
        // Evening: Asr + 15 mins
        scheduleAdhkarAlarm(context.getString(R.string.evening_azkar), prayerTimes.asr, 15, settings.eveningAdhkar, settings.adhkarSoundEnabled, 201)
        // Qiyam: Custom user time or default relative to Fajr
        scheduleQiyamAlarm(settings.qiyamTime, prayerTimes.fajr, settings.qiyamAdhkar, settings.adhkarSoundEnabled, 202)
    }

    private fun schedulePrayerAlarm(name: String, timeStr: String, enabled: Boolean, id: Int) {
        if (!enabled) {
            cancelAlarm(id)
            return
        }
        
        if (timeStr == "--:--") {
            Log.w(TAG, "Skipping $name alarm — invalid time")
            return
        }

        val calendar = getCalendarForTime(timeStr)

        // If the time has already passed today, schedule for tomorrow
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        
        Log.d(TAG, "Scheduling $name at ${calendar.time}, id=$id")

        val body = if (id == 100) { // Fajr ID
            "${context.getString(R.string.notification_prayer_body, name)} - ${context.getString(R.string.fajr_special)}"
        } else {
            context.getString(R.string.notification_prayer_body, name)
        }

        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("title", context.getString(R.string.notification_prayer_title, name))
            putExtra("body", body)
            putExtra("channelId", NotificationHelper.ADHAN_CHANNEL_ID)
            putExtra("id", id)
            addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context, id, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        scheduleAlarmCompat(calendar.timeInMillis, pendingIntent)
    }

    private fun scheduleAdhkarAlarm(name: String, relativeTo: String, offsetMinutes: Int, enabled: Boolean, soundEnabled: Boolean, id: Int) {
        if (!enabled) {
            cancelAlarm(id)
            return
        }

        var calendar = getCalendarForTime(relativeTo)
        calendar.add(Calendar.MINUTE, offsetMinutes)

        // If the time has already passed today, schedule for tomorrow
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("title", name)
            putExtra("body", context.getString(R.string.notification_adhkar_body))
            putExtra("channelId", NotificationHelper.ADHKAR_CHANNEL_ID)
            putExtra("id", id)
            putExtra("soundEnabled", soundEnabled)
            addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context, id, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        scheduleAlarmCompat(calendar.timeInMillis, pendingIntent)
    }

    private fun scheduleQiyamAlarm(qiyamTimeSetting: String, fajrTomorrowStr: String, enabled: Boolean, soundEnabled: Boolean, id: Int) {
        if (!enabled) {
            cancelAlarm(id)
            return
        }

        val calendar = if (qiyamTimeSetting == "DEFAULT") {
            // Default: 1 hour before Fajr tomorrow
            getCalendarForTime(fajrTomorrowStr).apply {
                add(Calendar.DAY_OF_YEAR, 1) // Tomorrow
                add(Calendar.HOUR_OF_DAY, -1) // 60 mins before
            }
        } else {
            // User set specific time
            getCalendarForTime(qiyamTimeSetting)
        }

        // Final check: if the calculated time (even after adding tomorrow for DEFAULT) 
        // has passed relative to NOW, move to next possible occurrence.
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("title", context.getString(R.string.notification_qiyam_title))
            putExtra("body", context.getString(R.string.notification_qiyam_body))
            putExtra("channelId", NotificationHelper.QIYAM_CHANNEL_ID)
            putExtra("id", id)
            putExtra("soundEnabled", soundEnabled)
            addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context, id, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        scheduleAlarmCompat(calendar.timeInMillis, pendingIntent)
    }

    private fun cancelAlarm(id: Int) {
        val intent = Intent(context, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, id, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
        }
    }

    /**
     * Schedule an alarm with exact timing using setAlarmClock to bypass Doze mode optimizations.
     * Falls back to inexact on API 31+ if exact alarms are not permitted.
     */
    private fun scheduleAlarmCompat(triggerAtMillis: Long, pendingIntent: PendingIntent) {
        val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerAtMillis, pendingIntent)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
                } else {
                    Log.w(TAG, "Exact alarm permission not granted, using inexact alarm")
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerAtMillis,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException scheduling alarm: ${e.message}")
            // Fallback to inexact alarm
            alarmManager.setAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerAtMillis,
                pendingIntent
            )
        }
    }

    private fun getCalendarForTime(timeStr: String): Calendar {
        val parts = timeStr.split(":").map { it.toInt() }
        return Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, parts[0])
            set(Calendar.MINUTE, parts[1])
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }
}
