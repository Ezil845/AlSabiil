package al.sabil.repository

import al.sabil.model.AyahData
import java.time.chrono.HijrahDate
import java.time.temporal.ChronoField
import java.time.temporal.ChronoUnit
import java.util.*

class AyahRepository {
    private val dailyContent = listOf(
        AyahData(1, "إِنَّ مَعَ الْعُسْرِ يُسْرًا", "Indeed, with hardship comes ease.", "Surah Ash-Sharh 94:6", "ayah"),
        AyahData(2, "وَمَن يَتَوَكَّلْ عَلَى اللَّهِ فَهُوَ حَسْبُهُ", "And whoever relies upon Allah - then He is sufficient for him.", "Surah At-Talaq 65:3", "ayah"),
        AyahData(3, "رَبِّ اشْرَحْ لِي صَدْرِي وَيَسِّرْ لِي أَمْرِي", "My Lord, expand for me my chest and ease for me my task.", "Surah Ta-Ha 20:25-26", "douaa"),
        AyahData(4, "فَاذْكُرُونِي أَذْكُرْكُمْ", "So remember Me; I will remember you.", "Surah Al-Baqarah 2:152", "ayah"),
        AyahData(5, "اللَّهُمَّ إِنِّي أَسْأَلُكَ الْهُدَى وَالتُّقَى وَالْعَفَافَ وَالْغِنَى", "O Allah, I ask You for guidance, piety, chastity and self-sufficiency.", "Hadith - Sahih Muslim", "douaa"),
        AyahData(6, "وَقُل رَّبِّ زِدْنِي عِلْمًا", "And say: My Lord, increase me in knowledge.", "Surah Ta-Ha 20:114", "ayah"),
        AyahData(7, "رَبَّنَا آتِنَا فِي الدُّنْيَا حَسَنَةً وَفِي الْآخِرَةِ حَسَنَةً", "Our Lord, give us good in this world and good in the Hereafter.", "Surah Al-Baqarah 2:201", "douaa"),
        AyahData(8, "وَاللَّهُ خَيْرُ الرَّازِقِينَ", "And Allah is the best of providers.", "Surah Al-Jumu'ah 62:11", "ayah"),
        AyahData(101, "إِنَّمَا الأَعْمَالُ بِالنِّيَّاتِ", "Actions are judged by intentions.", "Sahih Bukhari", "hadith"),
        AyahData(102, "الْبِرُّ حُسْنُ الْخُلُقِ", "Righteousness is good character.", "Sahih Muslim", "hadith"),
        AyahData(103, "مَنْ كَانَ يُؤْمِنُ بِاللَّهِ وَالْيَوْمِ الآخِرِ فَلْيَقُلْ خَيْرًا أَوْ لِيَصْمُتْ", "Whoever believes in Allah and the Last Day should speak good or remain silent.", "Sahih Bukhari", "hadith")
    )

    private val jumuahContent = listOf(
        AyahData(1, "يَا أَيُّهَا الَّذِينَ آمَنُوا إِذَا نُودِيَ لِلصَّلَاةِ مِن يَوْمِ الْجُمُعَةِ فَاسْعَوْا إِلَىٰ ذِكْرِ اللَّهِ", "O you who have believed, when the adhan is called for prayer on Friday, then proceed to the remembrance of Allah.", "Surah Al-Jumu'ah 62:9", "ayah"),
        AyahData(2, "الْحَمْدُ لِلَّهِ الَّذِي أَنزَلَ عَلَىٰ عَبْدِهِ الْكِتَابَ وَلَمْ يَجْعَل لَّهُ عِوَجًا", "Praise be to Allah, who sent down the Book to His Servant and has not made therein any deviance.", "Surah Al-Kahf 18:1", "ayah"),
        AyahData(3, "اللَّهُمَّ صَلِّ وَسَلِّمْ عَلَى نَبِيِّنَا مُحَمَّدٍ", "O Allah, send blessings and peace upon our Prophet Muhammad.", "Hadith / Adhkar", "douaa"),
        AyahData(4, "إِنَّ اللَّهَ وَمَلَائِكَتَهُ يُصَلُّونَ عَلَى النَّبِيِّ", "Indeed, Allah and His angels bless the Prophet.", "Surah Al-Ahzab 33:56", "ayah")
    )

    fun getDailyContent(hijriOffset: Int = 0): List<AyahData> {
        val calendar = Calendar.getInstance()
        
        // Check for Hijri Events
        val hijriDate = try {
            HijrahDate.now().plus(hijriOffset.toLong(), ChronoUnit.DAYS)
        } catch (e: Exception) {
            null
        }

        if (hijriDate != null) {
            val day = hijriDate.get(ChronoField.DAY_OF_MONTH)
            val month = hijriDate.get(ChronoField.MONTH_OF_YEAR)
            val eventContent = getEventSpecificContent(day, month)
            if (eventContent.isNotEmpty()) return eventContent
        }

        return if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY) {
            jumuahContent
        } else {
            dailyContent
        }
    }

    private fun getEventSpecificContent(day: Int, month: Int): List<AyahData> {
        return when (month) {
            1 -> { // Muharram
                if (day == 10) listOf(
                    AyahData(201, "صيام يوم عاشوراء أحتسب على الله أن يكفر السنة التي قبله", "Fasting the day of Ashura, I hope Allah will expiate the sins of the year before it.", "Hadith - Muslim", "hadith"),
                    AyahData(202, "عاشوراء يوم شكر لله تعالى على نجاة موسى عليه السلام", "Ashura is a day of gratitude to Allah for the salvation of Musa (PBUH).", "Ibn al-Uthaymeen", "hadith")
                ) else emptyList()
            }
            9 -> { // Ramadan
                if (day in listOf(21, 23, 25, 27, 29)) listOf(
                    AyahData(203, "لَيْلَةُ الْقَدْرِ خَيْرٌ مِّنْ أَلْفِ شَهْرٍ", "The Night of Decree is better than a thousand months.", "Surah Al-Qadr 97:3", "ayah"),
                    AyahData(204, "ليالي الوتر أرجى لليلة القدر، فاجتهدوا فيها بالدعاء والذكر", "The odd nights are the most likely for Laylat al-Qadr, so strive in them with supplication and remembrance.", "Ibn Baz", "hadith"),
                    AyahData(205, "اللَّهُمَّ إِنَّكَ عَفُوٌّ تُحِبُّ الْعَفْوَ فَاعْفُ عَنِّي", "O Allah, You are Forgiving and love forgiveness, so forgive me.", "Hadith / Douaa", "douaa")
                ) else if (day == 1) listOf(
                    AyahData(206, "شَهْرُ رَمَضَانَ الَّذِي أُنزِلَ فِيهِ الْقُرْآنُ", "The month of Ramadan [is that] in which was revealed the Qur'an.", "Surah Al-Baqarah 2:185", "ayah"),
                    AyahData(207, "رمضان مدرسة للتقوى وتطهير للنفوس", "Ramadan is a school for piety and purification of the soul.", "Abdul Razzaq al-Badr", "hadith")
                ) else emptyList()
            }
            10 -> { // Shawwal
                if (day == 1) listOf(
                    AyahData(208, "يوم العيد يوم فرح بفضل الله وإتمام النعمة", "The day of Eid is a day of joy for the grace of Allah and the completion of the blessing.", "Ibn al-Uthaymeen", "hadith"),
                    AyahData(209, "تَقَبَّلَ اللَّهُ مِنَّا وَمِنْكُمْ", "May Allah accept from us and from you.", "Salaf Tradition", "douaa")
                ) else emptyList()
            }
            12 -> { // Dhu al-Hijjah
                if (day == 9) listOf(
                    AyahData(210, "خَيْرُ الدُّعَاءِ دُعَاءُ يَوْمِ عَرَفَةَ", "The best of supplications is the supplication of the day of Arafah.", "Hadith - Tirmidhi", "hadith"),
                    AyahData(211, "يوم عرفة يوم العتق من النار والمباهاة بأهل الموقف", "The day of Arafah is the day of liberation from the fire and boasting of the people of the standing.", "Ibn Baz", "hadith")
                ) else if (day == 10) listOf(
                    AyahData(212, "أعظم الأيام عند الله يوم النحر", "The greatest of days with Allah is the Day of Sacrifice (Eid).", "Hadith - Abu Dawood", "hadith"),
                    AyahData(213, "يوم الأضحى يوم التوحيد والامتثال لأمر الله", "The day of Eid al-Adha is the day of Tawhid and compliance with the command of Allah.", "Abdul Razzaq al-Badr", "hadith")
                ) else emptyList()
            }
            else -> emptyList()
        }
    }
}
