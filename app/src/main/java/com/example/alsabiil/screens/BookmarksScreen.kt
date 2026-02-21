package com.example.alsabiil.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.alsabiil.R
import com.example.alsabiil.components.BookmarkManager
import com.example.alsabiil.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarksScreen(
    onBack: () -> Unit,
    onNavigateToPage: (Int) -> Unit,
    viewModel: SettingsViewModel = viewModel()
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        stringResource(R.string.tab_bookmarks), 
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
                    containerColor = Color.White
                )
            )
        },
        containerColor = Color.White
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color.White)
        ) {
            BookmarkManager(
                currentPage = 1, // Not relevant for just managing
                currentAyah = null, // Not relevant for just managing
                settingsViewModel = viewModel,
                onNavigateToPage = onNavigateToPage,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
