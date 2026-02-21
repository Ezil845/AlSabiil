package com.example.alsabiil.screens

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alsabiil.R
import com.example.alsabiil.components.AzkarCard
import com.example.alsabiil.repository.AzkarRepository

@Composable
fun AzkarScreen(repository: AzkarRepository) {
    val categories = remember { repository.getCategories() }
    var selectedCategory by remember { mutableStateOf(categories.firstOrNull() ?: "") }
    val athkarCounts = remember { mutableStateMapOf<String, Int>() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White) // Pure white for a modern clean look
    ) {
        // Modern Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(top = 32.dp, bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.azkar_title),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1A202C),
                    fontSize = 32.sp,
                    platformStyle = androidx.compose.ui.text.PlatformTextStyle(
                        includeFontPadding = true
                    )
                ),
                textAlign = TextAlign.Center,
                maxLines = 1,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Decorative centered bar
            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(0xFF70a080))
            )
            
            Spacer(modifier = Modifier.height(24.dp))

            // Modern Scrollable Tab Switcher
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                items(categories) { category ->
                    val isSelected = selectedCategory == category
                    val categoryTitle = when (category) {
                        AzkarRepository.MORNING -> stringResource(R.string.morning_azkar)
                        AzkarRepository.EVENING -> stringResource(R.string.evening_azkar)
                        AzkarRepository.AFTER_PRAYER -> stringResource(R.string.after_prayer_azkar)
                        else -> category
                    }

                    Surface(
                        modifier = Modifier
                            .height(40.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { selectedCategory = category },
                        color = if (isSelected) Color(0xFF70a080) else Color(0xFFF1F5F9),
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Box(
                            modifier = Modifier.padding(horizontal = 20.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = categoryTitle,
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else Color(0xFF718096),
                                    fontSize = 13.sp
                                )
                            )
                        }
                    }
                }
            }
        }

        // Animated Content Area
        Crossfade(
            targetState = selectedCategory,
            label = "CategoryTransition",
            animationSpec = tween(durationMillis = 300)
        ) { targetCategory ->
            val items = remember(targetCategory) { repository.getAzkarByCategory(targetCategory) }
            
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(items) { item ->
                    val stateKey = "${targetCategory}_${item.zekr}"
                    val currentCount = athkarCounts[stateKey] ?: 0
                    val targetCount = item.repeat
                    
                    AzkarCard(
                        item = item,
                        currentCount = currentCount,
                        isCompleted = currentCount >= targetCount,
                        onCountPress = {
                            if (currentCount < targetCount) {
                                athkarCounts[stateKey] = currentCount + 1
                            }
                        }
                    )
                }
            }
        }
    }
}
