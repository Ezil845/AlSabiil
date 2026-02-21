package com.example.alsabiil.utils

import java.util.*
import kotlin.math.*

enum class CalculationMethod(val fajrAngle: Double, val ishaAngle: Double) {
    ISNA(15.0, 15.0),
    MWL(18.0, 17.0),
    EGYPT(19.5, 17.5),
    KARACHI(18.0, 18.0),
    MAKKAH(18.5, 90.0) // Isha is 90 min after Maghrib
}

data class PrayerTimes(
    val fajr: String,
    val sunrise: String,
    val dhuhr: String,
    val asr: String,
    val maghrib: String,
    val isha: String
)

data class NextPrayerInfo(
    val name: String,
    val hoursLeft: Int,
    val minutesLeft: Int,
    val secondsLeft: Int
)

class PrayerTimeCalculator(
    private val latitude: Double,
    private val longitude: Double,
    private val method: CalculationMethod = CalculationMethod.ISNA
) {
    // ... existing private methods ...
    private val timezone = (TimeZone.getDefault().rawOffset / 3600000.0)

    private fun degToRad(deg: Double) = deg * PI / 180.0
    private fun radToDeg(rad: Double) = rad * 180.0 / PI

    private fun getJulianDay(date: Calendar): Double {
        val year = date.get(Calendar.YEAR)
        val month = date.get(Calendar.MONTH) + 1
        val day = date.get(Calendar.DAY_OF_MONTH)

        val a = floor((14.0 - month) / 12.0)
        val y = year + 4800 - a
        val m = month + 12 * a - 3
        
        return day + floor((153.0 * m + 2.0) / 5.0) + 365.0 * y + floor(y / 4.0) - floor(y / 100.0) + floor(y / 400.0) - 32045.0 - 0.5
    }

    private fun getSunDeclination(jd: Double): Double {
        val n = jd - 2451545.0
        val l = (280.460 + 0.9856474 * n) % 360.0
        val g = degToRad((357.528 + 0.9856003 * n) % 360.0)
        val lambda = degToRad(l + 1.915 * sin(g) + 0.020 * sin(2.0 * g))
        return asin(sin(lambda) * sin(degToRad(23.439)))
    }

    private fun getEquationOfTime(jd: Double): Double {
        val n = jd - 2451545.0
        val l = degToRad((280.460 + 0.9856474 * n) % 360.0)
        val g = degToRad((357.528 + 0.9856003 * n) % 360.0)
        val lambda = l + degToRad(1.915 * sin(g) + 0.020 * sin(2.0 * g))
        val alpha = atan2(cos(degToRad(23.439)) * sin(lambda), cos(lambda))
        return radToDeg(l - alpha) * 4.0
    }

    private fun getHourAngle(angle: Double, dec: Double, lat: Double): Double? {
        val rLat = degToRad(lat)
        val rAlt = degToRad(angle)
        val cosH = (sin(rAlt) - sin(rLat) * sin(dec)) / (cos(rLat) * cos(dec))
        return if (cosH in -1.0..1.0) acos(cosH) else null
    }

    private fun getAsrHourAngle(dec: Double, lat: Double): Double? {
        val rLat = degToRad(lat)
        val shadowLength = 1.0 + tan(abs(rLat - dec))
        val altitude = atan(1.0 / shadowLength)
        return getHourAngle(radToDeg(altitude), dec, lat)
    }

    private fun hourAngleToTime(ha: Double?, eot: Double): Double? {
        if (ha == null) return null
        val time = 12.0 - radToDeg(ha) / 15.0 - eot / 60.0 - longitude / 15.0 + timezone
        return ((time % 24.0) + 24.0) % 24.0
    }

    private fun formatTime(time: Double?): String {
        if (time == null) return "--:--"
        val totalMinutes = (time * 60.0).roundToInt()
        val hours = (totalMinutes / 60) % 24
        val minutes = totalMinutes % 60
        return String.format("%02d:%02d", hours, minutes)
    }

    fun calculateTimes(date: Calendar = Calendar.getInstance()): PrayerTimes {
        val jd = getJulianDay(date)
        val dec = getSunDeclination(jd)
        val eot = getEquationOfTime(jd)

        val fajrHA = getHourAngle(-method.fajrAngle, dec, latitude)
        val sunriseHA = getHourAngle(-0.833, dec, latitude)
        val asrHA = getAsrHourAngle(dec, latitude)
        
        val fajr = hourAngleToTime(fajrHA, eot)
        val sunrise = hourAngleToTime(sunriseHA, eot)
        val dhuhr = 12.0 - eot / 60.0 - longitude / 15.0 + timezone
        val dhuhrAdj = ((dhuhr % 24.0) + 24.0) % 24.0
        val asr = asrHA?.let { hourAngleToTime(-it, eot) }
        val sunset = sunriseHA?.let { hourAngleToTime(-it, eot) }
        val maghrib = sunset?.plus(4.0 / 60.0) // 4 min buffer
        
        val isha = if (method == CalculationMethod.MAKKAH) {
            maghrib?.plus(1.5)
        } else {
            val ishaHA = getHourAngle(-method.ishaAngle, dec, latitude)
            ishaHA?.let { hourAngleToTime(-it, eot) }
        }

        return PrayerTimes(
            fajr = formatTime(fajr),
            sunrise = formatTime(sunrise),
            dhuhr = formatTime(dhuhrAdj),
            asr = formatTime(asr),
            maghrib = formatTime(maghrib),
            isha = formatTime(isha)
        )
    }

    fun calculateDetailedPrayerTimes(lat: Double, lon: Double): PrayerTimes {
        return calculateTimes()
    }
    
    fun getNextPrayer(times: PrayerTimes): NextPrayerInfo {
        val now = Calendar.getInstance()
        val prayers = listOf("Fajr", "Dhuhr", "Asr", "Maghrib", "Isha")
        val timeStrings = listOf(times.fajr, times.dhuhr, times.asr, times.maghrib, times.isha)
        
        for (i in prayers.indices) {
            val (h, m) = timeStrings[i].split(":").map { it.toInt() }
            val prayerCal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, h)
                set(Calendar.MINUTE, m)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            
            if (prayerCal.after(now)) {
                val diffMillis = prayerCal.timeInMillis - now.timeInMillis
                val hours = (diffMillis / (1000 * 60 * 60)).toInt()
                val minutes = ((diffMillis / (1000 * 60)) % 60).toInt()
                val seconds = ((diffMillis / 1000) % 60).toInt()
                return NextPrayerInfo(prayers[i], hours, minutes, seconds)
            }
        }
        
        // Next is Fajr tomorrow
        val (h, m) = times.fajr.split(":").map { it.toInt() }
        val prayerCal = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_YEAR, 1)
            set(Calendar.HOUR_OF_DAY, h)
            set(Calendar.MINUTE, m)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        val diffMillis = prayerCal.timeInMillis - now.timeInMillis
        val hours = (diffMillis / (1000 * 60 * 60)).toInt()
        val minutes = ((diffMillis / (1000 * 60)) % 60).toInt()
        val seconds = ((diffMillis / 1000) % 60).toInt()
        
        return NextPrayerInfo("Fajr", hours, minutes, seconds)
    }

    fun getCurrentPrayer(times: PrayerTimes): String {
        // ... (implementation unchanged, keeping as is)
        val now = Calendar.getInstance()
        val currentTime = now.get(Calendar.HOUR_OF_DAY) + now.get(Calendar.MINUTE) / 60.0
        
        val decimals = timeStringsToDecimals(times)
        
        if (currentTime >= decimals[4] || currentTime < decimals[0]) return "Isha"
        if (currentTime >= decimals[0] && currentTime < decimals[1]) return "Fajr"
        if (currentTime >= decimals[1] && currentTime < decimals[2]) return "Dhuhr"
        if (currentTime >= decimals[2] && currentTime < decimals[3]) return "Asr"
        if (currentTime >= decimals[3] && currentTime < decimals[4]) return "Maghrib"
        
        return "Unknown"
    }

    private fun timeStringsToDecimals(times: PrayerTimes): List<Double> {
        return listOf(times.fajr, times.dhuhr, times.asr, times.maghrib, times.isha).map {
            val parts = it.split(":")
            parts[0].toDouble() + parts[1].toDouble() / 60.0
        }
    }
}
