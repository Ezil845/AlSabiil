package com.example.alsabiil.model

import kotlinx.serialization.Serializable

@Serializable
data class Bookmark(
    val surahNumber: Int,
    val ayahNumber: Int,
    val pageNumber: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val name: String = ""
)