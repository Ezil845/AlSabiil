package com.example.alsabiil.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alsabiil.model.Ayah
import com.example.alsabiil.ui.theme.HafsSmart
import com.example.alsabiil.ui.theme.SurahNames

@Composable
fun MushafPage(
    pageNumber: Int,
    ayahs: List<Ayah>,
    bookmarkedAyahs: Set<Pair<Int, Int>> = emptySet(),
    onAyahClick: (Ayah) -> Unit
) {
    if (ayahs.isEmpty()) return

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    // 5.5% of width, similar to React Native logic
    val dynamicFontSize = (screenWidth.value * 0.055).sp
    val dynamicLineHeight = dynamicFontSize * 1.8

    val surahGroups = remember(ayahs) {
        val groups = mutableListOf<SurahGroup>()
        ayahs.forEach { ayah ->
            val lastGroup = groups.lastOrNull()
            if (lastGroup == null || lastGroup.suraNo != ayah.sura_no) {
                groups.add(
                    SurahGroup(
                        suraNo = ayah.sura_no,
                        suraName = ayah.sura_name_ar,
                        ayahs = mutableListOf()
                    )
                )
            }
            groups.last().ayahs.add(ayah)
        }
        groups
    }

    val surahLabel = remember { String(Character.toChars(0xE000)) }
    
    fun getSurahGlyph(surahNumber: Int): String {
        return String(Character.toChars(0xE000 + surahNumber))
    }

    val basmalah = "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ"

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFFFCF2))
                .padding(horizontal = 8.dp, vertical = 24.dp)
        ) {
            surahGroups.forEach { group ->
                // Surah Header
                if (group.ayahs.first().aya_no == 1) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${getSurahGlyph(group.suraNo)} $surahLabel",
                            style = TextStyle(
                                fontFamily = SurahNames,
                                fontSize = 40.sp,
                                color = Color(0xFF70a080),
                                textAlign = TextAlign.Center
                            ),
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        if (group.suraNo != 1 && group.suraNo != 9) {
                            Text(
                                text = basmalah,
                                style = TextStyle(
                                    fontFamily = HafsSmart,
                                    fontSize = 22.sp,
                                    color = Color.Black,
                                    textAlign = TextAlign.Center
                                ),
                                modifier = Modifier.padding(bottom = 10.dp)
                            )
                        }
                    }
                }

                val annotatedString = buildAnnotatedString {
                    group.ayahs.forEach { ayah ->
                        val isBookmarked = bookmarkedAyahs.contains(Pair(ayah.sura_no, ayah.aya_no))
                        pushStringAnnotation(tag = "AYAH", annotation = ayah.aya_no.toString())
                        withStyle(style = SpanStyle(
                            color = if (isBookmarked) Color(0xFF2E7D32) else Color.Black,
                            background = if (isBookmarked) Color(0x2070A080) else Color.Transparent
                        )) {
                            append("${ayah.aya_text} ")
                        }
                        pop()
                    }
                }

                ClickableText(
                    text = annotatedString,
                    style = TextStyle(
                        fontFamily = HafsSmart,
                        fontSize = dynamicFontSize,
                        lineHeight = dynamicLineHeight,
                        textAlign = TextAlign.Justify, // Reverting to Justify for standard Mushaf look
                        letterSpacing = 0.sp, 
                        textDirection = androidx.compose.ui.text.style.TextDirection.Content // Best for bidirectional/RTL content
                    ),
                    onClick = { offset ->
                        annotatedString.getStringAnnotations(tag = "AYAH", start = offset, end = offset)
                            .firstOrNull()?.let { annotation ->
                                val ayaNo = annotation.item.toInt()
                                val clickedAyah = group.ayahs.find { it.aya_no == ayaNo }
                                if (clickedAyah != null) {
                                    onAyahClick(clickedAyah)
                                }
                            }
                    }
                )
                
                Spacer(modifier = Modifier.height(15.dp))
            }

            Spacer(modifier = Modifier.weight(1f))
            
            Text(
                text = pageNumber.toString(),
                style = TextStyle(
                    color = Color.Gray,
                    fontSize = 14.sp
                ),
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

private data class SurahGroup(
    val suraNo: Int,
    val suraName: String,
    val ayahs: MutableList<Ayah>
)