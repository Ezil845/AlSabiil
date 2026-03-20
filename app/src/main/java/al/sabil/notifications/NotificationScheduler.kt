package al.sabil.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import al.sabil.data.UserSettings
import al.sabil.utils.PrayerTimeCalculator
import al.sabil.R
import al.sabil.utils.HijriEvents
import java.time.chrono.HijrahDate
import java.time.temporal.ChronoField
import java.time.temporal.ChronoUnit
import java.util.*

private const val TAG = "NotificationScheduler"

class NotificationScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleNotifications(settings: UserSettings, latitude: Double, longitude: Double) {
        Log.d(TAG, "scheduleNotifications called. lat=$latitude, lon=$longitude")
        
        val calcMethod = when (settings.calculationMethod) {
            "ISNA" -> al.sabil.utils.CalculationMethod.ISNA
            "MAKKAH" -> al.sabil.utils.CalculationMethod.MAKKAH
            "EGYPT" -> al.sabil.utils.CalculationMethod.EGYPT
            "KARACHI" -> al.sabil.utils.CalculationMethod.KARACHI
            else -> al.sabil.utils.CalculationMethod.MWL
        }
        val calculator = PrayerTimeCalculator(latitude, longitude, method = calcMethod)
        val prayerTimes = calculator.calculateDetailedPrayerTimes(latitude, longitude)
        
        Log.d(TAG, "Prayer times: Fajr=${prayerTimes.fajr}, Dhuhr=${prayerTimes.dhuhr}, Asr=${prayerTimes.asr}, Maghrib=${prayerTimes.maghrib}, Isha=${prayerTimes.isha}")

        // 1. Prayer Notifications
        schedulePrayerAlarm(context.getString(R.string.fajr), prayerTimes.fajr, settings.fajrNotif, 100, latitude, longitude)
        schedulePrayerAlarm(context.getString(R.string.sunrise), prayerTimes.sunrise, settings.sunriseNotif, 101, latitude, longitude)
        schedulePrayerAlarm(context.getString(R.string.dhuhr), prayerTimes.dhuhr, settings.dhuhrNotif, 102, latitude, longitude)
        schedulePrayerAlarm(context.getString(R.string.asr), prayerTimes.asr, settings.asrNotif, 103, latitude, longitude)
        schedulePrayerAlarm(context.getString(R.string.maghrib), prayerTimes.maghrib, settings.maghribNotif, 104, latitude, longitude)
        schedulePrayerAlarm(context.getString(R.string.isha), prayerTimes.isha, settings.ishaNotif, 105, latitude, longitude)

        // 2. Adhkar Notifications (relative to prayers)
        // Morning: Fajr + 30 mins
        scheduleAdhkarAlarm(context.getString(R.string.morning_azkar), prayerTimes.fajr, 30, settings.morningAdhkar, settings.adhkarSoundEnabled, 200, latitude, longitude)
        // Evening: Asr + 15 mins
        scheduleAdhkarAlarm(context.getString(R.string.evening_azkar), prayerTimes.asr, 15, settings.eveningAdhkar, settings.adhkarSoundEnabled, 201, latitude, longitude)
        // Qiyam: Custom user time or default relative to Fajr (Always with sound if enabled)
        scheduleQiyamAlarm(settings.qiyamTime, prayerTimes.fajr, settings.qiyamAdhkar, true, 202, latitude, longitude)

        // 3. Khatmah Daily Reminder
        scheduleKhatmahReminderAlarm(settings)

        // 4. Hijri Event Notifications
        scheduleHijriEventAlarms(settings.hijriOffset, prayerTimes.maghrib)
    }

    private fun scheduleHijriEventAlarms(offset: Int, maghribTimeStr: String) {
        val todayHijri = try {
            HijrahDate.now().plus(offset.toLong(), ChronoUnit.DAYS)
        } catch (e: Exception) {
            return
        }

        // Check for today and tomorrow for events
        checkAndScheduleEvent(todayHijri, 0, maghribTimeStr)
        checkAndScheduleEvent(todayHijri.plus(1, ChronoUnit.DAYS), 1, maghribTimeStr)
    }

    private fun checkAndScheduleEvent(hijriDate: HijrahDate, dayOffset: Int, maghribTimeStr: String) {
        val day = hijriDate.get(ChronoField.DAY_OF_MONTH)
        val month = hijriDate.get(ChronoField.MONTH_OF_YEAR)
        
        // We want to check if TOMORROW is Laylat al-Qadr, because the night of Laylat al-Qadr
        // happens on the evening BEFORE the actual day (Islamic days start at Maghrib).
        val tomorrowHijri = hijriDate.plus(1, ChronoUnit.DAYS)
        val tomorrowDay = tomorrowHijri.get(ChronoField.DAY_OF_MONTH)
        val tomorrowMonth = tomorrowHijri.get(ChronoField.MONTH_OF_YEAR)
        
        val eventTomorrow = HijriEvents.getEventForDate(tomorrowDay, tomorrowMonth)
        val eventToday = HijriEvents.getEventForDate(day, month)

        // 1. Special handling for Laylat al-Qadr: Notify the evening BEFORE
        if (eventTomorrow != null && eventTomorrow.nameResId == R.string.event_laylat_al_qadr) {
            val calendar = getCalendarForTime(maghribTimeStr).apply {
                if (dayOffset > 0) add(Calendar.DAY_OF_YEAR, dayOffset)
                // Schedule 30 minutes after Maghrib
                add(Calendar.MINUTE, 30)
            }

            if (calendar.timeInMillis > System.currentTimeMillis()) {
                val intent = Intent(context, NotificationReceiver::class.java).apply {
                    putExtra("title", context.getString(eventTomorrow.nameResId))
                    putExtra("body", context.getString(eventTomorrow.descResId))
                    putExtra("channelId", NotificationHelper.ADHKAR_CHANNEL_ID)
                    putExtra("id", 400 + tomorrowMonth * 100 + tomorrowDay) // Unique ID for Qadr
                    addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
                }

                val pendingIntent = PendingIntent.getBroadcast(
                    context, 400 + tomorrowMonth * 100 + tomorrowDay, intent,
                    PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )

                scheduleAlarmCompat(calendar.timeInMillis, pendingIntent)
            }
        }

        // 2. Normal handling for other events (9:00 AM on the day of the event)
        if (eventToday != null && eventToday.nameResId != R.string.event_laylat_al_qadr) {
            val calendar = Calendar.getInstance().apply {
                if (dayOffset > 0) add(Calendar.DAY_OF_YEAR, dayOffset)
                set(Calendar.HOUR_OF_DAY, 9) // 9:00 AM
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            if (calendar.timeInMillis > System.currentTimeMillis()) {
                val intent = Intent(context, NotificationReceiver::class.java).apply {
                    putExtra("title", context.getString(eventToday.nameResId))
                    putExtra("body", context.getString(eventToday.descResId))
                    putExtra("channelId", NotificationHelper.ADHKAR_CHANNEL_ID)
                    putExtra("id", 300 + month * 100 + day) // Unique ID for events
                    addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
                }

                val pendingIntent = PendingIntent.getBroadcast(
                    context, 300 + month * 100 + day, intent,
                    PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )

                scheduleAlarmCompat(calendar.timeInMillis, pendingIntent)
            }
        }
    }

    private fun schedulePrayerAlarm(name: String, timeStr: String, enabled: Boolean, id: Int, latitude: Double, longitude: Double) {
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
            // Sunrise (id=101) shouldn't trigger adhan sound or full-screen alert
            val channelId = if (id == 101) NotificationHelper.ADHKAR_CHANNEL_ID else NotificationHelper.ADHAN_CHANNEL_ID
            putExtra("channelId", channelId)
            putExtra("id", id)
            putExtra("latitude", latitude)
            putExtra("longitude", longitude)
            addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context, id, intent,
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        scheduleAlarmCompat(calendar.timeInMillis, pendingIntent)
    }

    private fun scheduleAdhkarAlarm(name: String, relativeTo: String, offsetMinutes: Int, enabled: Boolean, soundEnabled: Boolean, id: Int, latitude: Double, longitude: Double) {
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
            putExtra("latitude", latitude)
            putExtra("longitude", longitude)
            addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context, id, intent,
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        scheduleAlarmCompat(calendar.timeInMillis, pendingIntent)
    }

    private fun scheduleQiyamAlarm(qiyamTimeSetting: String, fajrTomorrowStr: String, enabled: Boolean, soundEnabled: Boolean, id: Int, latitude: Double, longitude: Double) {
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
            putExtra("latitude", latitude)
            putExtra("longitude", longitude)
            addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context, id, intent,
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        scheduleAlarmCompat(calendar.timeInMillis, pendingIntent)
    }

    private fun scheduleKhatmahReminderAlarm(settings: UserSettings) {
        val id = 203
        if (!settings.khatmahActive || !settings.khatmahReminderEnabled) {
            cancelAlarm(id)
            return
        }

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 8)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // If 8:00 AM has already passed today, schedule for tomorrow
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("title", context.getString(R.string.khatmah_reminder_notif_title))
            putExtra("body", context.getString(R.string.khatmah_reminder_notif_body))
            putExtra("channelId", NotificationHelper.ADHKAR_CHANNEL_ID)
            putExtra("id", id)
            addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context, id, intent,
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        scheduleAlarmCompat(calendar.timeInMillis, pendingIntent)
    }

    private fun cancelAlarm(id: Int) {
        val intent = Intent(context, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, id, intent,
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_NO_CREATE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
        }
    }

    /**
     * Schedule an alarm with exact timing using setAlarmClock to bypass Doze mode.
     * USE_EXACT_ALARM (API 33+) guarantees this permission is always granted.
     * For API 31-32, SCHEDULE_EXACT_ALARM is used (can be revoked, so we have a fallback).
     */
    private fun scheduleAlarmCompat(triggerAtMillis: Long, pendingIntent: PendingIntent) {
        val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerAtMillis, pendingIntent)
        try {
            alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
        } catch (e: SecurityException) {
            Log.w(TAG, "setAlarmClock failed (exact alarm permission revoked?), using exact fallback: ${e.message}")
            try {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            } catch (e2: SecurityException) {
                Log.e(TAG, "setExactAndAllowWhileIdle also failed, final fallback: ${e2.message}")
                alarmManager.setAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerAtMillis,
                    pendingIntent
                )
            }
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
