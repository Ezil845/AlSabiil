package com.example.alsabiil.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alsabiil.model.AzkarItem
import com.example.alsabiil.ui.theme.HafsSmart

@Composable
fun AzkarCard(
    item: AzkarItem,
    onCountPress: () -> Unit,
    currentCount: Int,
    isCompleted: Boolean
) {
    val targetCount = item.repeat
    val progress = remember(currentCount, targetCount) {
        if (targetCount > 0) currentCount.toFloat() / targetCount else 0f
    }
    
    val animatedProgress by animateFloatAsState(
        targetValue = progress, 
        label = "Progress",
        animationSpec = spring(stiffness = Spring.StiffnessLow)
    )
    val backgroundColor by animateColorAsState(
        targetValue = if (isCompleted) Color(0xFFF0FAF4) else Color.White,
        label = "BgColor"
    )

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .shadow(
                    elevation = if (isCompleted) 0.dp else 4.dp,
                    shape = RoundedCornerShape(20.dp),
                    ambientColor = Color(0xFF70a080),
                    spotColor = Color(0xFF70a080).copy(alpha = 0.2f)
                )
                .clip(RoundedCornerShape(20.dp))
                .background(backgroundColor)
                .clickable(enabled = !isCompleted, onClick = onCountPress)
        ) {
            // Background Progress Fill (Subtle)
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .fillMaxWidth(animatedProgress)
                    .background(Color(0xFF70a080).copy(alpha = 0.05f))
            )

            Column(
                modifier = Modifier
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Arabic Text with enhanced typography
                Text(
                    text = item.zekr,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontFamily = HafsSmart,
                        fontSize = 24.sp,
                        lineHeight = 48.sp,
                        fontWeight = FontWeight.Normal,
                        color = if (isCompleted) Color(0xFF70a080) else Color(0xFF1A202C),
                        platformStyle = androidx.compose.ui.text.PlatformTextStyle(
                            includeFontPadding = true
                        )
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                )

                if (item.bless.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = item.bless,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 13.sp,
                            lineHeight = 22.sp,
                            color = Color(0xFF718096),
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        ),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Modern Minimal Counter
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End, // Align to the right
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Right side: Count Badge
                    Surface(
                        color = if (isCompleted) Color(0xFF70a080) else Color(0xFFF1F5F9),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (isCompleted) "✓" else "$currentCount / $targetCount",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isCompleted) Color.White else Color(0xFF4A5568),
                                fontSize = 14.sp
                            )
                        )
                    }
                }
            }
        }
    }
}
