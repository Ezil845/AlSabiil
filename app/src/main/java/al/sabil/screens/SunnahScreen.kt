package al.sabil.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.composables.icons.lucide.*
import al.sabil.R
import al.sabil.model.SunnahCategory
import al.sabil.model.SunnahItem
import al.sabil.viewmodel.SunnahViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SunnahScreen(
    onBack: () -> Unit,
    viewModel: SunnahViewModel = viewModel()
) {
    val checkedItems by viewModel.checkedItems.collectAsState()
    val sunnahItems by viewModel.sunnahItems.collectAsState()
    
    val backgroundColor = Color(0xFFFFFCF2) // Mushaf Cream
    val emerald = Color(0xFF70a080)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { 
                    Text(
                        stringResource(R.string.sunnah_checklist), 
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF333333)
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Lucide.ChevronLeft, contentDescription = stringResource(R.string.back_button), tint = Color(0xFF333333))
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = backgroundColor)
            )
        },
        containerColor = backgroundColor
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
        ) {
            // Progress Section
            val total = sunnahItems.size
            val count = checkedItems.size
            val progress = if (total > 0) count.toFloat() / total else 0f
            
            SunnahProgressCard(progress, count, total, emerald)

            Spacer(modifier = Modifier.height(24.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                val grouped = sunnahItems.groupBy { it.category }
                
                grouped.forEach { (category, items) ->
                    item {
                        Text(
                            text = when(category) {
                                SunnahCategory.DAILY -> stringResource(R.string.sunnah_daily)
                                SunnahCategory.WEEKLY -> stringResource(R.string.sunnah_weekly)
                                SunnahCategory.MONTHLY -> stringResource(R.string.sunnah_monthly)
                            },
                            style = MaterialTheme.typography.labelLarge,
                            color = emerald,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    items(items) { item ->
                        SunnahItemRow(
                            item = item,
                            isChecked = checkedItems.contains(item.id),
                            onCheckedChange = { viewModel.toggleItem(item.id, it) },
                            emerald = emerald
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SunnahProgressCard(progress: Float, count: Int, total: Int, emerald: Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = emerald
                    )
                    Text(
                        text = stringResource(R.string.sunnah_completed_message),
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
                Text(
                    text = "$count/$total",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = emerald,
                trackColor = emerald.copy(alpha = 0.1f)
            )
        }
    }
}

@Composable
fun SunnahItemRow(item: SunnahItem, isChecked: Boolean, onCheckedChange: (Boolean) -> Unit, emerald: Color) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isChecked) emerald.copy(alpha = 0.05f) else Color.White
        ),
        border = if (isChecked) androidx.compose.foundation.BorderStroke(1.dp, emerald.copy(alpha = 0.2f)) else null,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(item.titleResId),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isChecked) emerald else Color(0xFF1A202C)
                )
                Text(
                    text = stringResource(item.descResId),
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
            Checkbox(
                checked = isChecked,
                onCheckedChange = onCheckedChange,
                colors = CheckboxDefaults.colors(checkedColor = emerald)
            )
        }
    }
}
