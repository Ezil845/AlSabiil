package al.sabil.screens

import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.unit.sp
import al.sabil.R
import al.sabil.components.MushafPage
import al.sabil.components.QuranIndexModal
import al.sabil.components.TafseerModal
import al.sabil.components.BookmarkManager
import al.sabil.model.Ayah
import al.sabil.repository.QuranRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import androidx.lifecycle.viewmodel.compose.viewModel
import al.sabil.viewmodel.SettingsViewModel
import com.composables.icons.lucide.Lucide
import com.composables.icons.lucide.BookOpen
import com.composables.icons.lucide.Type

@OptIn(ExperimentalMaterial3Api::class, FlowPreview::class)
@Composable
fun MushafScreen(
    repository: QuranRepository, 
    initialPage: Int? = null
) {
    val context = LocalContext.current
    val settingsViewModel: SettingsViewModel = viewModel()
    val userSettings by settingsViewModel.settings.collectAsState()
    val bookmarks by settingsViewModel.bookmarks.collectAsState()

    val isDarkMode = userSettings?.quranDarkMode ?: false
    val backgroundColor = if (isDarkMode) Color.Black else Color(0xFFFFFCF2)
    val topBarColor = if (isDarkMode) Color(0xFF121212) else Color(0xFFf3f6f3)
    val primaryColor = if (isDarkMode) Color(0xFFD4AF37) else Color(0xFF70a080)

    // Ensure system bars are handled
    LaunchedEffect(Unit) {
        val window = (context as? android.app.Activity)?.window ?: return@LaunchedEffect
        WindowCompat.getInsetsController(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    val bookmarkedAyahs = remember(bookmarks) {
        bookmarks.map { Pair(it.surahNumber, it.ayahNumber) }.toSet()
    }

    val totalPages = 604
    val lazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    var showIndex by remember { mutableStateOf(false) }
    var showBookmarks by remember { mutableStateOf(false) }
    var showFontSizeSheet by remember { mutableStateOf(false) }
    var isRestored by remember { mutableStateOf(false) }

    val currentPage by remember {
        derivedStateOf { lazyListState.firstVisibleItemIndex + 1 }
    }

    LaunchedEffect(userSettings, initialPage) {
        if (!isRestored && userSettings != null) {
            val targetPage = initialPage ?: userSettings!!.lastReadPage
            if (targetPage > 1) {
                lazyListState.scrollToItem(targetPage - 1)
            }
            isRestored = true
        }
    }

    LaunchedEffect(lazyListState) {
        snapshotFlow { lazyListState.firstVisibleItemIndex }
            .debounce(500L)
            .collect { index ->
                val page = index + 1
                if (isRestored && userSettings != null && userSettings!!.lastReadPage != page) {
                    settingsViewModel.saveLastReadPage(page)
                    if (userSettings!!.khatmahActive && page > userSettings!!.khatmahCurrentPage) {
                        settingsViewModel.updateKhatmahCurrentPage(page)
                    }
                }
            }
    }

    var selectedAyahForTafseer by remember { mutableStateOf<Ayah?>(null) }
    var currentAyah by remember { mutableStateOf<Ayah?>(null) }

    val surahs = remember(repository) { repository.getAllSurahs() }
    val juzzs = remember(repository) { repository.getAllJuzz() }

    var showKhatmahSheet by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            val surahName = repository.getSurahNameByPage(currentPage)
            val juzz = repository.getJuzzByPage(currentPage)

            Column {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    color = topBarColor,
                    shadowElevation = if (isDarkMode) 0.dp else 2.dp,
                    border = if (isDarkMode) androidx.compose.foundation.BorderStroke(0.5.dp, Color.DarkGray) else null
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.juzz_num, juzz),
                            style = MaterialTheme.typography.bodyMedium,
                            color = primaryColor,
                            fontWeight = FontWeight.SemiBold
                        )
                        
                        Text(
                            text = surahName,
                            style = MaterialTheme.typography.titleMedium,
                            color = primaryColor,
                            fontWeight = FontWeight.Bold
                        )

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { showFontSizeSheet = true },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Lucide.Type,
                                    contentDescription = "Text Size",
                                    tint = primaryColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            IconButton(
                                onClick = { showBookmarks = !showBookmarks },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bookmark,
                                    contentDescription = stringResource(R.string.tab_bookmarks),
                                    tint = primaryColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            IconButton(
                                onClick = { showKhatmahSheet = true },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Lucide.BookOpen,
                                    contentDescription = stringResource(R.string.khatmah_planner),
                                    tint = primaryColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            IconButton(
                                onClick = { showIndex = true },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.List,
                                    contentDescription = stringResource(R.string.index_label),
                                    tint = primaryColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                // Khatmah progress indicator under the top bar
                al.sabil.components.KhatmahProgressBar(
                    userSettings = userSettings,
                    isDarkMode = isDarkMode,
                    onClick = { showKhatmahSheet = true }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = innerPadding.calculateTopPadding())
        ) {
            LazyColumn(
                state = lazyListState,
                modifier = Modifier
                    .fillMaxSize()
                    .then(if (showBookmarks) Modifier.padding(bottom = 220.dp) else Modifier)
            ) {
                items(totalPages) { pageIndex ->
                    val pageNumber = pageIndex + 1
                    val ayahs = repository.getPageData(pageNumber)

                    if (ayahs.isNotEmpty()) {
                        MushafPage(
                            pageNumber = pageNumber,
                            ayahs = ayahs,
                            bookmarkedAyahs = bookmarkedAyahs,
                            isDarkMode = isDarkMode,
                            fontSizeMultiplier = userSettings?.quranFontSizeMultiplier ?: 1.0f,
                            onAyahClick = { ayah ->
                                currentAyah = ayah
                                selectedAyahForTafseer = ayah
                            }
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(600.dp)
                                .background(backgroundColor),
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

            if (showBookmarks) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(220.dp),
                    color = if (isDarkMode) Color(0xFF121212) else Color(0xFFf8faf8),
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

        if (showKhatmahSheet) {
            al.sabil.components.KhatmahBottomSheet(
                userSettings = userSettings,
                settingsViewModel = settingsViewModel,
                isDarkMode = isDarkMode,
                onDismiss = { showKhatmahSheet = false },
                onKhatmahStarted = {
                    coroutineScope.launch {
                        lazyListState.scrollToItem(0) // Scroll to page 1
                    }
                }
            )
        }

        if (showFontSizeSheet) {
            ModalBottomSheet(
                onDismissRequest = { showFontSizeSheet = false },
                containerColor = backgroundColor,
                dragHandle = { BottomSheetDefaults.DragHandle() }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp)
                        .padding(bottom = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Text Size",
                        style = MaterialTheme.typography.titleMedium,
                        color = if (isDarkMode) Color.White else Color.Black,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 24.dp)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("A", fontSize = 14.sp, color = primaryColor)
                        Slider(
                            value = userSettings?.quranFontSizeMultiplier ?: 1.0f,
                            onValueChange = { newValue ->
                                settingsViewModel.updateQuranFontSizeMultiplier(newValue)
                            },
                            valueRange = 0.8f..1.5f,
                            steps = 6,
                            modifier = Modifier.weight(1f).padding(horizontal = 16.dp),
                            colors = SliderDefaults.colors(
                                thumbColor = primaryColor,
                                activeTrackColor = primaryColor,
                                inactiveTrackColor = primaryColor.copy(alpha = 0.3f)
                            )
                        )
                        Text("A", fontSize = 24.sp, color = primaryColor)
                    }
                }
            }
        }
    }
}
