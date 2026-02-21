package com.example.alsabiil.utils

object QuranUtils {
    const val SURAH_LABEL = "\uE000"
    const val ENCODED_BASMALAH = " \u0628\u0650\u0633\u0652\u0645\u064e \u0627\u0644\u0644\u064e\u0651\u0647\u0650 \u0627\u0644\u0631\u064e\u0651\u062d\u0652\u0645\u064e\u0646\u0650 \u0627\u0644\u0631\u064e\u0651\u062d\u0650\u064a\u0645\u0650 " 
    // Wait, let's use the actual encoded PUA characters from the data
    const val PUA_BASMALAH = "\uE8DB \uE338\uE48E \uE338\uE0AF\uE238\uE903 \uE338\uE0AF\uE238\uE045\uE1C0\uE2E5"

    /**
     * Character mapping for Surah names using 'surah-name-v4.ttf'
     * Surah label word is at 0xE000
     * Surah names start at 0xE001
     */
    fun getSurahFontChar(surahIndex: Int): String {
        val PUA_START = 0xE001
        
        return (PUA_START + (surahIndex - 1)).toChar().toString()
    }
}
