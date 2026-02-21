package com.example.alsabiil.repository

import com.example.alsabiil.model.AyahData
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

    fun getDailyContent(): List<AyahData> {
        val calendar = Calendar.getInstance()
        return if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY) {
            jumuahContent
        } else {
            dailyContent
        }
    }
}
