package com.example.alsabiil.screens

import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.ui.unit.dp
import com.example.alsabiil.R
import com.example.alsabiil.components.MushafPage
import com.example.alsabiil.components.QuranIndexModal
import com.example.alsabiil.components.TafseerModal
import com.example.alsabiil.components.BookmarkManager
import com.example.alsabiil.model.Ayah
import com.example.alsabiil.repository.QuranRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.alsabiil.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class, FlowPreview::class)
@Composable
fun MushafScreen(repository: QuranRepository, initialPage: Int? = null) {
    val context = LocalContext.current
    val view = LocalView.current
    val settingsViewModel: SettingsViewModel = viewModel()
    val userSettings by settingsViewModel.settings.collectAsState()
    val bookmarks by settingsViewModel.bookmarks.collectAsState()

    // Derive a set of (surahNumber, ayahNumber) for highlight lookup
    val bookmarkedAyahs = remember(bookmarks) {
        bookmarks.map { Pair(it.surahNumber, it.ayahNumber) }.toSet()
    }

    val totalPages = 604

    val lazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var showIndex by remember { mutableStateOf(false) }
    var showBookmarks by remember { mutableStateOf(false) }
    var isRestored by remember { mutableStateOf(false) }

    val currentPage by remember {
        derivedStateOf { lazyListState.firstVisibleItemIndex + 1 }
    }

    // Restore last read page or navigate to initialPage from bookmark
    LaunchedEffect(userSettings, initialPage) {
        if (!isRestored && userSettings != null) {
            val targetPage = initialPage ?: userSettings!!.lastReadPage
            if (targetPage > 1) {
                lazyListState.scrollToItem(targetPage - 1)
            }
            isRestored = true
        }
    }

    // Save last read page (debounced to avoid excessive writes during scrolling)
    LaunchedEffect(lazyListState) {
        snapshotFlow { lazyListState.firstVisibleItemIndex }
            .debounce(500L)
            .collect { index ->
                val page = index + 1
                if (isRestored && userSettings != null && userSettings!!.lastReadPage != page) {
                    settingsViewModel.saveLastReadPage(page)
                }
            }
    }

    var selectedAyahForTafseer by remember { mutableStateOf<Ayah?>(null) }
    var currentAyah by remember { mutableStateOf<Ayah?>(null) }

    val surahs = remember(repository) { repository.getAllSurahs() }
    val juzzs = remember(repository) { repository.getAllJuzz() }



    Scaffold(
        containerColor = Color(0xFFFFFCF2), // Mushaf background color
        topBar = {
            val surahName = repository.getSurahNameByPage(currentPage)
            val juzz = repository.getJuzzByPage(currentPage)

            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(end = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.juzz_num, juzz),
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFF70a080),
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = surahName,
                            style = MaterialTheme.typography.titleLarge,
                            color = Color(0xFF70a080),
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showBookmarks = !showBookmarks }) {
                        Icon(
                            imageVector = Icons.Default.Bookmark,
                            contentDescription = stringResource(R.string.tab_bookmarks),
                            tint = Color(0xFF70a080)
                        )
                    }
                    IconButton(onClick = { showIndex = true }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.List,
                            contentDescription = stringResource(R.string.index_label),
                            tint = Color(0xFF70a080)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFf3f6f3),
                    titleContentColor = Color(0xFF70a080)
                )
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                state = lazyListState,
                modifier = Modifier
                    .fillMaxSize()
                    .then(if (showBookmarks) Modifier.padding(bottom = 220.dp) else Modifier) // Make space for bookmarks panel
            ) {
                items(totalPages) { pageIndex ->
                    val pageNumber = pageIndex + 1
                    val ayahs = repository.getPageData(pageNumber)

                    if (ayahs.isNotEmpty()) {
                        MushafPage(
                            pageNumber = pageNumber,
                            ayahs = ayahs,
                            bookmarkedAyahs = bookmarkedAyahs,
                            onAyahClick = { ayah ->
                                currentAyah = ayah
                                selectedAyahForTafseer = ayah
                            }
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(600.dp) // Approximate page height
                                .background(Color(0xFFFFFCF2)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = stringResource(R.string.page_simple_label, pageNumber),
                                color = Color.Gray
                            )
                        }
                    }
                }
            }

            // Bookmarks Panel - appears at the bottom when toggled
            if (showBookmarks) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(220.dp),
                    color = Color(0xFFf8faf8),
                    shadowElevation = 8.dp
                ) {
                    BookmarkManager(
                        currentPage = currentPage,
                        currentAyah = currentAyah,
                        settingsViewModel = settingsViewModel,
                        onNavigateToPage = { page ->
                            coroutineScope.launch {
                                lazyListState.scrollToItem(page - 1)
                                showBookmarks = false
                            }
                        },
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(8.dp)
                    )
                }
            }
        }

        if (showIndex) {
            QuranIndexModal(
                surahs = surahs,
                juzzs = juzzs,
                settingsViewModel = settingsViewModel,
                onSelectPage = { page ->
                    coroutineScope.launch {
                        lazyListState.scrollToItem(page - 1)
                    }
                },
                onDismiss = { showIndex = false }
            )
        }

        selectedAyahForTafseer?.let { ayah ->
            val tafseerType = userSettings?.selectedTafseer ?: "saddi"
            val tafseer = repository.getTafseer(ayah.sura_no, ayah.aya_no, tafseerType)
            TafseerModal(
                ayah = ayah,
                tafseerText = tafseer,
                tafseerType = tafseerType,
                onDismiss = { selectedAyahForTafseer = null }
            )
        }
    }
}
