package com.example.alsabiil

import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.activity.result.contract.ActivityResultContracts
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.example.alsabiil.components.PrayerTimeCard
import com.example.alsabiil.components.AyahCard
import com.example.alsabiil.screens.WelcomeScreen
import com.example.alsabiil.screens.MushafScreen
import com.example.alsabiil.screens.AzkarScreen
import com.example.alsabiil.screens.QiblaScreen
import com.example.alsabiil.repository.QuranRepository
import com.example.alsabiil.repository.AzkarRepository
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.alsabiil.ui.theme.AlSabiilTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.FormatAlignRight
import androidx.compose.material.icons.filled.Explore
import com.composables.icons.lucide.*
import android.os.Build
import com.example.alsabiil.screens.SettingsScreen
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import java.util.Locale

sealed class Screen(val route: String, val resourceId: Int, val icon: @Composable (Boolean) -> Unit) {
    object Welcome : Screen("welcome", R.string.app_name, { isSelected -> 
        Icon(Lucide.House, contentDescription = null, tint = if (isSelected) Color(0xFF70a080) else Color(0xFFa0b0a0)) 
    })
    object Quran : Screen("quran", R.string.app_name, { isSelected -> 
        Icon(Lucide.BookOpen, contentDescription = null, tint = if (isSelected) Color(0xFF70a080) else Color(0xFFa0b0a0)) 
    })
    object Azkar : Screen("azkar", R.string.morning_azkar, { isSelected -> 
        Icon(Lucide.LayoutList, contentDescription = null, tint = if (isSelected) Color(0xFF70a080) else Color(0xFFa0b0a0)) 
    })
    object Qibla : Screen("qibla", R.string.qibla_direction, { isSelected -> 
        Icon(Lucide.Compass, contentDescription = null, tint = if (isSelected) Color(0xFF70a080) else Color(0xFFa0b0a0)) 
    })
    object SettingsTab : Screen("settings", R.string.settings, { isSelected -> 
        Icon(Lucide.Settings, contentDescription = null, tint = if (isSelected) Color(0xFF70a080) else Color(0xFFa0b0a0)) 
    })
    object Bookmarks : Screen("bookmarks", R.string.tab_bookmarks, { _ -> })
}

class MainActivity : ComponentActivity() {
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        // Handle permissions if needed
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Ensure Arabic Locale is applied
        AlSabiilApp.forceArabicLocale(this)

        // Request permissions
        val permissions = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
            permissions.add(Manifest.permission.USE_FULL_SCREEN_INTENT)
        }
        
        if (permissions.any { ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED }) {
            requestPermissionLauncher.launch(permissions.toTypedArray())
        }

        enableEdgeToEdge()
        
        // Globally hide system bars for immersive experience
        WindowCompat.getInsetsController(window, window.decorView).apply {
            hide(WindowInsetsCompat.Type.systemBars())
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
        
        setContent {
            val settingsViewModel: com.example.alsabiil.viewmodel.SettingsViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
            val userSettings by settingsViewModel.settings.collectAsState()
            val currentPalette = com.example.alsabiil.ui.theme.AppPalette.fromId(userSettings?.selectedPalette ?: "emerald")

            AlSabiilTheme(palette = currentPalette) {
                val navController = rememberNavController()
                val context = LocalContext.current
                val quranRepository = remember { QuranRepository(context) }
                val azkarRepository = remember { AzkarRepository(context) }
                val items = listOf(
                    Screen.Welcome,
                    Screen.Quran,
                    Screen.Azkar,
                    Screen.Qibla
                )
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = Color(0xFFFFFCF2), // Mushaf-like background globally
                    bottomBar = {
                        val navBackStackEntry by navController.currentBackStackEntryAsState()
                        val currentDestination = navBackStackEntry?.destination
                        
                        // Hide bottom bar on Quran screen for immersive reading
                        val isQuranScreen = currentDestination?.route == Screen.Quran.route
                        val showBottomBar = items.any { it.route == currentDestination?.route } && !isQuranScreen
                        
                        if (showBottomBar) {
                            NavigationBar(
                                containerColor = Color(0xFFFFFCF2), // Warm Cream
                                tonalElevation = 0.dp,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(64.dp) // Tighter height without labels
                                    .border(
                                        width = 1.dp,
                                        color = Color(0xFFd8e2d8), // Top border color
                                        shape = androidx.compose.ui.graphics.RectangleShape
                                    ),
                                windowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0)
                            ) {
                                items.forEach { screen ->
                                    val isSelected = currentDestination?.hierarchy?.any { it.route == screen.route } == true
                                    NavigationBarItem(
                                        icon = { screen.icon(isSelected) },
                                        selected = isSelected,
                                        alwaysShowLabel = false,
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = Color(0xFF70a080), // Sage Green
                                            unselectedIconColor = Color(0xFFa0b0a0), // Muted Sage
                                            indicatorColor = Color.Transparent // No pill background
                                        ),
                                        onClick = {
                                            navController.navigate(screen.route) {
                                                popUpTo(navController.graph.findStartDestination().id) {
                                                    saveState = true
                                                }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    NavHost(navController, startDestination = Screen.Welcome.route, modifier = Modifier.padding(innerPadding)) {
                        composable(Screen.Welcome.route) { 
                            WelcomeScreen(onSettingsClick = { navController.navigate(Screen.SettingsTab.route) }) 
                        }
                        composable(Screen.Quran.route) { 
                            MushafScreen(quranRepository) 
                        }
                        composable("quran/{page}") { backStackEntry ->
                            val page = backStackEntry.arguments?.getString("page")?.toIntOrNull()
                            MushafScreen(quranRepository, initialPage = page)
                        }
                        composable(Screen.Azkar.route) { AzkarScreen(azkarRepository) }
                        composable(Screen.Qibla.route) { QiblaScreen() }
                        composable(Screen.SettingsTab.route) { 
                            SettingsScreen(
                                onBack = { navController.popBackStack() },
                                onManageBookmarks = { navController.navigate(Screen.Bookmarks.route) }
                            ) 
                        }
                        composable(Screen.Bookmarks.route) {
                            com.example.alsabiil.screens.BookmarksScreen(
                                onBack = { navController.popBackStack() },
                                onNavigateToPage = { page ->
                                    navController.navigate("quran/$page") {
                                        popUpTo(Screen.Welcome.route)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
