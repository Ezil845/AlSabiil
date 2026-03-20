package al.sabil.model

import kotlinx.serialization.Serializable

@Serializable
data class SunnahItem(
    val id: String,
    val titleResId: Int,
    val descResId: Int,
    val category: SunnahCategory,
    val iconResId: Int? = null
)

enum class SunnahCategory {
    DAILY,
    WEEKLY, // e.g. Friday
    MONTHLY // e.g. White days
}

data class SunnahStatus(
    val itemId: String,
    val isChecked: Boolean,
    val lastUpdatedMillis: Long
)
