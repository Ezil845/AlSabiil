package com.example.alsabiil.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alsabiil.R
import com.example.alsabiil.model.Ayah
import com.example.alsabiil.model.Bookmark
import com.example.alsabiil.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarkManager(
    currentPage: Int,
    currentAyah: Ayah?,
    settingsViewModel: SettingsViewModel,
    onNavigateToPage: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var showBookmarkDialog by remember { mutableStateOf(false) }
    var bookmarkName by remember { mutableStateOf("") }
    
    val bookmarks by settingsViewModel.bookmarks.collectAsState()

    if (showBookmarkDialog) {
        AlertDialog(
            onDismissRequest = { showBookmarkDialog = false },
            title = { Text(stringResource(R.string.create_bookmark)) },
            text = {
                Column {
                    Text(stringResource(R.string.page_label, currentPage))
                    if (currentAyah != null) {
                        Text(stringResource(R.string.surah_label, currentAyah.sura_name_ar, currentAyah.sura_no))
                        Text(stringResource(R.string.ayah_label, currentAyah.aya_no))
                    }
                    
                    OutlinedTextField(
                        value = bookmarkName,
                        onValueChange = { bookmarkName = it },
                        label = { Text(stringResource(R.string.bookmark_name_hint)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (currentAyah != null) {
                            val bookmark = Bookmark(
                                surahNumber = currentAyah.sura_no,
                                ayahNumber = currentAyah.aya_no,
                                pageNumber = currentPage,
                                name = bookmarkName
                            )
                            settingsViewModel.saveBookmark(bookmark)
                        }
                        showBookmarkDialog = false
                        bookmarkName = ""
                    }
                ) {
                    Text(stringResource(R.string.save))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { 
                        showBookmarkDialog = false
                        bookmarkName = ""
                    }
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    Column(
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.tab_bookmarks),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF70a080)
            )
            
            IconButton(onClick = { showBookmarkDialog = true }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.add_bookmark),
                    tint = Color(0xFF70a080)
                )
            }
        }
        
        LazyColumn(
            modifier = Modifier.heightIn(max = 200.dp)
        ) {
            items(bookmarks) { bookmark ->
                BookmarkItem(
                    bookmark = bookmark,
                    onClick = { onNavigateToPage(bookmark.pageNumber) },
                    onDelete = { 
                        settingsViewModel.removeBookmark(bookmark.surahNumber, bookmark.ayahNumber)
                    }
                )
            }
        }
    }
}

@Composable
fun BookmarkItem(
    bookmark: Bookmark,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFf8faf8)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = if (bookmark.name.isNotBlank()) bookmark.name else stringResource(R.string.surah_ayah_num, bookmark.surahNumber, bookmark.ayahNumber),
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF333333)
                )
                Text(
                    text = stringResource(R.string.page_num, bookmark.pageNumber),
                    fontSize = 12.sp,
                    color = Color(0xFF777777)
                )
            }
            
            Row {
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.BookmarkBorder,
                        contentDescription = stringResource(R.string.delete_bookmark),
                        tint = Color(0xFF70a080)
                    )
                }
                
                IconButton(onClick = onClick) {
                    Icon(
                        imageVector = Icons.Default.Bookmark,
                        contentDescription = stringResource(R.string.go_to_bookmark),
                        tint = Color(0xFF70a080)
                    )
                }
            }
        }
    }
}