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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.composables.icons.lucide.*
import com.example.alsabiil.R
import com.example.alsabiil.components.AyahCard
import com.example.alsabiil.components.PrayerTimeCard
import com.example.alsabiil.components.LoadingIndicator
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
    permissionsHandled: Boolean = true,
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
                countdown = String.format(Locale.ENGLISH, "%02d:%02d:%02d", h, m, s)
                
                delay(1000L)
            }
        }
    }

    // First-launch dialog flow
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    var hasOverlayPermission by remember { mutableStateOf(android.provider.Settings.canDrawOverlays(context)) }
    
    // Track if the user explicitly clicked "Cancel" on the overlay dialog so we can move on
    var overlayDialogDismissed by remember { mutableStateOf(false) }
    
    var showOverlayPermissionDialog by remember { mutableStateOf(false) }
    var showWelcomeDialog by remember { mutableStateOf(false) }

    // Recheck overlay permission when returning from system settings
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                hasOverlayPermission = android.provider.Settings.canDrawOverlays(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // Logic for ordering the dialogs
    LaunchedEffect(permissionsHandled, hasOverlayPermission, overlayDialogDismissed, userSettings?.firstLaunchCompleted) {
        if (userSettings?.firstLaunchCompleted == false && permissionsHandled) {
            if (!hasOverlayPermission && !overlayDialogDismissed) {
                // Step 1: Show Overlay Dialog
                showOverlayPermissionDialog = true
                showWelcomeDialog = false
            } else {
                // Step 2: Overlay is granted or dismissed -> Show Welcome Dialog
                showOverlayPermissionDialog = false
                showWelcomeDialog = true
            }
        } else {
            showOverlayPermissionDialog = false
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
                hijriOffset = userSettings?.hijriOffset ?: 0
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
            },
            containerColor = Color.White,
            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
            tonalElevation = 0.dp
        )
    }

    // Overlay permission dialog
    if (showOverlayPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showOverlayPermissionDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.overlay_permission_title),
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A202C)
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.overlay_permission_desc),
                    color = Color(0xFF4A5568)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val intent = android.content.Intent(
                            android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            android.net.Uri.parse("package:${context.packageName}")
                        )
                        context.startActivity(intent)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF70a080))
                ) {
                    Text(stringResource(R.string.open_settings), color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { overlayDialogDismissed = true }) {
                    Text(stringResource(R.string.cancel), color = Color.Gray)
                }
            },
            containerColor = Color.White,
            shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
            tonalElevation = 0.dp
        )
    }
}
