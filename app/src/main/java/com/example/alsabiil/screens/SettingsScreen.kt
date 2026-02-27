package com.example.alsabiil.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.alsabiil.R
import com.example.alsabiil.data.SettingsManager
import com.example.alsabiil.viewmodel.SettingsViewModel
import com.composables.icons.lucide.*

import androidx.compose.material.icons.filled.Schedule
import androidx.compose.ui.platform.LocalContext
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onManageBookmarks: () -> Unit,
    viewModel: SettingsViewModel = viewModel()
) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsState()
    val backgroundColor = Color.White

    var showTimePicker by remember { mutableStateOf(false) }
    var showOverlayPermissionDialog by remember { mutableStateOf(false) }

    if (showOverlayPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showOverlayPermissionDialog = false },
            title = { Text(stringResource(R.string.overlay_permission_title)) },
            text = { Text(stringResource(R.string.overlay_permission_desc)) },
            confirmButton = {
                Button(
                    onClick = {
                        val intent = android.content.Intent(
                            android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            android.net.Uri.parse("package:${context.packageName}")
                        )
                        context.startActivity(intent)
                        showOverlayPermissionDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF70a080))
                ) {
                    Text(stringResource(R.string.open_settings), color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showOverlayPermissionDialog = false }) {
                    Text(stringResource(R.string.cancel), color = Color.Gray)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(24.dp)
        )
    }

    if (showTimePicker && settings != null) {
        val current = if (settings!!.qiyamTime == "DEFAULT") {
            Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 3); set(Calendar.MINUTE, 0) }
        } else {
            val parts = settings!!.qiyamTime.split(":").map { it.toInt() }
            Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, parts[0]); set(Calendar.MINUTE, parts[1]) }
        }

        val timePickerState = rememberTimePickerState(
            initialHour = current.get(Calendar.HOUR_OF_DAY),
            initialMinute = current.get(Calendar.MINUTE),
            is24Hour = true
        )

        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
            content = {
                Surface(
                    modifier = Modifier
                        .width(IntrinsicSize.Max)
                        .wrapContentHeight()
                        .padding(24.dp),
                    shape = RoundedCornerShape(28.dp),
                    color = Color.White,
                    tonalElevation = 6.dp
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.qiyam_time_label),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A202C)
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        TimePicker(
                            state = timePickerState,
                            colors = TimePickerDefaults.colors(
                                clockDialColor = Color(0xFFF1F5F9),
                                clockDialSelectedContentColor = Color.White,
                                clockDialUnselectedContentColor = Color(0xFF4A5568),
                                selectorColor = Color(0xFF70a080),
                                periodSelectorSelectedContainerColor = Color(0xFF70a080).copy(alpha = 0.2f),
                                periodSelectorSelectedContentColor = Color(0xFF70a080),
                                periodSelectorUnselectedContentColor = Color(0xFF4A5568),
                                timeSelectorSelectedContainerColor = Color(0xFF70a080).copy(alpha = 0.2f),
                                timeSelectorSelectedContentColor = Color(0xFF70a080),
                                timeSelectorUnselectedContainerColor = Color(0xFFF1F5F9),
                                timeSelectorUnselectedContentColor = Color(0xFF4A5568)
                            )
                        )
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = { 
                                viewModel.updateQiyamTime("DEFAULT")
                                showTimePicker = false 
                            }) {
                                Text(
                                    text = stringResource(R.string.default_time), 
                                    color = Color.Gray,
                                    fontSize = 11.sp,
                                    maxLines = 1
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Button(
                                onClick = {
                                    viewModel.updateQiyamTime(String.format("%02d:%02d", timePickerState.hour, timePickerState.minute))
                                    showTimePicker = false
                                },
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF70a080))
                            ) {
                                Text(stringResource(R.string.save), color = Color.White, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        stringResource(R.string.settings_title), 
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333)
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ChevronLeft, 
                            contentDescription = stringResource(R.string.back_button),
                            tint = Color(0xFF333333)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = backgroundColor
                )
            )
        },
        containerColor = backgroundColor
    ) { padding ->
        settings?.let { userSettings ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item { SectionTitle(stringResource(R.string.notifications_title)) }
                
                item {
                    SettingsCard {
                        NotificationRow(stringResource(R.string.fajr), userSettings.fajrNotif, onToggle = { viewModel.togglePrayerNotif(SettingsManager.FAJR_NOTIF, it) })
                        HorizontalDivider(color = Color(0xFFEEEEEE))
                        NotificationRow(stringResource(R.string.sunrise), userSettings.sunriseNotif, onToggle = { viewModel.togglePrayerNotif(SettingsManager.SUNRISE_NOTIF, it) })
                        HorizontalDivider(color = Color(0xFFEEEEEE))
                        NotificationRow(stringResource(R.string.dhuhr), userSettings.dhuhrNotif, onToggle = { viewModel.togglePrayerNotif(SettingsManager.DHUHR_NOTIF, it) })
                        HorizontalDivider(color = Color(0xFFEEEEEE))
                        NotificationRow(stringResource(R.string.asr), userSettings.asrNotif, onToggle = { viewModel.togglePrayerNotif(SettingsManager.ASR_NOTIF, it) })
                        HorizontalDivider(color = Color(0xFFEEEEEE))
                        NotificationRow(stringResource(R.string.maghrib), userSettings.maghribNotif, onToggle = { viewModel.togglePrayerNotif(SettingsManager.MAGHRIB_NOTIF, it) })
                        HorizontalDivider(color = Color(0xFFEEEEEE))
                        NotificationRow(stringResource(R.string.isha), userSettings.ishaNotif, onToggle = { viewModel.togglePrayerNotif(SettingsManager.ISHA_NOTIF, it) })
                    }
                }

                item { SectionTitle(stringResource(R.string.adhkar_reminders_title)) }
                item {
                    SettingsCard {
                        NotificationRow(stringResource(R.string.morning_azkar), userSettings.morningAdhkar, onToggle = { viewModel.toggleAdhkarNotif(SettingsManager.MORNING_ADHKAR, it) })
                        HorizontalDivider(color = Color(0xFFEEEEEE))
                        NotificationRow(stringResource(R.string.evening_azkar), userSettings.eveningAdhkar, onToggle = { viewModel.toggleAdhkarNotif(SettingsManager.EVENING_ADHKAR, it) })
                        HorizontalDivider(color = Color(0xFFEEEEEE))
                        NotificationRow(
                            label = stringResource(R.string.qiyam_adhkar), 
                            isEnabled = userSettings.qiyamAdhkar, 
                            onToggle = { enabled ->
                                if (enabled) {
                                    // Check if we have 'Display over other apps' permission
                                    if (!android.provider.Settings.canDrawOverlays(context)) {
                                        showOverlayPermissionDialog = true
                                    } else {
                                        viewModel.toggleAdhkarNotif(SettingsManager.QIYAM_ADHKAR, true)
                                    }
                                } else {
                                    viewModel.toggleAdhkarNotif(SettingsManager.QIYAM_ADHKAR, false)
                                }
                            },
                            subtitle = stringResource(R.string.qiyam_alarm_desc)
                        )
                        HorizontalDivider(color = Color(0xFFEEEEEE))
                        
                        // Custom Qiyam time
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showTimePicker = true }
                                .padding(horizontal = 16.dp, vertical = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = stringResource(R.string.qiyam_time_label),
                                    fontSize = 16.sp,
                                    color = Color(0xFF333333),
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    text = if (userSettings.qiyamTime == "DEFAULT") stringResource(R.string.default_time) else userSettings.qiyamTime,
                                    fontSize = 12.sp,
                                    color = Color(0xFF70a080)
                                )
                            }
                            Icon(Icons.Default.Schedule, contentDescription = null, tint = Color(0xFF70a080))
                        }
                        


                    }
                }

                item { SectionTitle(stringResource(R.string.adhan_settings_title)) }
                item {
                    SettingsCard {
                        NotificationRow(
                            label = stringResource(R.string.force_adhan_silent), 
                            isEnabled = userSettings.forceAdhanInSilent, 
                            onToggle = { viewModel.toggleForceAdhanInSilent(it) }
                        )
                        HorizontalDivider(color = Color(0xFFEEEEEE))
                        NotificationRow(
                            label = stringResource(R.string.use_system_volume), 
                            isEnabled = userSettings.useSystemVolume, 
                            onToggle = { viewModel.toggleUseSystemVolume(it) }
                        )
                        HorizontalDivider(color = Color(0xFFEEEEEE))
                        
                        AdhanSelector(
                            selected = userSettings.selectedAdhan,
                            onSelect = { viewModel.updateSelectedAdhan(it) },
                            onTest = { viewModel.playTestAdhan() },
                            onStop = { viewModel.stopAdhan() }
                        )
                    }
                }

                item { SectionTitle(stringResource(R.string.general_title)) }
                item {
                    SettingsCard {
                        GeneralRow(stringResource(R.string.location_label), stringResource(R.string.auto_detect))
                        HorizontalDivider(color = Color(0xFFEEEEEE))
                        GeneralRow(stringResource(R.string.language_label), "العربية")
                    }
                }

                item { SectionTitle(stringResource(R.string.hijri_adjustment_title)) }
                item {
                    SettingsCard {
                        val offsets = listOf(
                            -2 to stringResource(R.string.hijri_minus_two),
                            -1 to stringResource(R.string.hijri_minus_one),
                            0 to stringResource(R.string.hijri_zero),
                            1 to stringResource(R.string.hijri_plus_one),
                            2 to stringResource(R.string.hijri_plus_two)
                        )
                        
                        Text(
                            text = stringResource(R.string.hijri_adjustment_desc),
                            fontSize = 12.sp,
                            lineHeight = 18.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(16.dp)
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .padding(bottom = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            offsets.forEach { (offset, label) ->
                                val isSelected = userSettings.hijriOffset == offset
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { viewModel.updateHijriOffset(offset) }
                                        .padding(vertical = 8.dp)
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { viewModel.updateHijriOffset(offset) },
                                        colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF70a080))
                                    )
                                    Text(
                                        text = label,
                                        fontSize = 14.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color(0xFF70a080) else Color(0xFF333333)
                                    )
                                }
                            }
                        }
                    }
                }

                item { SectionTitle(stringResource(R.string.bookmarks_title)) }
                item {
                    SettingsCard {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onManageBookmarks() }
                                .padding(horizontal = 16.dp, vertical = 16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.manage_bookmarks),
                                fontSize = 16.sp,
                                color = Color(0xFF333333),
                                fontWeight = FontWeight.Medium
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.List,
                                contentDescription = stringResource(R.string.bookmarks_title),
                                tint = Color(0xFF70a080)
                            )
                        }
                    }
                }

/*
                item { SectionTitle("CALCULATION METHOD") }
                item {
                    val methods = listOf("MWL", "ISNA", "MAKKAH", "EGYPT", "KARACHI")
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(methods) { method ->
                            FilterChip(
                                selected = userSettings.calculationMethod == method,
                                onClick = { viewModel.updateCalculationMethod(method) },
                                label = { Text(method) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color(0xFF70a080),
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
*/

                item { SectionTitle(stringResource(R.string.color_palette_title)) }
                item {
                    PaletteSelector(
                        selected = userSettings.selectedPalette,
                        onSelect = { viewModel.updatePalette(it) }
                    )
                }

                item { SectionTitle(stringResource(R.string.tafseer_title)) }
                item {
                    SettingsCard {
                        TafseerSelectionRow(stringResource(R.string.tafseer_saddi), userSettings.selectedTafseer == "saddi") {
                            viewModel.updateTafseer("saddi")
                        }
                        HorizontalDivider(color = Color(0xFFEEEEEE))
                        TafseerSelectionRow(stringResource(R.string.tafseer_ibn_kathir), userSettings.selectedTafseer == "ibn_kathir") {
                            viewModel.updateTafseer("ibn_kathir")
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(32.dp)) }
                
                item {
                    val uriHandler = androidx.compose.ui.platform.LocalUriHandler.current
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(
                            onClick = { uriHandler.openUri("https://github.com/Ezil845/AlSabiil.git") },
                            modifier = Modifier.size(64.dp)
                        ) {
                            Icon(
                                imageVector = Lucide.Github,
                                contentDescription = "Open GitHub",
                                tint = Color(0xFFa0b0a0),
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }
                }
                
                item { Spacer(modifier = Modifier.height(40.dp)) }
            }
        } ?: run {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF70a080))
            }
        }
    }
}

@Composable
fun SectionTitle(title: String) {
    Text(
        text = title,
        fontSize = 14.sp,
        fontWeight = FontWeight.Bold,
        color = Color(0xFF70a080), // Sage Green
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
        letterSpacing = 1.sp
    )
}

@Composable
fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(vertical = 4.dp), content = content)
    }
}

@Composable
fun NotificationRow(
    label: String, 
    isEnabled: Boolean, 
    onToggle: (Boolean) -> Unit,
    subtitle: String? = null,
    action: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Column {
                Text(
                    text = label, 
                    fontSize = 16.sp,
                    color = Color(0xFF333333),
                    fontWeight = FontWeight.Medium
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        fontSize = 12.sp,
                        color = Color.Gray,
                        lineHeight = 16.sp
                    )
                }
            }
            if (action != null) {
                Spacer(modifier = Modifier.width(8.dp))
                action()
            }
        }
        Switch(
            checked = isEnabled,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color(0xFFFFA500), // Orange for active
                uncheckedThumbColor = Color(0xFFF4F3F4),
                uncheckedTrackColor = Color(0xFF767577)
            )
        )
    }
}

@Composable
fun GeneralRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label, 
            fontSize = 16.sp,
            color = Color(0xFF333333),
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            fontSize = 14.sp,
            color = Color(0xFF666666)
        )
    }
}

@Composable
fun TafseerSelectionRow(label: String, isSelected: Boolean, onSelect: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 16.sp,
            color = Color(0xFF333333),
            fontWeight = FontWeight.Medium
        )
        RadioButton(
            selected = isSelected,
            onClick = onSelect,
            colors = RadioButtonDefaults.colors(
                selectedColor = Color(0xFF70a080)
            )
        )
    }
}

@Composable
fun AdhanSelector(
    selected: String,
    onSelect: (String) -> Unit,
    onTest: () -> Unit,
    onStop: () -> Unit
) {
    val adhans = listOf(
        "adhan_ahmed_kourdi" to stringResource(R.string.adhan_ahmed_kourdi),
        "adhan_makkah" to stringResource(R.string.adhan_makkah),
        "adhan_rabeh" to stringResource(R.string.adhan_rabeh)
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.select_adhan),
                fontSize = 16.sp,
                color = Color(0xFF333333),
                fontWeight = FontWeight.Medium
            )
            
            Row {
                IconButton(onClick = onTest, modifier = Modifier.size(32.dp)) {
                    Icon(Lucide.Play, contentDescription = "Test", tint = Color(0xFF70a080), modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = onStop, modifier = Modifier.size(32.dp)) {
                    Icon(Lucide.Square, contentDescription = "Stop", tint = Color.Red.copy(alpha = 0.7f), modifier = Modifier.size(20.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        adhans.forEach { (id, label) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onSelect(id) }
                    .padding(vertical = 8.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (selected == id),
                    onClick = { onSelect(id) },
                    colors = RadioButtonDefaults.colors(selectedColor = Color(0xFF70a080))
                )
                Text(
                    text = label,
                    fontSize = 14.sp,
                    color = if (selected == id) Color(0xFF70a080) else Color(0xFF4A5568),
                    fontWeight = if (selected == id) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
    }
}

@Composable
fun PaletteSelector(selected: String, onSelect: (String) -> Unit) {
    val palettes = listOf(
        "emerald" to Color(0xFF059669),
        "dark_emerald" to Color(0xFF022C22),
        "black" to Color(0xFF1F2937),
        "teal" to Color(0xFF0D9488),
        "indigo" to Color(0xFF4F46E5),
        "gold" to Color(0xFFD97706)
    )

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        items(palettes) { (id, color) ->
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(Color(0xFFF5F5F5), RoundedCornerShape(12.dp))
                    .clickable { onSelect(id) }
                    .padding(4.dp),
                contentAlignment = Alignment.Center
            ) {
                // If selected, draw border around the inner circle
                if (selected == id) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .border(2.dp, Color(0xFF70a080), RoundedCornerShape(10.dp))
                    )
                }
                
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(color, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (selected == id) {
                        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}
