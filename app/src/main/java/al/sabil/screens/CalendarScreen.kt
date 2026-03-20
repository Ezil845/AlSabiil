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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import al.sabil.R
import al.sabil.utils.HijriEvents
import al.sabil.utils.IslamicEventData
import com.composables.icons.lucide.*
import java.time.chrono.HijrahDate
import java.time.format.DateTimeFormatter
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
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = backgroundColor
                )
            )
        },
        containerColor = backgroundColor
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
        ) {
            // Month Navigation
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
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

            Spacer(modifier = Modifier.height(24.dp))

            // Islamic Events
            Text(
                text = stringResource(R.string.islamic_events),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = primaryColor,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            IslamicEventsList(currentMonth)
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
    val daysInMonth = hijriDate.range(ChronoField.DAY_OF_MONTH).maximum.toInt()
    val firstDayOfMonth = hijriDate.with(ChronoField.DAY_OF_MONTH, 1)
    val rawFirstDayOfWeek = firstDayOfMonth.get(ChronoField.DAY_OF_WEEK)
    
    var adjustedFirstDayOfWeek = rawFirstDayOfWeek - hijriOffset
    while (adjustedFirstDayOfWeek > 7) adjustedFirstDayOfWeek -= 7
    while (adjustedFirstDayOfWeek < 1) adjustedFirstDayOfWeek += 7
    
    val emptySlots = if (adjustedFirstDayOfWeek == 7) 0 else adjustedFirstDayOfWeek
    
    val daysOfWeek = listOf(
        stringResource(R.string.day_sun),
        stringResource(R.string.day_mon),
        stringResource(R.string.day_tue),
        stringResource(R.string.day_wed),
        stringResource(R.string.day_thu),
        stringResource(R.string.day_fri),
        stringResource(R.string.day_sat)
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            daysOfWeek.forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

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
                        
                        val highlightColor = when {
                            isToday -> primaryColor
                            hasEvent -> Color(0xFFE9C46A).copy(alpha = 0.3f)
                            else -> Color.Transparent
                        }

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(4.dp)
                                .clip(CircleShape)
                                .background(highlightColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = currentDay.toString(),
                                color = if (isToday) Color.White else if (hasEvent) Color(0xFFB45309) else Color(0xFF1A202C),
                                fontWeight = if (isToday || hasEvent) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 14.sp
                            )
                        }
                        currentDay++
                    }
                }
            }
            if (currentDay > daysInMonth) break
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
fun IslamicEventsList(month: Int) {
    val events = HijriEvents.events.filter { it.month == month }

    if (events.isEmpty()) {
        Box(modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp), contentAlignment = Alignment.Center) {
            Text(stringResource(R.string.no_events_month), color = Color.Gray, fontSize = 14.sp)
        }
    } else {
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(events) { event ->
                EventCard(event)
            }
        }
    }
}

@Composable
fun EventCard(event: IslamicEventData) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val dayText = when {
                    event.dateTextResId != null -> stringResource(event.dateTextResId)
                    event.specificDays != null -> event.specificDays.joinToString(",")
                    event.endDay != null -> "${event.day}-${event.endDay}"
                    else -> event.day.toString()
                }
                Text(
                    text = dayText,
                    fontSize = if (dayText.length > 5) 10.sp else if (dayText.length > 2) 12.sp else 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF70a080),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = getHijriMonthName(event.month),
                    fontSize = 10.sp,
                    color = Color.Gray
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column {
                Text(
                    text = stringResource(event.nameResId),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A202C)
                )
                Text(
                    text = stringResource(event.descResId),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
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
