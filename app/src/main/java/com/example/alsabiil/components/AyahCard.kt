package com.example.alsabiil.components

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alsabiil.model.AyahData
import kotlinx.coroutines.delay

import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

@Composable
fun AyahCard(
    contentList: List<AyahData>,
    rotateInterval: Long = 10000L
) {
    var currentIndex by remember { mutableIntStateOf(0) }
    val currentContent = contentList.getOrNull(currentIndex) ?: return

    LaunchedEffect(Unit) {
        while (true) {
            delay(rotateInterval)
            currentIndex = (currentIndex + 1) % contentList.size
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        val primaryColor = MaterialTheme.colorScheme.primary
        val primaryContainer = MaterialTheme.colorScheme.primaryContainer
        
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = primaryColor.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(24.dp)
                ),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(primaryColor, primaryContainer)
                        )
                    )
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .background(Color.White.copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (currentContent.type == "ayah") Icons.Default.Book else Icons.Default.Favorite,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (currentContent.type == "ayah") "Ayah of the Day" else "Daily Douaa",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }

                Crossfade(targetState = currentContent, label = "content") { content ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        // Arabic Text
                        Text(
                            text = content.arabic,
                            fontSize = 24.sp,
                            lineHeight = 40.sp,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        // Translation
                        Text(
                            text = content.translation,
                            fontSize = 14.sp,
                            lineHeight = 22.sp,
                            color = Color.White.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center,
                            fontStyle = FontStyle.Italic,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        // Source
                        Text(
                            text = content.source,
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.5f),
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }
                }

                // Dots indicator
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    contentList.forEachIndexed { index, _ ->
                        Box(
                            modifier = Modifier
                                .height(6.dp)
                                .width(if (index == currentIndex) 18.dp else 6.dp)
                                .background(
                                    color = if (index == currentIndex) Color.White else Color.White.copy(alpha = 0.3f),
                                    shape = CircleShape
                                )
                        )
                    }
                }
            }
        }
    }
}
