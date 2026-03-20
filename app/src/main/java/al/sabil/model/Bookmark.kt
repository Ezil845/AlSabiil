package al.sabil.model

import al.sabil.data.BookmarkEntity

data class Bookmark(
    val surahNumber: Int,
    val ayahNumber: Int,
    val pageNumber: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val name: String = ""
)

fun BookmarkEntity.toBookmark() = Bookmark(
    surahNumber = surahNumber,
    ayahNumber = ayahNumber,
    pageNumber = pageNumber,
    timestamp = timestamp,
    name = name
)

fun Bookmark.toEntity() = BookmarkEntity(
    surahNumber = surahNumber,
    ayahNumber = ayahNumber,
    pageNumber = pageNumber,
    timestamp = timestamp,
    name = name
)