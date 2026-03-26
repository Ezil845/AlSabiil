package al.sabil.components

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.TextStyle
import com.composables.icons.lucide.*
import al.sabil.R
import al.sabil.model.AyahData
import kotlinx.coroutines.delay

// ── Colors ───────────────────────────────────────────────────────────────────
private val TemplateCream       = Color(0xFFF5EFE0)
private val TemplateTealDark    = Color(0xFF1B5E47)
private val TemplateGold        = Color(0xFFC9A84C)
private val TemplateGoldDark    = Color(0xFFB8952A)
private val TemplateGoldText    = Color(0xFFF0C842)
private val TemplateWhite       = Color(0xFFFFFFFF)
private val TemplateWhiteFaded  = Color(0x99FFFFFF)

@Composable
fun AyahCard(
    contentList: List<AyahData>,
    rotateInterval: Long = 10000L,
    isModern: Boolean = true
) {
    if (isModern) {
        ModernAyahCard(contentList, rotateInterval)
    } else {
        ClassicAyahCard(contentList, rotateInterval)
    }
}

@Composable
fun ModernAyahCard(
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

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .wrapContentHeight()
            .background(TemplateCream)
            .drawBehind {
                drawOrnateFrame()
            }
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(8.dp))

            // ── Label ──────────────────────────────────────────────────────
            Text(
                text = if (currentContent.type == "ayah") "AYAH OF THE DAY" else "DAILY REMINDER",
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 3.sp,
                color = TemplateTealDark,
                textAlign = TextAlign.Center,
                fontFamily = FontFamily.Serif
            )

            Spacer(Modifier.height(8.dp))

            // ── Decorative rule ─────────────────────────────────────────────
            ThreeDotRule()

            Spacer(Modifier.height(10.dp))

            // ── Teal Banner ────────────────────────────────────────────────
            Crossfade(targetState = currentContent, label = "content") { content ->
                TealBanner(content)
            }

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun TealBanner(content: AyahData) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(TemplateTealDark)
            .drawBehind {
                val inset = 5.dp.toPx()
                drawRect(
                    color = TemplateGold.copy(alpha = 0.4f),
                    topLeft = Offset(inset, inset),
                    size = Size(size.width - inset * 2, size.height - inset * 2),
                    style = Stroke(width = 1.dp.toPx())
                )
                drawCornerBrackets(
                    offset = 9.dp.toPx(),
                    length = 14.dp.toPx(),
                    strokeWidth = 1.5.dp.toPx(),
                    color = TemplateGold.copy(alpha = 0.7f)
                )
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(Modifier.fillMaxWidth()) {
                BookmarkRibbon()
            }

            Spacer(Modifier.height(4.dp))

            // Arabic text
            Text(
                text = content.arabic,
                fontSize = 24.sp,
                lineHeight = 38.sp,
                color = TemplateGoldText,
                textAlign = TextAlign.Center,
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.Bold,
                style = TextStyle(textDirection = TextDirection.Rtl),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(10.dp))

            GoldDivider()

            Spacer(Modifier.height(10.dp))

            // Translation
            Text(
                text = content.translation,
                fontSize = 15.sp,
                lineHeight = 22.sp,
                fontStyle = FontStyle.Italic,
                color = TemplateWhite,
                textAlign = TextAlign.Center,
                fontFamily = FontFamily.Serif,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(6.dp))

            // Source
            Text(
                text = content.source,
                fontSize = 10.sp,
                color = TemplateWhiteFaded,
                textAlign = TextAlign.Center,
                letterSpacing = 0.5.sp,
                fontFamily = FontFamily.Serif
            )

            Spacer(Modifier.height(10.dp))

            GoldSeal()

            Spacer(Modifier.height(4.dp))
        }
    }
}

@Composable
private fun BookmarkRibbon() {
    Box(
        modifier = Modifier
            .width(20.dp)
            .height(32.dp)
            .drawBehind {
                val w = size.width
                val h = size.height
                drawRect(color = TemplateGold)
                val path = Path().apply {
                    moveTo(0f, h)
                    lineTo(w / 2f, h * 0.75f)
                    lineTo(w, h)
                    close()
                }
                drawPath(path, color = TemplateTealDark)
                drawLine(
                    color = TemplateTealDark.copy(alpha = 0.4f),
                    start = Offset(4.dp.toPx(), 8.dp.toPx()),
                    end = Offset(w - 4.dp.toPx(), 8.dp.toPx()),
                    strokeWidth = 0.8.dp.toPx()
                )
                drawLine(
                    color = TemplateTealDark.copy(alpha = 0.4f),
                    start = Offset(4.dp.toPx(), 12.dp.toPx()),
                    end = Offset(w - 4.dp.toPx(), 12.dp.toPx()),
                    strokeWidth = 0.8.dp.toPx()
                )
            }
    )
}

@Composable
private fun GoldSeal() {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(48.dp)
            .drawBehind {
                val cx = size.width / 2f
                val cy = size.height / 2f
                val r  = size.width / 2f
                val cut = r * 0.29f

                val outerPath = octagonPath(cx, cy, r, cut)
                drawPath(outerPath, color = TemplateGoldDark)

                val innerPath = octagonPath(cx, cy, r * 0.80f, cut * 0.80f)
                drawPath(innerPath, color = TemplateGold)

                val ringPath = octagonPath(cx, cy, r * 0.65f, cut * 0.65f)
                drawPath(ringPath, color = TemplateGoldDark, style = Stroke(width = 1.dp.toPx()))
            }
    ) {
        Text(
            text = "الله",
            fontSize = 12.sp,
            color = TemplateTealDark,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Serif,
            textAlign = TextAlign.Center,
            style = TextStyle(textDirection = TextDirection.Rtl)
        )
    }
}

@Composable
private fun ThreeDotRule() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(Modifier.weight(1f).height(1.dp).background(TemplateGold))
        repeat(3) {
            Spacer(Modifier.width(4.dp))
            Box(
                Modifier
                    .size(5.dp)
                    .drawBehind {
                        val path = diamondPath(size.width / 2, size.height / 2, size.width / 2)
                        drawPath(path, TemplateGold)
                    }
            )
        }
        Spacer(Modifier.width(4.dp))
        Box(Modifier.weight(1f).height(1.dp).background(TemplateGold))
    }
}

@Composable
private fun GoldDivider() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Box(Modifier.weight(1f).height(0.5.dp).background(TemplateGold.copy(alpha = 0.5f)))
        Spacer(Modifier.width(6.dp))
        Box(
            Modifier
                .size(6.dp)
                .drawBehind {
                    val path = diamondPath(size.width / 2, size.height / 2, size.width / 2)
                    drawPath(path, TemplateGold.copy(alpha = 0.8f))
                }
        )
        Spacer(Modifier.width(6.dp))
        Box(Modifier.weight(1f).height(0.5.dp).background(TemplateGold.copy(alpha = 0.5f)))
    }
}

private fun DrawScope.drawOrnateFrame() {
    val w = size.width
    val h = size.height
    val thick = 3.dp.toPx()
    val thin  = 1.dp.toPx()
    val gap1  = 7.dp.toPx()
    val gap2  = 10.dp.toPx()
    val sqSize = 12.dp.toPx()

    drawRect(color = TemplateGoldDark, style = Stroke(width = thick))

    drawRect(
        color = TemplateGold,
        topLeft = Offset(gap1, gap1),
        size = Size(w - gap1 * 2, h - gap1 * 2),
        style = Stroke(width = thin)
    )

    drawRect(
        color = TemplateGold,
        topLeft = Offset(gap2, gap2),
        size = Size(w - gap2 * 2, h - gap2 * 2),
        style = Stroke(width = thin)
    )

    listOf(
        Offset(0f, 0f),
        Offset(w - sqSize, 0f),
        Offset(0f, h - sqSize),
        Offset(w - sqSize, h - sqSize)
    ).forEach { drawRect(color = TemplateGoldDark, topLeft = it, size = Size(sqSize, sqSize)) }

    val dmSize = 5.dp.toPx()
    listOf(
        Offset(w / 2, 0f),
        Offset(w / 2, h),
        Offset(0f,    h / 2),
        Offset(w,     h / 2)
    ).forEach { center ->
        val dp = Path().apply {
            moveTo(center.x, center.y - dmSize)
            lineTo(center.x + dmSize, center.y)
            lineTo(center.x, center.y + dmSize)
            lineTo(center.x - dmSize, center.y)
            close()
        }
        drawPath(dp, TemplateCream)
        drawPath(dp, TemplateGoldDark, style = Stroke(width = 1.5.dp.toPx()))
    }
}

private fun DrawScope.drawCornerBrackets(
    offset: Float,
    length: Float,
    strokeWidth: Float,
    color: Color
) {
    val w = size.width
    val h = size.height
    val corners = listOf(
        Triple(Offset(offset, offset),       Offset(offset + length, offset),       Offset(offset, offset + length)),
        Triple(Offset(w - offset, offset),   Offset(w - offset - length, offset),   Offset(w - offset, offset + length)),
        Triple(Offset(offset, h - offset),   Offset(offset + length, h - offset),   Offset(offset, h - offset - length)),
        Triple(Offset(w-offset, h - offset), Offset(w-offset-length, h - offset),   Offset(w-offset, h - offset - length))
    )
    corners.forEach { (corner, h1, v1) ->
        drawLine(color, corner, h1, strokeWidth)
        drawLine(color, corner, v1, strokeWidth)
    }
}

private fun octagonPath(cx: Float, cy: Float, r: Float, cut: Float): Path {
    return Path().apply {
        moveTo(cx - r + cut, cy - r)
        lineTo(cx + r - cut, cy - r)
        lineTo(cx + r,       cy - r + cut)
        lineTo(cx + r,       cy + r - cut)
        lineTo(cx + r - cut, cy + r)
        lineTo(cx - r + cut, cy + r)
        lineTo(cx - r,       cy + r - cut)
        lineTo(cx - r,       cy - r + cut)
        close()
    }
}

private fun diamondPath(cx: Float, cy: Float, r: Float): Path {
    return Path().apply {
        moveTo(cx, cy - r)
        lineTo(cx + r, cy)
        lineTo(cx, cy + r)
        lineTo(cx - r, cy)
        close()
    }
}

@Composable
fun ClassicAyahCard(
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

    androidx.compose.runtime.CompositionLocalProvider(androidx.compose.ui.platform.LocalLayoutDirection provides androidx.compose.ui.unit.LayoutDirection.Rtl) {
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
                        brush = androidx.compose.ui.graphics.Brush.linearGradient(
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
                        androidx.compose.material3.Icon(
                            imageVector = if (currentContent.type == "ayah") Lucide.BookOpen else Lucide.Heart,
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
