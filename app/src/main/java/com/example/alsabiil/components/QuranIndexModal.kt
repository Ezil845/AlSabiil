package com.example.alsabiil.components

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alsabiil.model.JuzzInfo
import com.example.alsabiil.model.SurahInfo
import com.example.alsabiil.ui.theme.HafsSmart
import com.example.alsabiil.model.Bookmark
import com.example.alsabiil.viewmodel.SettingsViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.res.stringResource
import com.example.alsabiil.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranIndexModal(
    surahs: List<SurahInfo>,
    juzzs: List<JuzzInfo>,
    settingsViewModel: SettingsViewModel? = null,
    onSelectPage: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color(0xFFFFFCF2), // Match Parchment theme
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        var selectedTabIndex by remember { mutableStateOf(0) }
        val tabs = listOf(
            stringResource(R.string.tab_surah), 
            stringResource(R.string.tab_juzz), 
            stringResource(R.string.tab_bookmarks)
        )

        val bookmarks by settingsViewModel?.bookmarks?.collectAsState() ?:
            produceState(initialValue = emptyList<Bookmark>()) { value = emptyList() }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 16.dp)
        ) {
            // Header with Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Custom Tab Switcher (Pill style) to fix text clipping issues
                Row(
                    modifier = Modifier
                        .width(280.dp)
                        .height(48.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFFE0E8E0))
                        .padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    tabs.forEachIndexed { index, title ->
                        val isSelected = selectedTabIndex == index
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isSelected) Color(0xFF70A080) else Color.Transparent)
                                .clickable { selectedTabIndex = index },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = title,
                                color = if (isSelected) Color.White else Color(0xFF557560),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // List Content
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 24.dp)
            ) {
                when (selectedTabIndex) {
                    0 -> {
                        items(surahs) { surah ->
                            SurahItem(surah = surah, onClick = {
                                onSelectPage(surah.startPage)
                                onDismiss()
                            })
                        }
                    }
                    1 -> {
                        items(juzzs) { juzz ->
                            JuzzItem(juzz = juzz, onClick = {
                                onSelectPage(juzz.startPage)
                                onDismiss()
                            })
                        }
                    }
                    2 -> {
                        items(bookmarks) { bookmark ->
                            BookmarkItem(bookmark = bookmark, onClick = {
                                onSelectPage(bookmark.pageNumber)
                                onDismiss()
                            })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SurahItem(surah: SurahInfo, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .background(Color(0xFFE0E8E0), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = surah.number.toString(),
                color = Color(0xFF70A080),
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = surah.nameAr,
                fontFamily = HafsSmart,
                fontSize = 20.sp,
                color = Color.Black,
                textAlign = TextAlign.Start
            )
            Text(
                text = surah.nameEn,
                fontSize = 14.sp,
                color = Color(0xFF557560)
            )
        }

        Text(
            text = stringResource(R.string.page_num, surah.startPage),
            fontSize = 12.sp,
            color = Color(0xFFA0B0A0)
        )
    }
    HorizontalDivider(color = Color(0xFFE0E8E0))
}

@Composable
fun JuzzItem(juzz: JuzzInfo, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .background(Color(0xFFE0E8E0), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = juzz.number.toString(),
                color = Color(0xFF70A080),
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            modifier = Modifier.weight(1f),
            text = stringResource(R.string.juzz_num, juzz.number),
            fontSize = 18.sp,
            color = Color.Black
        )

        Text(
            text = stringResource(R.string.page_num, juzz.startPage),
            fontSize = 12.sp,
            color = Color(0xFFA0B0A0)
        )
    }
    HorizontalDivider(color = Color(0xFFE0E8E0))
}

@Composable
fun BookmarkItem(bookmark: Bookmark, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .background(Color(0xFFE0E8E0), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${bookmark.surahNumber}:${bookmark.ayahNumber}",
                color = Color(0xFF70A080),
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (bookmark.name.isNotBlank()) bookmark.name else stringResource(R.string.surah_ayah_num, bookmark.surahNumber, bookmark.ayahNumber),
                fontSize = 16.sp,
                color = Color.Black
            )
            Text(
                text = stringResource(R.string.page_num, bookmark.pageNumber),
                fontSize = 12.sp,
                color = Color(0xFF557560)
            )
        }
    }
    HorizontalDivider(color = Color(0xFFE0E8E0))
}
