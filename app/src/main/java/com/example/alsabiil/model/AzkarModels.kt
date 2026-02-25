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

@Serializable
data class AdhkarV2Item(
    val id: Int,
    val text: String,
    val count: Int
)

@Serializable
data class AdhkarV2Category(
    val id: Int,
    val category: String,
    val array: List<AdhkarV2Item>
)
