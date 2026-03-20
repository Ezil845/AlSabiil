package al.sabil.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.*
import androidx.glance.text.*
import androidx.glance.unit.ColorProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.appwidget.cornerRadius
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import al.sabil.MainActivity
import al.sabil.R
import al.sabil.data.SettingsManager
import al.sabil.utils.PrayerTimeCalculator
import al.sabil.utils.CalculationMethod
import al.sabil.utils.LocationService
import al.sabil.utils.PrayerTimes
import kotlinx.coroutines.flow.first
import java.time.chrono.HijrahDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.time.temporal.ChronoUnit

class PrayerWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val settingsManager = SettingsManager(context)
        val settings = settingsManager.settingsFlow.first()
        
        val loc = LocationService.getCachedLocation(context)
        
        val calcMethod = when (settings.calculationMethod) {
            "ISNA" -> CalculationMethod.ISNA
            "MAKKAH" -> CalculationMethod.MAKKAH
            "EGYPT" -> CalculationMethod.EGYPT
            "KARACHI" -> CalculationMethod.KARACHI
            else -> CalculationMethod.MWL
        }

        val prayerTimes = loc?.let {
            val calculator = PrayerTimeCalculator(it.latitude, it.longitude, method = calcMethod)
            calculator.calculateTimes()
        }

        val nextPrayerInfo = prayerTimes?.let {
            val calculator = PrayerTimeCalculator(loc.latitude, loc.longitude, method = calcMethod)
            calculator.getNextPrayer(it)
        }

        val hijriDate = try {
            HijrahDate.now().plus(settings.hijriOffset.toLong(), ChronoUnit.DAYS)
        } catch (e: Exception) {
            null
        }
        
        val hijriStr = hijriDate?.let {
            val day = it.get(java.time.temporal.ChronoField.DAY_OF_MONTH)
            val month = getLocalizedMonth(context, it.get(java.time.temporal.ChronoField.MONTH_OF_YEAR))
            val year = it.get(java.time.temporal.ChronoField.YEAR)
            "$day $month $year"
        } ?: ""

        provideContent {
            val emeraldPrimary = Color(0xFF059669)
            val emeraldDark = Color(0xFF064E3B)
            val creamBackground = Color(0xFFFFFCF2)
            val warningRed = Color(0xFFDC2626)
            val grayText = Color(0xFF6B7280)

            // Dynamic logic for countdown highlight
            val isUrgent = nextPrayerInfo != null && nextPrayerInfo.hoursLeft == 0 && nextPrayerInfo.minutesLeft < 30
            val highlightColor = if (isUrgent) warningRed else emeraldPrimary

            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(creamBackground)
                    .padding(12.dp)
                    .cornerRadius(28.dp)
                    .clickable(actionStartActivity<MainActivity>()),
                verticalAlignment = Alignment.Top,
                horizontalAlignment = Alignment.Start
            ) {
                if (prayerTimes == null || nextPrayerInfo == null) {
                    Box(modifier = GlanceModifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = context.getString(R.string.locating), style = TextStyle(color = ColorProvider(emeraldPrimary), fontWeight = FontWeight.Bold))
                    }
                } else {
                    // Bento Row 1: Next Prayer & Hijri
                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Hijri Date Badge
                        Box(
                            modifier = GlanceModifier
                                .background(emeraldPrimary.copy(alpha = 0.1f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .cornerRadius(12.dp)
                        ) {
                            Text(
                                text = hijriStr,
                                style = TextStyle(color = ColorProvider(emeraldDark), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            )
                        }
                        
                        Spacer(modifier = GlanceModifier.defaultWeight())
                        
                        // Prayer Name
                        Text(
                            text = getLocalizedPrayerName(context, nextPrayerInfo.name),
                            style = TextStyle(color = ColorProvider(grayText), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                        )
                    }

                    Spacer(modifier = GlanceModifier.height(8.dp))

                    // Bento Row 2: Large Countdown Highlight
                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Column(modifier = GlanceModifier.defaultWeight()) {
                            val timeStr = if (nextPrayerInfo.hoursLeft > 0) {
                                "${nextPrayerInfo.hoursLeft}:${String.format("%02d", nextPrayerInfo.minutesLeft)}"
                            } else {
                                "${nextPrayerInfo.minutesLeft}:${String.format("%02d", nextPrayerInfo.secondsLeft)}"
                            }
                            
                            val countdownLabel = if (nextPrayerInfo.hoursLeft > 0) {
                                context.getString(R.string.hours_left_short)
                            } else {
                                context.getString(R.string.minutes_left_short)
                            }

                            Text(
                                text = timeStr,
                                style = TextStyle(
                                    color = ColorProvider(highlightColor), 
                                    fontSize = 42.sp, 
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            
                            Text(
                                text = countdownLabel,
                                style = TextStyle(color = ColorProvider(grayText.copy(alpha = 0.8f)), fontSize = 10.sp)
                            )
                        }

                        // Right: Next Time
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = getPrayerTime(prayerTimes, nextPrayerInfo.name),
                                style = TextStyle(color = ColorProvider(emeraldDark), fontSize = 18.sp, fontWeight = FontWeight.Bold)
                            )
                            Text(
                                text = context.getString(R.string.adhan_label),
                                style = TextStyle(color = ColorProvider(grayText), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            )
                        }
                    }

                    Spacer(modifier = GlanceModifier.height(12.dp))

                    // Progress Bar
                    Box(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .background(emeraldPrimary.copy(alpha = 0.1f))
                            .cornerRadius(2.dp)
                    ) {
                        Row(modifier = GlanceModifier.fillMaxWidth()) {
                            val prayers = listOf("Fajr", "Dhuhr", "Asr", "Maghrib", "Isha")
                            prayers.forEach { p ->
                                Box(
                                    modifier = GlanceModifier
                                        .defaultWeight()
                                        .fillMaxHeight()
                                        .padding(horizontal = 1.dp)
                                        .background(if (p == nextPrayerInfo.name) highlightColor else emeraldPrimary.copy(alpha = 0.3f))
                                        .cornerRadius(2.dp)
                                ) {}
                            }
                        }
                    }
                }
            }
        }
    }

    private fun getLocalizedPrayerName(context: Context, name: String): String {
        return when (name) {
            "Fajr" -> context.getString(R.string.fajr)
            "Dhuhr" -> context.getString(R.string.dhuhr)
            "Asr" -> context.getString(R.string.asr)
            "Maghrib" -> context.getString(R.string.maghrib)
            "Isha" -> context.getString(R.string.isha)
            else -> name
        }
    }

    private fun getPrayerTime(times: PrayerTimes, name: String): String {
        return when (name) {
            "Fajr" -> times.fajr
            "Dhuhr" -> times.dhuhr
            "Asr" -> times.asr
            "Maghrib" -> times.maghrib
            "Isha" -> times.isha
            else -> "--:--"
        }
    }

    private fun getLocalizedMonth(context: Context, month: Int): String {
        val resId = context.resources.getIdentifier("hijri_month_$month", "string", context.packageName)
        return if (resId != 0) context.getString(resId) else ""
    }
}
