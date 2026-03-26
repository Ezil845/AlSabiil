package al.sabil.repository

import android.content.Context
import android.util.Log
import al.sabil.data.AppDatabase
import al.sabil.data.entity.AyahEntity
import al.sabil.model.Ayah
import al.sabil.model.JuzzInfo
import al.sabil.model.SurahInfo
import kotlinx.serialization.json.*

private const val TAG = "QuranRepository"

class QuranRepository(private val context: Context) {
    private val database = AppDatabase.getInstance(context)
    private val ayahDao = database.ayahDao()
    
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }
    
    // Cache for Surahs and Juzz to avoid frequent DB hits for static lists
    private var cachedSurahs: List<SurahInfo>? = null
    private var cachedJuzz: List<JuzzInfo>? = null

    private fun AyahEntity.toDomain(): Ayah = Ayah(
        id = id,
        jozz = jozz,
        sura_no = sura_no,
        sura_name_en = sura_name_en,
        sura_name_ar = sura_name_ar,
        page = page,
        line_start = line_start,
        line_end = line_end,
        aya_no = aya_no,
        aya_text = aya_text,
        aya_text_emlaey = aya_text_emlaey
    )

    private var currentTafseerType: String? = null
    private var currentTafseerSurah: Int? = null
    private var tafseerData: Map<String, String> = emptyMap()

    private fun loadTafseer(type: String, sura: Int) {
        if (currentTafseerType == type) {
            if (type == "ibn_kathir") {
                if (currentTafseerSurah == sura && tafseerData.isNotEmpty()) return
            } else {
                if (tafseerData.isNotEmpty()) return
            }
        }
        
        val fileName = if (type == "ibn_kathir") {
            "tafseer/ar-tafseer-ibn-kathir/surah_$sura.json"
        } else {
            "data/ar-tafseer-al-saddi.json"
        }
        
        try {
            val processedMap = mutableMapOf<String, String>()
            
            context.assets.open(fileName).use { inputStream ->
                val jsonString = inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
                val root = json.parseToJsonElement(jsonString).jsonObject
                
                if (type == "ibn_kathir") {
                    for ((key, value) in root) {
                        try {
                            if (value is JsonPrimitive && value.isString) {
                                val compositeKey = "$sura:$key"
                                processedMap[compositeKey] = value.content
                            }
                        } catch (e: Exception) {
                            Log.w(TAG, "Skipping invalid entry in Ibn Kathir for key $key: ${e.message}")
                        }
                    }
                } else {
                    val tempMap = mutableMapOf<String, Any>()
                    for ((key, value) in root) {
                        try {
                            if (value is JsonObject) {
                                tempMap[key] = value
                            } else if (value is JsonPrimitive) {
                                tempMap[key] = value.content
                            }
                        } catch (e: Exception) {
                             Log.w(TAG, "Skipping invalid entry in first pass for key $key: ${e.message}")
                        }
                    }
    
                    for ((key, value) in tempMap) {
                        try {
                            var text = ""
                            if (value is JsonObject) {
                                text = value["text"]?.jsonPrimitive?.content ?: ""
                            } else if (value is String) {
                                var targetKey = value
                                var attempts = 0
                                while (attempts < 5) {
                                    val targetVal = tempMap[targetKey]
                                    if (targetVal is JsonObject) {
                                        text = targetVal["text"]?.jsonPrimitive?.content ?: ""
                                        break
                                    } else if (targetVal is String) {
                                        targetKey = targetVal
                                    } else {
                                        break
                                    }
                                    attempts++
                                }
                            }
                            if (text.isNotEmpty()) {
                                processedMap[key] = text
                            }
                        } catch (e: Exception) {
                            Log.w(TAG, "Error resolving entry for key $key: ${e.message}")
                        }
                    }
                }
            }
            
            tafseerData = processedMap
            currentTafseerType = type
            currentTafseerSurah = sura
        } catch (e: Exception) {
            Log.e(TAG, "Critical error loading $type from $fileName: ${e.message}")
            tafseerData = emptyMap()
            currentTafseerType = null
            currentTafseerSurah = null
        }
    }

    suspend fun getPageData(pageNumber: Int): List<Ayah> {
        return ayahDao.getAyahsByPage(pageNumber).map { it.toDomain() }
    }

    fun getTafseer(sura: Int, aya: Int, type: String): String {
        loadTafseer(type, sura)
        val key = "$sura:$aya"
        val result = tafseerData[key]
        return result ?: "No Tafseer available for $key ($type)."
    }

    suspend fun getSurahNameByPage(pageNumber: Int): String {
        return getPageData(pageNumber).firstOrNull()?.sura_name_ar ?: ""
    }

    suspend fun getJuzzByPage(pageNumber: Int): Int {
        return getPageData(pageNumber).firstOrNull()?.jozz ?: 1
    }

    suspend fun getAllSurahs(): List<SurahInfo> {
        if (cachedSurahs != null) return cachedSurahs!!
        val surahs = ayahDao.getAllAyahs()
            .distinctBy { it.sura_no }
            .map {
                SurahInfo(
                    number = it.sura_no,
                    nameEn = it.sura_name_en,
                    nameAr = it.sura_name_ar,
                    startPage = it.page
                )
            }
        cachedSurahs = surahs
        return surahs
    }

    suspend fun getAllJuzz(): List<JuzzInfo> {
        if (cachedJuzz != null) return cachedJuzz!!
        val juzz = ayahDao.getAllAyahs()
            .distinctBy { it.jozz }
            .map {
                JuzzInfo(
                    number = it.jozz,
                    startPage = it.page
                )
            }
        cachedJuzz = juzz
        return juzz
    }

    suspend fun searchAyahs(query: String): List<Ayah> {
        if (query.isBlank()) return emptyList()
        return ayahDao.searchAyahs(query).map { it.toDomain() }
    }
}
