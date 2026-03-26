package al.sabil.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import al.sabil.R
import al.sabil.data.SettingsManager
import al.sabil.ui.theme.AlSabiilTheme
import al.sabil.ui.theme.AppPalette
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PrayerAlertActivity : ComponentActivity() {

    private val timeHandler = Handler(Looper.getMainLooper())
    private var timeRunnable: Runnable? = null
    
    // Receiver to finish activity when stop action is clicked from notification
    private val finishReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            finish()
        }
    }

    override fun attachBaseContext(newBase: Context?) {
        val base = newBase ?: return super.attachBaseContext(newBase)
        val locale = java.util.Locale("ar")
        java.util.Locale.setDefault(locale)
        val config = Configuration(base.resources.configuration)
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        super.attachBaseContext(base.createConfigurationContext(config))
    }

    override fun onDestroy() {
        super.onDestroy()
        NotificationHelper.stopAdhanSound()
        timeRunnable?.let { timeHandler.removeCallbacks(it) }
        try {
            unregisterReceiver(finishReceiver)
        } catch (e: Exception) {
            // Receiver might not be registered
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Register receiver
        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                 registerReceiver(finishReceiver, IntentFilter("al.sabil.ACTION_STOP_SOUND"), Context.RECEIVER_NOT_EXPORTED)
            } else {
                 registerReceiver(finishReceiver, IntentFilter("al.sabil.ACTION_STOP_SOUND"))
            }
        } catch (e: Exception) {
            // Log error
        }

        // Ensure it shows even if phone is locked/screen is off
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
            val keyguardManager = getSystemService(android.content.Context.KEYGUARD_SERVICE) as android.app.KeyguardManager
            keyguardManager.requestDismissKeyguard(this, null)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                android.view.WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
                android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
                android.view.WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }

        val settingsManager = SettingsManager(this)

        setContent {
            // Hide status and navigation bars for immersive experience
            val view = androidx.compose.ui.platform.LocalView.current
            if (!view.isInEditMode) {
                SideEffect {
                    val window = (view.context as android.app.Activity).window
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                        window.setDecorFitsSystemWindows(false)
                        val controller = window.insetsController
                        if (controller != null) {
                            controller.hide(android.view.WindowInsets.Type.statusBars() or android.view.WindowInsets.Type.navigationBars())
                            controller.systemBarsBehavior = android.view.WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                        }
                    } else {
                        @Suppress("DEPRECATION")
                        window.decorView.systemUiVisibility = (
                            android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            or android.view.View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            or android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            or android.view.View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            or android.view.View.SYSTEM_UI_FLAG_FULLSCREEN
                            or android.view.View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        )
                    }
                }
            }

            val settings by settingsManager.settingsFlow.collectAsState(initial = null)
            val isModern = settings?.appThemeStyle == "MODERN"

            AlSabiilTheme(palette = AppPalette.fromId(settings?.selectedPalette ?: "emerald")) {
                if (isModern) {
                    ModernPrayerAlertScreen(
                        title = intent.getStringExtra("title") ?: stringResource(R.string.notification_prayer_title, ""),
                        body = intent.getStringExtra("body") ?: stringResource(R.string.notification_prayer_body, ""),
                        onStop = {
                            NotificationHelper.stopAdhanSound()
                            finish()
                        }
                    )
                } else {
                    ClassicPrayerAlertScreen(
                        title = intent.getStringExtra("title") ?: stringResource(R.string.notification_prayer_title, ""),
                        body = intent.getStringExtra("body") ?: stringResource(R.string.notification_prayer_body, ""),
                        onStop = {
                            NotificationHelper.stopAdhanSound()
                            finish()
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun ClassicPrayerAlertScreen(
    title: String,
    body: String,
    onStop: () -> Unit
) {
    var currentTime by remember { mutableStateOf("") }
    
    LaunchedEffect(Unit) {
        val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        while(true) {
            currentTime = dateFormat.format(Date())
            kotlinx.coroutines.delay(1000)
        }
    }

    // Animation for the pulsing circle
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "alpha"
    )

    // Emerald Gradient Background (Matching app palette)
    val emerald900 = Color(0xFF064E3B)
    val emerald950 = Color(0xFF022C22)
    val primaryEmerald = Color(0xFF059669)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        emerald900,
                        emerald950,
                        Color.Black
                    )
                )
            )
    ) {
        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp, vertical = 64.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top: Current Time
            Text(
                text = currentTime,
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 90.sp,
                    fontWeight = FontWeight.Thin,
                    letterSpacing = (-3).sp
                ),
                color = Color.White.copy(alpha = 0.95f)
            )

            // Center: Prayer Info
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Pulsing Icon with Emerald Theme
                Box(contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .scale(scale)
                            .background(primaryEmerald.copy(alpha = 0.15f), CircleShape)
                    )
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .scale(scale * 1.3f)
                            .background(primaryEmerald.copy(alpha = alpha), CircleShape)
                    )
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(56.dp)
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))

                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 36.sp
                    ),
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = body,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White.copy(alpha = 0.75f),
                    textAlign = TextAlign.Center,
                    lineHeight = 32.sp
                )
            }

            // Bottom: Stop Button (Emerald Pill)
            Button(
                onClick = onStop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryEmerald,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(24.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(28.dp))
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = stringResource(R.string.stop_sound),
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}

fun getArabicPrayerName(title: String): String {
    val t = title.lowercase()
    return when {
        t.contains("fajr") -> "صَلَاةُ ٱلْفَجْرِ"
        t.contains("sunrise") -> "صَلَاةُ ٱلشُّرُوقِ"
        t.contains("dhuhr") -> "صَلَاةُ ٱلظُّهْرِ"
        t.contains("asr") -> "صَلَاةُ ٱلْعَصْرِ"
        t.contains("maghrib") -> "صَلَاةُ ٱلْمَغْرِبِ"
        t.contains("isha") -> "صَلَاةُ ٱلْعِشَاءِ"
        t.contains("qiyam") -> "صَلَاةُ ٱلْقِيَامِ"
        else -> title
    }
}

@Composable
fun ModernPrayerAlertScreen(
    title: String,
    body: String,
    onStop: () -> Unit
) {
    var currentTime by remember { mutableStateOf("") }
    
    LaunchedEffect(Unit) {
        val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        while(true) {
            currentTime = dateFormat.format(Date())
            kotlinx.coroutines.delay(1000)
        }
    }

    // Modern Palette
    val OffWhite = Color(0xFFFAF7F0)
    val DeepTeal = Color(0xFF1B5B5B)
    val AntiqueGold = Color(0xFFD4AF37)
    val DarkCharcoal = Color(0xFF1A2A2A)

    // Animation for the pulsing circle
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(OffWhite)
    ) {
        // Ornate Geometric Frame (from PrayerTimeCard)
        // Increased padding to make the rectangle/frame slightly smaller and more centered
        Canvas(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 32.dp)) {
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
        }

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 40.dp, vertical = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top: Current Time
            Text(
                text = currentTime,
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 90.sp,
                    fontWeight = FontWeight.Thin,
                    letterSpacing = (-3).sp,
                    fontFamily = FontFamily.Serif
                ),
                color = DeepTeal
            )

            // Center: Prayer Info
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Pulsing Icon with Modern Theme
                Box(contentAlignment = Alignment.Center) {
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .scale(scale)
                            .background(AntiqueGold.copy(alpha = 0.15f), CircleShape)
                    )
                    Box(
                        modifier = Modifier
                            .size(140.dp)
                            .scale(scale * 1.3f)
                            .background(AntiqueGold.copy(alpha = alpha), CircleShape)
                    )
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = null,
                        tint = DeepTeal,
                        modifier = Modifier.size(56.dp)
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))

                // Arabic Prayer Name
                Text(
                    text = getArabicPrayerName(title),
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 42.sp,
                        fontFamily = FontFamily.Serif
                    ),
                    color = DeepTeal,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // English Body
                Text(
                    text = body,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontFamily = FontFamily.Serif
                    ),
                    color = DarkCharcoal.copy(alpha = 0.75f),
                    textAlign = TextAlign.Center,
                    lineHeight = 32.sp
                )
            }

            // Bottom: Stop Button (Deep Teal)
            Button(
                onClick = onStop,
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = DeepTeal,
                    contentColor = OffWhite
                ),
                shape = RoundedCornerShape(28.dp),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(20.dp), tint = AntiqueGold)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = stringResource(R.string.stop_sound),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif
                        ),
                        color = OffWhite
                    )
                }
            }
        }
    }
}
