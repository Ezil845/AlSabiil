package al.sabil.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import al.sabil.R
import al.sabil.data.UserSettings
import al.sabil.viewmodel.SettingsViewModel
import com.composables.icons.lucide.*
import java.util.concurrent.TimeUnit

/**
 * A thin Khatmah progress indicator that sits under the MushafScreen top bar.
 * Tapping it expands a bottom sheet with full details.
 */
@Composable
fun KhatmahProgressBar(
    userSettings: UserSettings?,
    isDarkMode: Boolean,
    onClick: () -> Unit
) {
    if (userSettings == null || !userSettings.khatmahActive) return

    val totalPages = 604
    val currentPage = userSettings.khatmahCurrentPage
    val progressPercent = (currentPage.toFloat() / totalPages.toFloat()).coerceIn(0f, 1f)
    val primaryColor = if (isDarkMode) Color(0xFFD4AF37) else Color(0xFF70a080)

    val targetDays = userSettings.khatmahTargetDays
    val startMillis = userSettings.khatmahStartMillis
    val daysElapsed = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - startMillis).toInt()
    val exactExpectedPage = ((daysElapsed + 1) * (totalPages.toDouble() / targetDays)).toInt().coerceIn(1, totalPages)
    val pagesLeftToday = maxOf(0, exactExpectedPage - currentPage)
    val isOnTrack = pagesLeftToday == 0

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .background(if (isDarkMode) Color(0xFF1a1a1a) else Color(0xFFF5F5F0))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Lucide.BookOpen,
                    contentDescription = null,
                    tint = primaryColor,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.khatmah_planner),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDarkMode) Color.LightGray else Color(0xFF4A5568)
                )
            }
            Text(
                text = if (isOnTrack) stringResource(R.string.khatmah_done) 
                       else stringResource(R.string.khatmah_pages_left, pagesLeftToday),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isOnTrack) primaryColor else Color(0xFFE53E3E)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        LinearProgressIndicator(
            progress = { progressPercent },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = primaryColor,
            trackColor = primaryColor.copy(alpha = 0.15f),
        )
    }
}

/**
 * Full Khatmah bottom sheet for setup and detailed tracking.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KhatmahBottomSheet(
    userSettings: UserSettings?,
    settingsViewModel: SettingsViewModel,
    isDarkMode: Boolean,
    onDismiss: () -> Unit,
    onKhatmahStarted: () -> Unit = {}
) {
    if (userSettings == null) return

    val isActive = userSettings.khatmahActive
    val primaryColor = if (isDarkMode) Color(0xFFD4AF37) else Color(0xFF70a080)
    val totalPages = 604
    val sheetBg = if (isDarkMode) Color(0xFF1E1E1E) else Color.White
    val textColor = if (isDarkMode) Color.White else Color(0xFF1A202C)
    val subtextColor = if (isDarkMode) Color.LightGray else Color(0xFF4A5568)

    var showConfirmCancel by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = sheetBg,
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 8.dp)
                .padding(bottom = 32.dp)
        ) {
            // Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Lucide.BookOpen,
                    contentDescription = null,
                    tint = primaryColor,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.khatmah_planner),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor
                )
                Spacer(modifier = Modifier.weight(1f))
                if (isActive) {
                    TextButton(onClick = { showConfirmCancel = true }) {
                        Text(
                            text = stringResource(R.string.khatmah_cancel),
                            color = Color(0xFFE53E3E),
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (!isActive) {
                // Setup mode
                var targetDays by remember { mutableStateOf(30f) }

                Text(
                    text = stringResource(R.string.khatmah_set_goal),
                    color = subtextColor,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.khatmah_target),
                        fontWeight = FontWeight.Bold,
                        color = subtextColor
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.khatmah_days, targetDays.toInt()),
                        color = primaryColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                Slider(
                    value = targetDays,
                    onValueChange = { targetDays = it },
                    valueRange = 10f..30f,
                    steps = 19,
                    colors = SliderDefaults.colors(
                        thumbColor = primaryColor,
                        activeTrackColor = primaryColor
                    )
                )

                val pagesPerDay = Math.ceil(totalPages.toDouble() / targetDays.toInt()).toInt()
                Text(
                    text = stringResource(R.string.khatmah_pages_per_day, pagesPerDay),
                    color = Color.Gray,
                    fontSize = 12.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Reminder toggle
                var reminderEnabled by remember { mutableStateOf(false) }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.khatmah_reminder),
                            fontWeight = FontWeight.SemiBold,
                            color = textColor,
                            fontSize = 14.sp
                        )
                        Text(
                            text = stringResource(R.string.khatmah_reminder_desc),
                            color = Color.Gray,
                            fontSize = 11.sp
                        )
                    }
                    Switch(
                        checked = reminderEnabled,
                        onCheckedChange = { reminderEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = primaryColor
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        settingsViewModel.startKhatmah(
                            targetDays = targetDays.toInt(),
                            startMillis = System.currentTimeMillis(),
                            currentPage = 1
                        )
                        settingsViewModel.toggleKhatmahReminder(reminderEnabled)
                        onKhatmahStarted()
                        onDismiss()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryColor),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(stringResource(R.string.khatmah_start), color = Color.White, fontWeight = FontWeight.Bold)
                }
            } else {
                // Active tracking mode
                val targetDays = userSettings.khatmahTargetDays
                val startMillis = userSettings.khatmahStartMillis
                val currentPage = userSettings.khatmahCurrentPage
                val daysElapsed = TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - startMillis).toInt()
                val exactExpectedPage = ((daysElapsed + 1) * (totalPages.toDouble() / targetDays)).toInt().coerceIn(1, totalPages)
                val pagesLeftToday = maxOf(0, exactExpectedPage - currentPage)
                val progressPercent = (currentPage.toFloat() / totalPages.toFloat()).coerceIn(0f, 1f)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.khatmah_today_target),
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = stringResource(R.string.khatmah_page_num, exactExpectedPage),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = stringResource(R.string.khatmah_remaining),
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                        Text(
                            text = if (pagesLeftToday == 0) stringResource(R.string.khatmah_done)
                                   else stringResource(R.string.khatmah_pages_left, pagesLeftToday),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (pagesLeftToday == 0) primaryColor else Color(0xFFE53E3E)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = stringResource(R.string.khatmah_total_progress),
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "${(progressPercent * 100).toInt()}%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryColor
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                LinearProgressIndicator(
                    progress = { progressPercent },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = primaryColor,
                    trackColor = primaryColor.copy(alpha = 0.2f),
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Reminder toggle in active mode
                val reminderEnabled = userSettings.khatmahReminderEnabled
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.khatmah_reminder),
                            fontWeight = FontWeight.SemiBold,
                            color = textColor,
                            fontSize = 14.sp
                        )
                        Text(
                            text = stringResource(R.string.khatmah_reminder_desc),
                            color = Color.Gray,
                            fontSize = 11.sp
                        )
                    }
                    Switch(
                        checked = reminderEnabled,
                        onCheckedChange = { settingsViewModel.toggleKhatmahReminder(it) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = primaryColor
                        )
                    )
                }
            }
        }
    }

    if (showConfirmCancel) {
        AlertDialog(
            onDismissRequest = { showConfirmCancel = false },
            title = {
                Text(
                    text = stringResource(R.string.khatmah_cancel),
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkMode) Color.White else Color(0xFF1A202C)
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.khatmah_cancel_confirm),
                    color = if (isDarkMode) Color.LightGray else Color(0xFF4A5568)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    settingsViewModel.stopKhatmah()
                    showConfirmCancel = false
                    onDismiss()
                }) {
                    Text(
                        text = stringResource(R.string.khatmah_cancel),
                        color = Color(0xFFE53E3E),
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmCancel = false }) {
                    Text(
                        text = stringResource(R.string.cancel),
                        color = Color.Gray
                    )
                }
            },
            containerColor = if (isDarkMode) Color(0xFF2A2A2A) else Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }
}
