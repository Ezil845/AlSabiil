package al.sabil.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "bookmarks",
    indices = [Index(value = ["surahNumber", "ayahNumber"], unique = true)]
)
data class BookmarkEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val surahNumber: Int,
    val ayahNumber: Int,
    val pageNumber: Int,
    val timestamp: Long = System.currentTimeMillis(),
    val name: String = ""
)
