package al.sabil.utils

import android.content.Context
import al.sabil.R
import java.time.chrono.HijrahDate
import java.time.temporal.ChronoField

data class IslamicEventData(
    val nameResId: Int,
    val day: Int,
    val month: Int,
    val descResId: Int,
    val endDay: Int? = null,
    val specificDays: List<Int>? = null,
    val dateTextResId: Int? = null
)

object HijriEvents {
    val events = listOf(
        IslamicEventData(R.string.event_hijri_new_year, 1, 1, R.string.event_hijri_new_year_desc),
        IslamicEventData(R.string.event_ashura, 10, 1, R.string.event_ashura_desc),
        IslamicEventData(R.string.event_mawlid, 12, 3, R.string.event_mawlid_desc),
        IslamicEventData(R.string.event_isra_miraj, 27, 7, R.string.event_isra_miraj_desc),
        IslamicEventData(R.string.event_ramadan, 1, 9, R.string.event_ramadan_desc),
        IslamicEventData(R.string.event_laylat_al_qadr, 21, 9, R.string.event_laylat_al_qadr_desc, specificDays = listOf(21, 23, 25, 27, 29), dateTextResId = R.string.event_laylat_al_qadr_date),
        IslamicEventData(R.string.event_eid_al_fitr, 1, 10, R.string.event_eid_al_fitr_desc),
        IslamicEventData(R.string.event_hajj_start, 1, 12, R.string.event_hajj_start_desc),
        IslamicEventData(R.string.event_arafah, 9, 12, R.string.event_arafah_desc),
        IslamicEventData(R.string.event_eid_al_adha, 10, 12, R.string.event_eid_al_adha_desc)
    )

    fun getEventForDate(day: Int, month: Int): IslamicEventData? {
        return events.find { event ->
            if (event.month == month) {
                when {
                    event.specificDays != null -> day in event.specificDays
                    event.endDay != null -> day in event.day..event.endDay
                    else -> event.day == day
                }
            } else false
        }
    }
}
