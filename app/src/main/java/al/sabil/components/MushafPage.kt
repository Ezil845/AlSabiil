package al.sabil.components

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
import al.sabil.model.Ayah
import al.sabil.ui.theme.HafsSmart
import al.sabil.ui.theme.SurahNames

@Composable
fun MushafPage(
    pageNumber: Int,
    ayahs: List<Ayah>,
    bookmarkedAyahs: Set<Pair<Int, Int>> = emptySet(),
    isDarkMode: Boolean = false,
    onAyahClick: (Ayah) -> Unit
) {
    if (ayahs.isEmpty()) return

    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val dynamicFontSize = (screenWidth.value * 0.055).sp
    val dynamicLineHeight = dynamicFontSize * 1.8

    // Colors based on theme
    val backgroundColor = if (isDarkMode) Color.Black else Color(0xFFFFFCF2)
    val textColor = if (isDarkMode) Color(0xFFE2E8F0) else Color.Black // Soft white
    val primaryColor = if (isDarkMode) Color(0xFFD4AF37) else Color(0xFF70a080) // Gold in dark mode
    val bookmarkColor = if (isDarkMode) Color(0xFFD4AF37) else Color(0xFF2E7D32)
    val bookmarkHighlight = if (isDarkMode) Color(0x33D4AF37) else Color(0x2070A080)

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
                .background(backgroundColor)
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
                                color = primaryColor,
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
                                    color = textColor,
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
                            color = if (isBookmarked) bookmarkColor else textColor,
                            background = if (isBookmarked) bookmarkHighlight else Color.Transparent
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
                        textAlign = TextAlign.Justify,
                        letterSpacing = 0.sp, 
                        textDirection = androidx.compose.ui.text.style.TextDirection.Content
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
                    color = if (isDarkMode) Color.Gray else Color.Gray,
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
