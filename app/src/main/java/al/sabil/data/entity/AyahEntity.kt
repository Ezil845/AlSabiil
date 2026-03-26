package al.sabil.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity(tableName = "ayahs")
@Serializable
data class AyahEntity(
    @PrimaryKey val id: Int,
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
