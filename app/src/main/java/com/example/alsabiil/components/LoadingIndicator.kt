package com.example.alsabiil.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.*
import com.example.alsabiil.R

@Composable
fun LoadingIndicator(
    modifier: Modifier = Modifier,
    size: Int = 200
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.loading_indicator))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever
    )

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = Modifier.size(size.dp)
        )
    }
}
