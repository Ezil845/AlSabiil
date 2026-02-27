package com.example.alsabiil.notifications

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alsabiil.R
import com.example.alsabiil.ui.theme.AlSabiilTheme
import com.example.alsabiil.ui.theme.AppPalette

class PrayerAlertActivity : ComponentActivity() {

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
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
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

        setContent {
            AlSabiilTheme(palette = AppPalette.fromId("emerald")) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black.copy(alpha = 0.4f)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            modifier = Modifier
                                .width(340.dp) // Wider dialog
                                .wrapContentHeight(),
                            shape = RoundedCornerShape(28.dp),
                            color = Color.White,
                            tonalElevation = 12.dp,
                            shadowElevation = 8.dp
                        ) {
                            val displayTitle = intent.getStringExtra("title") ?: stringResource(R.string.notification_prayer_title, "")
                            val displayBody = intent.getStringExtra("body") ?: stringResource(R.string.notification_prayer_body, "")

                            Column(
                                modifier = Modifier.padding(32.dp), // More generous padding
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = Color(0xFF70a080).copy(alpha = 0.15f),
                                    modifier = Modifier.size(72.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.NotificationsActive,
                                            contentDescription = null,
                                            tint = Color(0xFF70a080),
                                            modifier = Modifier.size(36.dp)
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(20.dp))
                                
                                Text(
                                    text = displayTitle,
                                    style = MaterialTheme.typography.headlineSmall, // Larger typography
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF1A202C)
                                )
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Text(
                                    text = displayBody,
                                    style = MaterialTheme.typography.bodyLarge,
                                    textAlign = TextAlign.Center,
                                    color = Color(0xFF718096),
                                    lineHeight = 24.sp
                                )
                                
                                Spacer(modifier = Modifier.height(32.dp))
                                
                                Button(
                                    onClick = {
                                        // Use static method to stop sound to be sure
                                        NotificationHelper.stopAdhanSound()
                                        finish()
                                    },
                                    modifier = Modifier.fillMaxWidth().height(56.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF70a080)),
                                    shape = CircleShape
                                ) {
                                    Text(
                                        text = stringResource(R.string.stop_sound),
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
