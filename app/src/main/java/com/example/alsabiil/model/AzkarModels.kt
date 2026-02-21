package com.example.alsabiil.model

import kotlinx.serialization.Serializable

@Serializable
data class AzkarItem(
    val zekr: String,
    val repeat: Int,
    val bless: String
)

@Serializable
data class AzkarCategory(
    val title: String,
    val content: List<AzkarItem>
)
