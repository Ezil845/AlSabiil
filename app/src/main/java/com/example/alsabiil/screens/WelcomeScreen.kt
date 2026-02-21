package com.example.alsabiil.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.alsabiil.R
import com.example.alsabiil.components.AyahCard
import com.example.alsabiil.components.PrayerTimeCard
import com.example.alsabiil.repository.AyahRepository
import com.example.alsabiil.utils.LocationService
import com.example.alsabiil.utils.PrayerTimeCalculator
import com.example.alsabiil.utils.PrayerTimes
import com.example.alsabiil.data.SettingsManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

@Composable
fun WelcomeScreen(
    onSettingsClick: () -> Unit,
    settingsViewModel: com.example.alsabiil.viewmodel.SettingsViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val context = LocalContext.current
    val ayahRepository = remember { AyahRepository() }
    val userSettings by settingsViewModel.settings.collectAsState()
    
    val locatingText = stringResource(R.string.locating)
    val locationUnavailableText = stringResource(R.string.location_unavailable)
    var locationName by remember { mutableStateOf(locatingText) }
    var location by remember { mutableStateOf<android.location.Location?>(null) }
    
    val ayahContent = remember { ayahRepository.getDailyContent() }
    
    var prayerTimes by remember { mutableStateOf<PrayerTimes?>(null) }
    var nextPrayerName by remember { mutableStateOf("--") }
    var countdown by remember { mutableStateOf("00:00:00") }

    val backgroundColor = Color(0xFFFFFCF2) // Explicit Mushaf background
    val primaryColor = MaterialTheme.colorScheme.primary

    // Calculate isMuted based on settings and next prayer
    // If notification is enabled (true), then isMuted is false.
    val isMuted = remember(nextPrayerName, userSettings) {
        userSettings?.let { settings ->
            when (nextPrayerName) {
                "Fajr" -> !settings.fajrNotif
                "Sunrise" -> !settings.sunriseNotif
                "Dhuhr" -> !settings.dhuhrNotif
                "Asr" -> !settings.asrNotif
                "Maghrib" -> !settings.maghribNotif
                "Isha" -> !settings.ishaNotif
                else -> false
            }
        } ?: false
    }

    // Toggle Mute Handler
    val onMuteToggle: () -> Unit = {
        val currentMuted = isMuted
        val newNotifValue = currentMuted // If muted (true), new notif value is true (enabled)
        
        when (nextPrayerName) {
            "Fajr" -> settingsViewModel.togglePrayerNotif(SettingsManager.FAJR_NOTIF, newNotifValue)
            "Sunrise" -> settingsViewModel.togglePrayerNotif(SettingsManager.SUNRISE_NOTIF, newNotifValue)
            "Dhuhr" -> settingsViewModel.togglePrayerNotif(SettingsManager.DHUHR_NOTIF, newNotifValue)
            "Asr" -> settingsViewModel.togglePrayerNotif(SettingsManager.ASR_NOTIF, newNotifValue)
            "Maghrib" -> settingsViewModel.togglePrayerNotif(SettingsManager.MAGHRIB_NOTIF, newNotifValue)
            "Isha" -> settingsViewModel.togglePrayerNotif(SettingsManager.ISHA_NOTIF, newNotifValue)
        }
    }

    // Fetch Location
    LaunchedEffect(Unit) {
        val loc = LocationService.getCurrentLocation(context)
        location = loc
        loc?.let {
            val name = LocationService.getCityNameWithCache(context, it.latitude, it.longitude)
            locationName = name
        } ?: run {
            locationName = locationUnavailableText
        }
    }

    // Calculate Prayer Times & Countdown
    LaunchedEffect(location, userSettings?.calculationMethod) {
        location?.let {
            val methodStr = userSettings?.calculationMethod ?: "MWL"
            val calcMethod = when (methodStr) {
                "ISNA" -> com.example.alsabiil.utils.CalculationMethod.ISNA
                "MAKKAH" -> com.example.alsabiil.utils.CalculationMethod.MAKKAH
                "EGYPT" -> com.example.alsabiil.utils.CalculationMethod.EGYPT
                "KARACHI" -> com.example.alsabiil.utils.CalculationMethod.KARACHI
                else -> com.example.alsabiil.utils.CalculationMethod.MWL
            }
            val calculator = PrayerTimeCalculator(it.latitude, it.longitude, method = calcMethod)
            val times = calculator.calculateTimes()
            prayerTimes = times
            
            // Calculate next prayer once, then just do simple countdown math each second
            var nextPrayer = calculator.getNextPrayer(times)
            var targetMillis = System.currentTimeMillis() + 
                (nextPrayer.hoursLeft * 3600L + nextPrayer.minutesLeft * 60L + nextPrayer.secondsLeft) * 1000L
            nextPrayerName = nextPrayer.name
            
            while (true) {
                val remainingMillis = targetMillis - System.currentTimeMillis()
                
                if (remainingMillis <= 0) {
                    // Current prayer time reached — recalculate for the next one
                    nextPrayer = calculator.getNextPrayer(times)
                    targetMillis = System.currentTimeMillis() + 
                        (nextPrayer.hoursLeft * 3600L + nextPrayer.minutesLeft * 60L + nextPrayer.secondsLeft) * 1000L
                    nextPrayerName = nextPrayer.name
                }
                
                val totalSeconds = (remainingMillis / 1000).coerceAtLeast(0)
                val h = (totalSeconds / 3600).toInt()
                val m = ((totalSeconds % 3600) / 60).toInt()
                val s = (totalSeconds % 60).toInt()
                countdown = String.format("%02d:%02d:%02d", h, m, s)
                
                delay(1000L)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .verticalScroll(rememberScrollState())
            .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 100.dp), // Extra bottom padding for nav bar
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))
        
        prayerTimes?.let { times ->
            PrayerTimeCard(
                nextPrayer = nextPrayerName,
                countdown = countdown,
                location = locationName,
                prayerTimes = times,
                isMuted = isMuted,
                onMuteToggle = onMuteToggle,
                onSettingsClick = onSettingsClick,
                hijriOffset = userSettings?.hijriOffset ?: 0
            )
        } ?: Box(
            modifier = Modifier.fillMaxWidth().height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = primaryColor)
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        AyahCard(contentList = ayahContent)
    }
}
