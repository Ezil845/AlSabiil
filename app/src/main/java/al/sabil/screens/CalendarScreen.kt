package al.sabil.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import al.sabil.R
import al.sabil.utils.HijriEvents
import al.sabil.utils.IslamicEventData
import com.composables.icons.lucide.*
import java.time.LocalDate
import java.time.chrono.HijrahDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.ChronoField
import java.time.temporal.ChronoUnit
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    onBack: () -> Unit,
    hijriOffset: Int = 0
) {
    var currentHijriDate by remember { 
        mutableStateOf(HijrahDate.now().plus(hijriOffset.toLong(), ChronoUnit.DAYS)) 
    }
    var showMonthPicker by remember { mutableStateOf(false) }
    
    val currentMonth = currentHijriDate.get(ChronoField.MONTH_OF_YEAR)
    val currentYear = currentHijriDate.get(ChronoField.YEAR)
    
    val backgroundColor = Color(0xFFFFFCF2) // Mushaf Cream
    val primaryColor = Color(0xFF70a080) // Emerald

    if (showMonthPicker) {
        MonthPicker(
            currentMonth = currentMonth,
            onMonthSelected = { month ->
                val diff = month - currentMonth
                currentHijriDate = currentHijriDate.plus(diff.toLong(), ChronoUnit.MONTHS)
                showMonthPicker = false
            },
            onDismiss = { showMonthPicker = false },
            primaryColor = primaryColor
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        stringResource(R.string.hijri_calendar), 
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333)
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Lucide.ChevronLeft, 
                            contentDescription = stringResource(R.string.back_button),
                            tint = Color(0xFF333333)
                        )
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            currentHijriDate = HijrahDate.now().plus(hijriOffset.toLong(), ChronoUnit.DAYS)
                        },
                        shape = RoundedCornerShape(20.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryColor,
                            contentColor = Color.White
                        ),
                        elevation = null,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "Today",
                            fontSize = 12.sp,
                            color = Color.White
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
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 12.dp) // Updated padding from 20 to 12
            ) {
                // Hero card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(primaryColor)
                        .padding(horizontal = 14.dp, vertical = 11.dp) // Updated padding from 16/12 to 14/11
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Left side
                        Column {
                            val gregDate = LocalDate.now()
                            val gregFormatted = gregDate.format(
                                DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ENGLISH)
                            )
                            Text(gregFormatted, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)

                            val dayOfWeek = gregDate.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.ENGLISH)
                            Text(dayOfWeek, fontSize = 11.sp, color = Color.White.copy(alpha = 0.60f))

                            val badgeText = when (currentMonth) {
                                9 -> "Fasting month"
                                1, 7, 11, 12 -> "Sacred month"
                                else -> null
                            }
                            if (badgeText != null) {
                                Spacer(Modifier.height(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(Color.White.copy(alpha = 0.18f))
                                        .padding(horizontal = 10.dp, vertical = 3.dp)
                                ) {
                                    Text(badgeText, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                                }
                            }
                        }

                        // Right side — Hijri date
                        Column(
                            modifier = Modifier.align(Alignment.CenterVertically),
                            horizontalAlignment = Alignment.End
                        ) {
                            Text("Hijri", fontSize = 10.sp, color = Color.White.copy(alpha = 0.5f))
                            Spacer(Modifier.height(3.dp))
                            Text(
                                "${currentHijriDate.get(ChronoField.DAY_OF_MONTH)} ${getHijriMonthName(currentMonth)}",
                                fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White.copy(alpha = 0.85f)
                            )
                            Text(
                                "$currentYear AH",
                                fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White.copy(alpha = 0.85f)
                            )
                        }
                    }
                }
                Spacer(Modifier.height(0.dp)) // Updated gap

                // Month Navigation
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp), // Updated padding from 16 to 8
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { 
                        currentHijriDate = currentHijriDate.minus(1, ChronoUnit.MONTHS) 
                    }) {
                        Icon(Lucide.ChevronLeft, contentDescription = null, tint = primaryColor)
                    }
                    
                    val monthName = getHijriMonthName(currentMonth) + " " + currentYear.toString()
                    Text(
                        text = monthName,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A202C),
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { showMonthPicker = true }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                    
                    IconButton(onClick = { 
                        currentHijriDate = currentHijriDate.plus(1, ChronoUnit.MONTHS) 
                    }) {
                        Icon(Lucide.ChevronRight, contentDescription = null, tint = primaryColor)
                    }
                }

                // Calendar Grid
                CalendarGrid(currentHijriDate, hijriOffset, primaryColor)

                // Legend strip
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, bottom = 4.dp), // Updated padding
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(Modifier.size(10.dp).clip(CircleShape).background(primaryColor))
                    Spacer(Modifier.width(4.dp))
                    Text("Today", fontSize = 11.sp, color = Color.Gray)
                    Spacer(Modifier.width(14.dp))
                    Box(Modifier.size(6.dp).clip(CircleShape).background(Color(0xFFE9C46A)))
                    Spacer(Modifier.width(4.dp))
                    Text("Event", fontSize = 11.sp, color = Color.Gray)
                    Spacer(Modifier.width(14.dp))
                    Text("Fr", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = primaryColor)
                    Spacer(Modifier.width(4.dp))
                    Text("Jumu'ah", fontSize = 11.sp, color = Color.Gray)
                }

                Spacer(modifier = Modifier.height(8.dp)) // Updated from 24 to 8

                // Islamic Events
                Text(
                    text = stringResource(R.string.islamic_events),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = primaryColor,
                    modifier = Modifier.padding(bottom = 6.dp) // Updated from 12 to 6
                )

                IslamicEventsList(currentMonth, primaryColor)
            }
        }
    }
}

@Composable
fun MonthPicker(
    currentMonth: Int,
    onMonthSelected: (Int) -> Unit,
    onDismiss: () -> Unit,
    primaryColor: Color
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {},
        title = {
            Text(
                text = stringResource(R.string.select_month),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                val months = (1..12).chunked(3)
                months.forEach { rowMonths ->
                    Row(modifier = Modifier.fillMaxWidth()) {
                        rowMonths.forEach { month ->
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(4.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (month == currentMonth) primaryColor else Color.Transparent)
                                    .clickable { onMonthSelected(month) }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = getHijriMonthName(month),
                                    fontSize = 11.sp,
                                    fontWeight = if (month == currentMonth) FontWeight.Bold else FontWeight.Normal,
                                    color = if (month == currentMonth) Color.White else Color.Black,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(28.dp)
    )
}

@Composable
fun CalendarGrid(hijriDate: HijrahDate, hijriOffset: Int, primaryColor: Color) {
    val goldColor = Color(0xFFE9C46A)
    val daysInMonth = hijriDate.range(ChronoField.DAY_OF_MONTH).maximum.toInt()
    val firstDayOfMonth = hijriDate.with(ChronoField.DAY_OF_MONTH, 1)
    val rawFirstDayOfWeek = firstDayOfMonth.get(ChronoField.DAY_OF_WEEK)
    
    var adjustedFirstDayOfWeek = rawFirstDayOfWeek - hijriOffset
    while (adjustedFirstDayOfWeek > 7) adjustedFirstDayOfWeek -= 7
    while (adjustedFirstDayOfWeek < 1) adjustedFirstDayOfWeek += 7
    
    val emptySlots = if (adjustedFirstDayOfWeek == 7) 0 else adjustedFirstDayOfWeek
    
    val daysOfWeek = listOf("Su", "Mo", "Tu", "We", "Th", "Fr", "Sa")

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .padding(horizontal = 10.dp, vertical = 8.dp) // Updated from 16 to 10/8
        ) {
            Row(modifier = Modifier.fillMaxWidth()) {
                daysOfWeek.forEachIndexed { index, day ->
                    Text(
                        text = day,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        color = if (index == 5) primaryColor else Color.Gray,
                        fontSize = 14.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp)) // Updated from 12 to 4

            var currentDay = 1
            for (row in 0..5) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    for (col in 0..6) {
                        val index = row * 7 + col
                        if (index < emptySlots || currentDay > daysInMonth) {
                            Spacer(modifier = Modifier.weight(1f))
                        } else {
                            val dateToCheck = hijriDate.with(ChronoField.DAY_OF_MONTH, currentDay.toLong())
                            val isToday = isHijriToday(dateToCheck, hijriOffset)
                            val hasEvent = HijriEvents.getEventForDate(currentDay, hijriDate.get(ChronoField.MONTH_OF_YEAR)) != null
                            val isFriday = dateToCheck.get(ChronoField.DAY_OF_WEEK) == 5

                            val highlightColor = when {
                                isToday -> primaryColor
                                else -> Color.Transparent
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .padding(2.dp) // Updated from 4 to 2
                                    .clip(CircleShape)
                                    .background(highlightColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = currentDay.toString(),
                                        color = when {
                                            isToday -> Color.White
                                            isFriday -> primaryColor
                                            hasEvent -> Color(0xFF92610A)
                                            else -> Color(0xFF1A202C)
                                        },
                                        fontWeight = if (isToday || hasEvent) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 14.sp
                                    )
                                    if (hasEvent && !isToday) {
                                        Box(
                                            modifier = Modifier
                                                .size(4.dp)
                                                .clip(CircleShape)
                                                .background(goldColor)
                                        )
                                    } else {
                                        Spacer(modifier = Modifier.size(4.dp))
                                    }
                                }
                            }
                            currentDay++
                        }
                    }
                }
                if (currentDay > daysInMonth) break
            }
        }
    }
}

fun isHijriToday(date: HijrahDate, offset: Int): Boolean {
    val adjustedToday = HijrahDate.now().plus(offset.toLong(), ChronoUnit.DAYS)
    return date.get(ChronoField.YEAR) == adjustedToday.get(ChronoField.YEAR) &&
           date.get(ChronoField.MONTH_OF_YEAR) == adjustedToday.get(ChronoField.MONTH_OF_YEAR) &&
           date.get(ChronoField.DAY_OF_MONTH) == adjustedToday.get(ChronoField.DAY_OF_MONTH)
}

@Composable
fun IslamicEventsList(month: Int, primaryColor: Color) {
    val events = HijriEvents.events.filter { it.month == month }

    if (events.isEmpty()) {
        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp), contentAlignment = Alignment.Center) {
            Text(stringResource(R.string.no_events_month), color = Color.Gray, fontSize = 14.sp)
        }
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) { // Updated from 8 to 6
            items(events) { event ->
                EventCard(event, primaryColor)
            }
        }
    }
}

@Composable
fun EventCard(event: IslamicEventData, primaryColor: Color) {
    val goldColor = Color(0xFFE9C46A)
    val accentColor = when (event.month) {
        9 -> Color(0xFF378ADD)       // Ramadan → blue
        10 -> primaryColor           // Eid al-Fitr → green
        1, 7, 11 -> goldColor        // Sacred months → gold
        12 -> primaryColor           // Dhul Hijjah → green
        else -> goldColor
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
            ) {
                // Colored left accent strip
                Box(
                    modifier = Modifier
                        .width(4.dp)
                        .fillMaxHeight()
                        .background(accentColor)
                )

                // Card content
                Row(
                    modifier = Modifier.padding(horizontal = 11.dp, vertical = 10.dp), // Updated from 16 to 11/10
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Date column
                    val dayText = when {
                        event.dateTextResId != null -> stringResource(event.dateTextResId)
                        event.specificDays != null -> event.specificDays.joinToString(",")
                        event.endDay != null -> "${event.day}-${event.endDay}"
                        else -> event.day.toString()
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.defaultMinSize(minWidth = 36.dp)
                    ) {
                        Text(
                            text = dayText,
                            fontSize = if (dayText.length > 5) 10.sp else if (dayText.length > 2) 12.sp else 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = primaryColor,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = getHijriMonthName(event.month),
                            fontSize = 10.sp,
                            color = Color.Gray
                        )
                    }

                    // Vertical separator
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .width(0.5.dp)
                            .height(40.dp)
                            .background(Color(0xFFECE8DE))
                    )
                    Spacer(modifier = Modifier.width(8.dp))

                    // Name + description + tag
                    Column {
                        Text(
                            text = stringResource(event.nameResId),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1A202C)
                        )
                        Text(
                            text = stringResource(event.descResId),
                            fontSize = 12.sp,
                            color = Color.Gray
                        )

                        // Tag badge
                        val tagLabel = when (event.month) {
                            9 -> "Fasting"
                            10, 12 -> "Eid"
                            1, 7, 11 -> "Sacred"
                            else -> null
                        }
                        if (tagLabel != null) {
                            Spacer(Modifier.height(4.dp))
                            val tagBg = if (event.month == 9) Color(0xFFEBF4FF) else Color(0xFFE8F5EF)
                            val tagFg = if (event.month == 9) Color(0xFF1A5FA8) else Color(0xFF0F6E56)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(tagBg)
                                    .padding(horizontal = 7.dp, vertical = 2.dp)
                            ) {
                                Text(tagLabel, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = tagFg)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun getHijriMonthName(month: Int): String {
    return when(month) {
        1 -> stringResource(R.string.hijri_month_1)
        2 -> stringResource(R.string.hijri_month_2)
        3 -> stringResource(R.string.hijri_month_3)
        4 -> stringResource(R.string.hijri_month_4)
        5 -> stringResource(R.string.hijri_month_5)
        6 -> stringResource(R.string.hijri_month_6)
        7 -> stringResource(R.string.hijri_month_7)
        8 -> stringResource(R.string.hijri_month_8)
        9 -> stringResource(R.string.hijri_month_9)
        10 -> stringResource(R.string.hijri_month_10)
        11 -> stringResource(R.string.hijri_month_11)
        12 -> stringResource(R.string.hijri_month_12)
        else -> ""
    }
}
