package com.example.alsabiil.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.composables.icons.lucide.*
import com.example.alsabiil.R
import com.example.alsabiil.utils.PrayerTimes
import java.text.SimpleDateFormat
import java.time.chrono.HijrahDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.math.roundToInt

@Composable
fun PrayerTimeCard(
    nextPrayer: String,
    countdown: String,
    location: String,
    prayerTimes: PrayerTimes,
    isMuted: Boolean,
    onMuteToggle: () -> Unit,
    onSettingsClick: () -> Unit,
    hijriOffset: Int = 0
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val primaryDark = MaterialTheme.colorScheme.primaryContainer
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .background(primaryDark) // Bottom background
        ) {
            // Header Section with Gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(primaryColor, primaryDark)
                        )
                    )
                    .padding(20.dp)
            ) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            Text(
                                text = getPrayerName(nextPrayer),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = location,
                                fontSize = 14.sp,
                                color = Color.White.copy(alpha = 0.8f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.fillMaxWidth(0.7f) // Reserve space for buttons
                            )
                            Text(
                                text = getHijriDate(hijriOffset),
                                fontSize = 14.sp,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = onMuteToggle) {
                                Icon(
                                    imageVector = if (isMuted) Lucide.VolumeX else Lucide.Volume2,
                                    contentDescription = null,
                                    tint = if (isMuted) Color(0xFFFF6464) else Color.White.copy(alpha = 0.8f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            IconButton(onClick = onSettingsClick) {
                                Icon(
                                    imageVector = Lucide.Settings,
                                    contentDescription = stringResource(R.string.settings_title),
                                    tint = Color.White.copy(alpha = 0.8f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Countdown
                    Text(
                        text = countdown,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Footer Section - Prayer List
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp, horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween // Changed to SpaceBetween to work well with weights if needed, or keeping default
            ) {
                // Apply weight to each item so they share width equally
                PrayerItemView(stringResource(R.string.fajr), prayerTimes.fajr, Lucide.Sunrise, nextPrayer == "Fajr", Modifier.weight(1f))
                PrayerItemView(stringResource(R.string.dhuhr), prayerTimes.dhuhr, Lucide.Sun, nextPrayer == "Dhuhr", Modifier.weight(1f))
                PrayerItemView(stringResource(R.string.asr), prayerTimes.asr, Lucide.CloudSun, nextPrayer == "Asr", Modifier.weight(1f))
                PrayerItemView(stringResource(R.string.maghrib), prayerTimes.maghrib, Lucide.Sunset, nextPrayer == "Maghrib", Modifier.weight(1f))
                PrayerItemView(stringResource(R.string.isha), prayerTimes.isha, Lucide.Moon, nextPrayer == "Isha", Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun getPrayerName(key: String): String {
    return when (key) {
        "Fajr" -> stringResource(R.string.fajr)
        "Sunrise" -> stringResource(R.string.sunrise)
        "Dhuhr" -> stringResource(R.string.dhuhr)
        "Asr" -> stringResource(R.string.asr)
        "Maghrib" -> stringResource(R.string.maghrib)
        "Isha" -> stringResource(R.string.isha)
        else -> key
    }
}

@Composable
fun PrayerItemView(
    label: String, 
    time: String, 
    icon: androidx.compose.ui.graphics.vector.ImageVector, 
    isActive: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.padding(vertical = 2.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = if (isActive) Color.White else Color.White.copy(alpha = 0.6f),
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(12.dp)) // Increased spacing
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(
                    if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent,
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = if (isActive) Color.White else Color.White.copy(alpha = 0.6f)
            )
        }
        Spacer(modifier = Modifier.height(12.dp)) // Increased spacing
        Text(
            text = time,
            fontSize = 12.sp,
            color = if (isActive) Color.White else Color.White.copy(alpha = 0.7f),
            fontWeight = FontWeight.SemiBold
        )
    }
}

fun getHijriDate(offset: Int = 0): String {
    return try {
        var today = HijrahDate.now()
        if (offset != 0) {
            today = today.plus(offset.toLong(), ChronoUnit.DAYS)
        }
        val formatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("ar"))
        today.format(formatter)
    } catch (e: Exception) {
        val calendar = Calendar.getInstance()
        if (offset != 0) {
            calendar.add(Calendar.DAY_OF_YEAR, offset)
        }
        val sdf = SimpleDateFormat("d MMMM yyyy", Locale("ar"))
        sdf.format(calendar.time)
    }
}
