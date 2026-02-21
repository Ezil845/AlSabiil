package com.example.alsabiil.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alsabiil.R
import com.example.alsabiil.model.Ayah
import com.example.alsabiil.ui.theme.HafsSmart

import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TafseerModal(
    ayah: Ayah,
    tafseerText: String,
    tafseerType: String = "saddi",
    onDismiss: () -> Unit
) {
    val tafseerTitle = when (tafseerType) {
        "ibn_kathir" -> stringResource(R.string.tafseer_ibn_kathir)
        else -> stringResource(R.string.tafseer_saddi)
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        ModalBottomSheet(
            onDismissRequest = onDismiss,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false),
            containerColor = Color(0xFFFFFCF2),
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 40.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Ayah Header
            Box(
                modifier = Modifier
                    .padding(vertical = 16.dp)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.surah_ayah_format, ayah.sura_name_ar, ayah.aya_no),
                    style = MaterialTheme.typography.titleLarge,
                    color = Color(0xFF2D3748),
                    fontWeight = FontWeight.Bold
                )
            }

            // Original Ayah Text
            Text(
                text = ayah.aya_text,
                fontFamily = HafsSmart,
                fontSize = 24.sp,
                lineHeight = 40.sp,
                textAlign = TextAlign.Center,
                color = Color(0xFF059669), // Emerald
                modifier = Modifier.padding(bottom = 24.dp)
            )

            HorizontalDivider(color = Color(0xFFE0E8E0))

            Spacer(modifier = Modifier.height(24.dp))

            // Tafseer Title
            Text(
                text = tafseerTitle,
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFF557560),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Start
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Tafseer Body
            val strippedTafseer = remember(tafseerText) {
                tafseerText
                    .replace(Regex("<[^>]*>"), "") // Strip HTML
                    .replace(Regex("[{}\\[\\]]"), "") // Strip { } and [ ]
                    .trim()
            }
            Text(
                text = strippedTafseer,
                fontSize = 18.sp,
                lineHeight = 32.sp,
                textAlign = TextAlign.Justify,
                color = Color(0xFF2D3748),
                modifier = Modifier.fillMaxWidth()
            )
        }
        }
    }
}
