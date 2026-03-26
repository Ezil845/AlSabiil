package al.sabil.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import com.composables.icons.lucide.*
import al.sabil.R
import al.sabil.utils.PrayerTimes
import java.text.SimpleDateFormat
import java.time.chrono.HijrahDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.*
import kotlin.math.absoluteValue

private val ParchmentCream = Color(0xFFF5F0E8)
private val DeepTeal = Color(0xFF1B5B5B)
private val AntiqueGold = Color(0xFFD4AF37)
private val DarkCharcoal = Color(0xFF1A2A2A)
private val OffWhite = Color(0xFFFAF7F0)

@Composable
fun PrayerTimeCard(
    nextPrayer: String,
    countdown: String,
    location: String,
    prayerTimes: PrayerTimes,
    isMuted: Boolean,
    onMuteToggle: () -> Unit,
    onSettingsClick: () -> Unit,
    onCalendarClick: () -> Unit = {},
    hijriOffset: Int = 0,
    showSunrise: Boolean = false,
    isModern: Boolean = true
) {
    if (isModern) {
        ModernPrayerTimeCard(
            nextPrayer = nextPrayer,
            countdown = countdown,
            location = location,
            prayerTimes = prayerTimes,
            isMuted = isMuted,
            onMuteToggle = onMuteToggle,
            onSettingsClick = onSettingsClick,
            onCalendarClick = onCalendarClick,
            hijriOffset = hijriOffset,
            showSunrise = showSunrise
        )
    } else {
        ClassicPrayerTimeCard(
            nextPrayer = nextPrayer,
            countdown = countdown,
            location = location,
            prayerTimes = prayerTimes,
            isMuted = isMuted,
            onMuteToggle = onMuteToggle,
            onSettingsClick = onSettingsClick,
            onCalendarClick = onCalendarClick,
            hijriOffset = hijriOffset,
            showSunrise = showSunrise
        )
    }
}

data class PrayerData(
    val english: String,
    val time: String,
    val arabic: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector
)

fun getArabicPrayerNameForHeader(english: String): String {
    return when(english.lowercase(Locale.ROOT)) {
        "fajr" -> "ٱلْفَجْرُ"
        "sunrise" -> "ٱلشُّرُوقُ"
        "dhuhr" -> "ٱلظُّهْرُ"
        "asr" -> "ٱلْعَصْرُ"
        "maghrib" -> "ٱلْمَغْرِبُ"
        "isha" -> "ٱلْعِشَاءُ"
        else -> "ٱلﺼَّلَاةُ"
    }
}

@Composable
fun ModernPrayerTimeCard(
    nextPrayer: String,
    countdown: String,
    location: String,
    prayerTimes: PrayerTimes,
    isMuted: Boolean,
    onMuteToggle: () -> Unit,
    onSettingsClick: () -> Unit,
    onCalendarClick: () -> Unit,
    hijriOffset: Int,
    showSunrise: Boolean = false
) {
    val DeepTeal = Color(0xFF1B5B5B)
    val AntiqueGold = Color(0xFFD4AF37)
    val OffWhite = Color(0xFFFAF7F0)
    val DarkCharcoal = Color(0xFF1A2A2A)

    val allPrayers = remember(prayerTimes, showSunrise) {
        val list = mutableListOf(
            PrayerData("Fajr", prayerTimes.fajr, "فَجْر", Lucide.Sunrise)
        )
        if (showSunrise) {
            list.add(PrayerData("Sunrise", prayerTimes.sunrise, "شُروق", Lucide.Sun))
        }
        list.addAll(listOf(
            PrayerData("Dhuhr", prayerTimes.dhuhr, "ظُهْر", Lucide.Sun),
            PrayerData("Asr", prayerTimes.asr, "عَصْر", Lucide.CloudSun),
            PrayerData("Maghrib", prayerTimes.maghrib, "مَغْرِب", Lucide.Sunset),
            PrayerData("Isha", prayerTimes.isha, "عِشَاء", Lucide.Moon)
        ))
        list
    }

    val initialPage = remember(allPrayers, nextPrayer) {
        val index = allPrayers.indexOfFirst { it.english.equals(nextPrayer, ignoreCase = true) }
        if (index != -1) index else 0
    }

    val pagerState = rememberPagerState(initialPage = initialPage, pageCount = { allPrayers.size })

    // Auto-scroll to next prayer when it changes
    LaunchedEffect(nextPrayer) {
        val index = allPrayers.indexOfFirst { it.english.equals(nextPrayer, ignoreCase = true) }
        if (index != -1 && index != pagerState.currentPage) {
            pagerState.animateScrollToPage(index)
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = OffWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(modifier = Modifier.padding(12.dp)) {
            // Ornamental Geometric Frame using Canvas
            Canvas(modifier = Modifier.matchParentSize()) {
                val gold = AntiqueGold
                val lightGold = gold.copy(alpha = 0.4f)
                val teal = DeepTeal.copy(alpha = 0.8f)
                
                val strokeOuter = 1.dp.toPx()
                val strokeInner = 1.5.dp.toPx()
                val padding = 6.dp.toPx()
                val cornerRadiusPx = 14.dp.toPx()
                
                // Outer Border
                drawRoundRect(
                    color = lightGold,
                    topLeft = Offset(0f, 0f),
                    size = Size(size.width, size.height),
                    cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx),
                    style = Stroke(width = strokeOuter)
                )
                
                // Inner Border
                drawRoundRect(
                    color = gold,
                    topLeft = Offset(padding, padding),
                    size = Size(size.width - padding * 2, size.height - padding * 2),
                    cornerRadius = CornerRadius(cornerRadiusPx - padding, cornerRadiusPx - padding),
                    style = Stroke(width = strokeInner)
                )

                // 8-pointed star (Rub el Hizb)
                fun drawRubElHizb(center: Offset, starSize: Float, strokeColor: Color) {
                    val half = starSize / 2
                    drawRect(
                        color = strokeColor,
                        topLeft = Offset(center.x - half, center.y - half),
                        size = Size(starSize, starSize),
                        style = Stroke(width = strokeOuter)
                    )
                    withTransform({
                        rotate(45f, center)
                    }) {
                        drawRect(
                            color = strokeColor,
                            topLeft = Offset(center.x - half, center.y - half),
                            size = Size(starSize, starSize),
                            style = Stroke(width = strokeOuter)
                        )
                    }
                    drawCircle(color = strokeColor, radius = starSize / 6, center = center)
                }

                val starSizePx = 18.dp.toPx()
                // Draw Stars on Corners
                drawRubElHizb(Offset(padding, padding), starSizePx, gold)
                drawRubElHizb(Offset(size.width - padding, padding), starSizePx, gold)
                drawRubElHizb(Offset(padding, size.height - padding), starSizePx, gold)
                drawRubElHizb(Offset(size.width - padding, size.height - padding), starSizePx, gold)

                // Top center arch decoration
                val centerX = size.width / 2f
                val archWidth = 60.dp.toPx()
                val archPath = androidx.compose.ui.graphics.Path().apply {
                    moveTo(centerX - archWidth, padding)
                    cubicTo(
                        centerX - archWidth / 2, padding + 15.dp.toPx(),
                        centerX + archWidth / 2, padding + 15.dp.toPx(),
                        centerX + archWidth, padding
                    )
                }
                drawPath(archPath, color = teal, style = Stroke(width = strokeInner))
            }

            // Foreground Content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top Action Icons
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
                        IconButton(onClick = onMuteToggle, modifier = Modifier.size(28.dp)) {
                            Icon(
                                imageVector = if (isMuted) Lucide.BellOff else Lucide.Bell,
                                contentDescription = null,
                                tint = if (isMuted) Color(0xFFFF6464) else AntiqueGold,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        IconButton(onClick = onCalendarClick, modifier = Modifier.size(28.dp)) {
                            Icon(
                                imageVector = Lucide.CalendarDays,
                                contentDescription = "Calendar",
                                tint = AntiqueGold,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        IconButton(onClick = onSettingsClick, modifier = Modifier.size(28.dp)) {
                            Icon(
                                imageVector = Lucide.Settings,
                                contentDescription = stringResource(R.string.settings_title),
                                tint = AntiqueGold,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Text(
                    text = getArabicPrayerNameForHeader(nextPrayer),
                    fontSize = 36.sp,
                    color = DeepTeal,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif
                )
                Text(
                    text = "${nextPrayer.uppercase(java.util.Locale.ROOT)} PRAYER",
                    fontSize = 12.sp,
                    color = AntiqueGold,
                    fontFamily = FontFamily.Serif,
                    letterSpacing = 4.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Location | Hijri Date - Refined layout to prevent overlap and handle BiDi text
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Lucide.MapPin,
                        contentDescription = null,
                        tint = DeepTeal,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    
                    // Clean location: if it contains a space, it might be "Arabic Name French Name", 
                    // we prioritize the first part which is typically Arabic in our service
                    val cleanLocation = location.split(" ").firstOrNull() ?: location
                    
                    Text(
                        text = cleanLocation,
                        style = MaterialTheme.typography.labelSmall,
                        color = DarkCharcoal.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    
                    Text(
                        text = "  |  ",
                        style = MaterialTheme.typography.labelSmall,
                        color = AntiqueGold.copy(alpha = 0.4f),
                        fontWeight = FontWeight.Light
                    )
                    
                    Text(
                        text = getHijriDate(hijriOffset),
                        style = MaterialTheme.typography.labelSmall,
                        color = DarkCharcoal.copy(alpha = 0.7f),
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Next Prayer In",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Serif
                )
                Text(
                    text = countdown,
                    fontSize = 42.sp,
                    color = AntiqueGold,
                    fontWeight = FontWeight.Light,
                    fontFamily = FontFamily.Monospace,
                    style = androidx.compose.ui.text.TextStyle(
                        shadow = androidx.compose.ui.graphics.Shadow(
                            color = AntiqueGold.copy(alpha = 0.2f),
                            offset = Offset(2f, 2f),
                            blurRadius = 4f
                        )
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Swiping prayer cards with HorizontalPager
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxWidth().height(100.dp),
                    contentPadding = PaddingValues(horizontal = 110.dp), // Show prev/next edges
                    pageSpacing = 16.dp,
                    verticalAlignment = Alignment.CenterVertically
                ) { page ->
                    val data = allPrayers[page]
                    val isNext = nextPrayer.equals(data.english, ignoreCase = true)
                    
                    // Animation values based on proximity to center
                    val pageOffset = ((pagerState.currentPage - page) + pagerState.currentPageOffsetFraction).absoluteValue
                    val scale = lerp(0.85f, 1f, 1f - pageOffset.coerceIn(0f, 1f))
                    val alpha = lerp(0.5f, 1f, 1f - pageOffset.coerceIn(0f, 1f))

                    Column(
                        modifier = Modifier
                            .graphicsLayer {
                                scaleX = scale
                                scaleY = scale
                                this.alpha = alpha
                            }
                            .width(80.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isNext) DeepTeal.copy(alpha = 0.05f) else Color.Transparent)
                            .padding(vertical = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = data.icon,
                            contentDescription = data.english,
                            tint = if (isNext) DeepTeal else AntiqueGold,
                            modifier = Modifier.size(if (isNext) 24.dp else 20.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = data.english,
                            fontSize = 10.sp,
                            color = if (isNext) DeepTeal else DarkCharcoal.copy(alpha = 0.6f),
                            fontFamily = FontFamily.Serif,
                            fontWeight = if (isNext) FontWeight.ExtraBold else FontWeight.Medium,
                            maxLines = 1
                        )
                        Text(
                            text = data.time,
                            fontSize = 13.sp,
                            color = if (isNext) DeepTeal else DarkCharcoal,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = if (isNext) FontWeight.Bold else FontWeight.Normal,
                            maxLines = 1
                        )
                        Text(
                            text = data.arabic,
                            fontSize = 12.sp,
                            color = if (isNext) DeepTeal else AntiqueGold.copy(alpha = 0.8f),
                            fontFamily = FontFamily.Serif,
                            maxLines = 1
                        )
                    }
                }
                
                // Pager Indicator dots
                Row(
                    Modifier.height(10.dp).fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    repeat(allPrayers.size) { iteration ->
                        val color = if (pagerState.currentPage == iteration) DeepTeal else DeepTeal.copy(alpha = 0.2f)
                        Box(
                            modifier = Modifier
                                .padding(2.dp)
                                .clip(CircleShape)
                                .background(color)
                                .size(6.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ClassicPrayerTimeCard(
    nextPrayer: String,
    countdown: String,
    location: String,
    prayerTimes: PrayerTimes,
    isMuted: Boolean,
    onMuteToggle: () -> Unit,
    onSettingsClick: () -> Unit,
    onCalendarClick: () -> Unit = {},
    hijriOffset: Int = 0,
    showSunrise: Boolean = false
) {
    val primaryDark = MaterialTheme.colorScheme.primaryContainer

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier.background(primaryDark)
        ) {
            // Header Section
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(primaryDark)
            ) {
                // Content
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
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
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                            Text(
                                text = getHijriDate(hijriOffset),
                                fontSize = 14.sp,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.End
                        ) {
                            CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
                                IconButton(onClick = onMuteToggle, modifier = Modifier.size(32.dp)) {
                                    Icon(
                                        imageVector = if (isMuted) Lucide.BellOff else Lucide.Bell,
                                        contentDescription = null,
                                        tint = if (isMuted) Color(0xFFFF6464) else Color.White.copy(alpha = 0.8f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                IconButton(onClick = onCalendarClick, modifier = Modifier.size(32.dp)) {
                                    Icon(
                                        imageVector = Lucide.CalendarDays,
                                        contentDescription = "Calendar",
                                        tint = Color.White.copy(alpha = 0.8f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                                IconButton(onClick = onSettingsClick, modifier = Modifier.size(32.dp)) {
                                    Icon(
                                        imageVector = Lucide.Settings,
                                        contentDescription = stringResource(R.string.settings_title),
                                        tint = Color.White.copy(alpha = 0.8f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
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
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ClassicPrayerItemView(stringResource(R.string.fajr), prayerTimes.fajr, Lucide.Sunrise, nextPrayer == "Fajr", Modifier.weight(1f))
                if (showSunrise) {
                    ClassicPrayerItemView(stringResource(R.string.sunrise), prayerTimes.sunrise, Lucide.Sun, nextPrayer == "Sunrise", Modifier.weight(1f))
                }
                ClassicPrayerItemView(stringResource(R.string.dhuhr), prayerTimes.dhuhr, Lucide.Sun, nextPrayer == "Dhuhr", Modifier.weight(1f))
                ClassicPrayerItemView(stringResource(R.string.asr), prayerTimes.asr, Lucide.CloudSun, nextPrayer == "Asr", Modifier.weight(1f))
                ClassicPrayerItemView(stringResource(R.string.maghrib), prayerTimes.maghrib, Lucide.Sunset, nextPrayer == "Maghrib", Modifier.weight(1f))
                ClassicPrayerItemView(stringResource(R.string.isha), prayerTimes.isha, Lucide.Moon, nextPrayer == "Isha", Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun ClassicPrayerItemView(
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
        Spacer(modifier = Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .size(36.dp)
                .background(
                    if (isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent,
                    androidx.compose.foundation.shape.CircleShape
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
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = time,
            fontSize = 12.sp,
            color = if (isActive) Color.White else Color.White.copy(alpha = 0.7f),
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun PrayerColumn(
    label: String,
    time: String,
    englishName: String,
    isActive: Boolean
) {
    val bgColor = if (isActive) AntiqueGold.copy(alpha = 0.15f) else Color.Transparent
    val borderColor = if (isActive) DeepTeal.copy(alpha = 0.5f) else Color.Transparent

    Column(
        modifier = Modifier
            .width(60.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Icon
        Icon(
            imageVector = getPrayerIcon(englishName),
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = if (isActive) DeepTeal else DarkCharcoal.copy(alpha = 0.4f)
        )
        Spacer(modifier = Modifier.height(4.dp))
        // English name
        Text(
            text = englishName,
            fontSize = 9.sp,
            color = if (isActive) DeepTeal else DarkCharcoal.copy(alpha = 0.5f),
            fontWeight = FontWeight.Bold
        )
        // Time
        Text(
            text = time,
            fontSize = 12.sp,
            color = if (isActive) DeepTeal else DarkCharcoal,
            fontWeight = FontWeight.ExtraBold
        )
        // Arabic name
        Text(
            text = label,
            fontSize = 10.sp,
            color = if (isActive) DeepTeal else DarkCharcoal.copy(alpha = 0.6f),
            fontWeight = FontWeight.Medium
        )
    }
}

private fun getPrayerIcon(name: String) = when (name) {
    "Fajr" -> Lucide.Sunrise
    "Sunrise" -> Lucide.Sun
    "Dhuhr" -> Lucide.Sun
    "Asr" -> Lucide.CloudSun
    "Maghrib" -> Lucide.Sunset
    "Isha" -> Lucide.Moon
    else -> Lucide.Clock
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

fun getHijriDate(offset: Int = 0): String {
    return try {
        var today = HijrahDate.now()
        if (offset != 0) {
            today = today.plus(offset.toLong(), ChronoUnit.DAYS)
        }
        val formatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("ar")).withDecimalStyle(java.time.format.DecimalStyle.of(Locale.ENGLISH))
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
