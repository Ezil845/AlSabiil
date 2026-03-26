package al.sabil.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "azkar")
data class AzkarEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val category: String,
    val zekr: String,
    val count: Int,
    val description: String? = null,
    val reference: String? = null
)
