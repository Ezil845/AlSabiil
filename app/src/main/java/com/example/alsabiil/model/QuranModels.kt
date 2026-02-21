package com.example.alsabiil.model

import kotlinx.serialization.Serializable

@Serializable
data class Ayah(
    val id: Int,
    val jozz: Int,
    val sura_no: Int,
    val sura_name_en: String,
    val sura_name_ar: String,
    val page: Int,
    val line_start: Int,
    val line_end: Int,
    val aya_no: Int,
    val aya_text: String,
    val aya_text_emlaey: String
)

data class SurahInfo(
    val number: Int,
    val nameEn: String,
    val nameAr: String,
    val startPage: Int
)

data class JuzzInfo(
    val number: Int,
    val startPage: Int
)

data class AyahData(
    val id: Int,
    val arabic: String,
    val translation: String,
    val source: String,
    val type: String
)
