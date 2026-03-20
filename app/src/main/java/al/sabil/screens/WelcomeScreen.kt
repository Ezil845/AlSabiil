package al.sabil.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.*
import al.sabil.R
import al.sabil.components.AyahCard
import al.sabil.components.PrayerTimeCard
import al.sabil.components.LoadingIndicator
import al.sabil.repository.AyahRepository
import al.sabil.utils.LocationService
import al.sabil.utils.PrayerTimeCalculator
import al.sabil.utils.PrayerTimes
import al.sabil.data.SettingsManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*

@Composable
fun WelcomeScreen(
    onSettingsClick: () -> Unit,
    onCalendarClick: () -> Unit = {},
    onSunnahClick: () -> Unit = {},
    permissionsHandled: Boolean = true,
    settingsViewModel: al.sabil.viewmodel.SettingsViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val context = LocalContext.current
    val ayahRepository = remember { AyahRepository() }
    val userSettings by settingsViewModel.settings.collectAsState()
    
    val locatingText = stringResource(R.string.locating)
    val locationUnavailableText = stringResource(R.string.location_unavailable)
    var locationName by remember { mutableStateOf(locatingText) }
    var location by remember { mutableStateOf<android.location.Location?>(null) }
    
    val ayahContent = remember(userSettings?.hijriOffset) { 
        ayahRepository.getDailyContent(userSettings?.hijriOffset ?: 0) 
    }
    
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

    // Fetch Location — retry a few times in case permission was just granted
    LaunchedEffect(Unit) {
        var retries = 0
        while (retries < 5) {
            val loc = LocationService.getCurrentLocation(context)
            if (loc != null) {
                location = loc
                val name = LocationService.getCityNameWithCache(context, loc.latitude, loc.longitude)
                locationName = name
                break
            }
            retries++
            delay(2000L) // Wait 2s between retries for permission/location to become available
        }
        if (location == null) {
            // Try cached location as fallback
            val cached = LocationService.getCachedLocation(context)
            if (cached != null) {
                location = cached
                val name = LocationService.getCityNameWithCache(context, cached.latitude, cached.longitude)
                locationName = name
            } else {
                locationName = locationUnavailableText
            }
        }
    }

    // Calculate Prayer Times & Countdown
    LaunchedEffect(location, userSettings?.calculationMethod) {
        location?.let {
            val methodStr = userSettings?.calculationMethod ?: "MWL"
            val calcMethod = when (methodStr) {
                "ISNA" -> al.sabil.utils.CalculationMethod.ISNA
                "MAKKAH" -> al.sabil.utils.CalculationMethod.MAKKAH
                "EGYPT" -> al.sabil.utils.CalculationMethod.EGYPT
                "KARACHI" -> al.sabil.utils.CalculationMethod.KARACHI
                else -> al.sabil.utils.CalculationMethod.MWL
            }
            val calculator = PrayerTimeCalculator(it.latitude, it.longitude, method = calcMethod)
            val times = calculator.calculateTimes()
            prayerTimes = times
            
            // Calculate next prayer once, then just do simple countdown math each second
            var nextPrayer = calculator.getNextPrayer(times, includeSunrise = userSettings?.sunriseNotif ?: false)
            var targetMillis = System.currentTimeMillis() + 
                (nextPrayer.hoursLeft * 3600L + nextPrayer.minutesLeft * 60L + nextPrayer.secondsLeft) * 1000L
            nextPrayerName = nextPrayer.name
            
            while (true) {
                val remainingMillis = targetMillis - System.currentTimeMillis()
                
                if (remainingMillis <= 0) {
                    // Current prayer time reached — recalculate for the next one
                    nextPrayer = calculator.getNextPrayer(times, includeSunrise = userSettings?.sunriseNotif ?: false)
                    targetMillis = System.currentTimeMillis() + 
                        (nextPrayer.hoursLeft * 3600L + nextPrayer.minutesLeft * 60L + nextPrayer.secondsLeft) * 1000L
                    nextPrayerName = nextPrayer.name
                }
                
                val totalSeconds = (remainingMillis / 1000).coerceAtLeast(0)
                val h = (totalSeconds / 3600).toInt()
                val m = ((totalSeconds % 3600) / 60).toInt()
                val s = (totalSeconds % 60).toInt()
                countdown = String.format(Locale.ENGLISH, "%02d:%02d:%02d", h, m, s)
                
                delay(1000L)
            }
        }
    }

    // First-launch dialog flow
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    
    var showWelcomeDialog by remember { mutableStateOf(false) }

    // Logic for ordering the dialogs
    LaunchedEffect(permissionsHandled, userSettings?.firstLaunchCompleted) {
        if (userSettings?.firstLaunchCompleted == false && permissionsHandled) {
            showWelcomeDialog = true
        } else {
            showWelcomeDialog = false
        }
    }

    if (prayerTimes == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor),
            contentAlignment = Alignment.Center
        ) {
            LoadingIndicator()
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .verticalScroll(rememberScrollState())
                .padding(top = 16.dp, start = 16.dp, end = 16.dp, bottom = 100.dp), // Extra bottom padding for nav bar
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            
            PrayerTimeCard(
                nextPrayer = nextPrayerName,
                countdown = countdown,
                location = locationName,
                prayerTimes = prayerTimes!!,
                isMuted = isMuted,
                onMuteToggle = onMuteToggle,
                onSettingsClick = onSettingsClick,
                onCalendarClick = onCalendarClick,
                hijriOffset = userSettings?.hijriOffset ?: 0,
                showSunrise = userSettings?.sunriseNotif ?: false
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            AyahCard(contentList = ayahContent)
        }
    }

    // Show welcome overlay for the first time
    if (showWelcomeDialog) {
        val uriHandler = LocalUriHandler.current
        
        AlertDialog(
            onDismissRequest = { 
                // Prevent dismiss by tapping outside, force them to click "Got it"
            },
            title = {
                Text(
                    text = stringResource(R.string.welcome_test_mode_title),
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A202C)
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.welcome_test_mode_desc),
                    color = Color(0xFF4A5568)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = { 
                        settingsViewModel.updateFirstLaunchCompleted()
                        showWelcomeDialog = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF70a080))
                ) {
                    Text(text = stringResource(R.string.got_it), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { 
                            val sendIntent: android.content.Intent = android.content.Intent().apply {
                                action = android.content.Intent.ACTION_SEND
                                putExtra(android.content.Intent.EXTRA_TEXT, context.getString(R.string.share_app_text))
                                type = "text/plain"
                            }
                            val shareIntent = android.content.Intent.createChooser(sendIntent, null)
                            context.startActivity(shareIntent)
                        }
                    ) {
                        Icon(
                            imageVector = Lucide.Share2,
                            contentDescription = stringResource(R.string.share_app),
                            tint = Color(0xFF70a080),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = { 
                            uriHandler.openUri("https://github.com/Ezil845/AlSabiil.git")
                            settingsViewModel.updateFirstLaunchCompleted()
                            showWelcomeDialog = false
                        }
                    ) {
                        Icon(
                            imageVector = Lucide.Github,
                            contentDescription = stringResource(R.string.open_github),
                            tint = Color(0xFF1E293B),
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            },
            containerColor = Color.White,
            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
            tonalElevation = 0.dp
        )
    }


}
