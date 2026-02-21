package com.example.alsabiil.repository

import android.content.Context
import android.util.Log
import com.example.alsabiil.model.Ayah
import com.example.alsabiil.model.JuzzInfo
import com.example.alsabiil.model.SurahInfo
import kotlinx.serialization.json.*

private const val TAG = "QuranRepository"

class QuranRepository(private val context: Context) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }
    
    private val hafsData: List<Ayah> by lazy {
        try {
            context.assets.open("data/hafs_smart_v8.json").use { inputStream ->
                val jsonString = inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
                val data = json.decodeFromString<List<Ayah>>(jsonString)
                Log.d(TAG, "Loaded ${data.size} ayahs from JSON")
                if (data.isNotEmpty()) {
                    Log.d(TAG, "First ayah: ID=${data[0].id}, Text='${data[0].aya_text.take(30)}'")
                }
                data
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading Quran data: ${e.message}")
            e.printStackTrace()
            emptyList()
        }
    }

    init {
        Log.d(TAG, "QuranRepository initialized")
        Log.d(TAG, "First ayah text: '${hafsData.firstOrNull()?.aya_text?.take(30)}'")
    }
    
    private var currentTafseerType: String? = null
    private var currentTafseerSurah: Int? = null
    private var tafseerData: Map<String, String> = emptyMap()

    /**
     * Loads tafseer data. 
     * For "ibn_kathir", it loads specific surah files (split to avoid OOM).
     * For other types (e.g., saddi), it loads the full file.
     */
    private fun loadTafseer(type: String, sura: Int) {
        // Optimization: Don't reload if we already have the data for this type (and surah if applicable)
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
        
        Log.d(TAG, "Attempting to load tafseer: $type (Surah $sura) from $fileName")
        
        try {
            // Debug check: list files in directory
            if (type == "ibn_kathir") {
                val list = context.assets.list("tafseer/ar-tafseer-ibn-kathir")
                Log.d(TAG, "Files in tafseer/ar-tafseer-ibn-kathir: ${list?.size ?: -1}. Example: ${list?.firstOrNull()}")
            }

            val processedMap = mutableMapOf<String, String>()
            
            context.assets.open(fileName).use { inputStream ->
                val jsonString = inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
                Log.d(TAG, "Loaded JSON string length: ${jsonString.length}. First 50 chars: '${jsonString.take(50)}'")
                
                // For Ibn Kathir, the split files are simple maps.
                // For Saddi, it's a large map.
                // Using lenient JsonElement parsing for robustness.
                val root = json.parseToJsonElement(jsonString).jsonObject
                Log.d(TAG, "Parsed root JSON object. Keys count: ${root.size}")
                
                // Ibn Kathir split files are simple maps: "ayah_number": "text"
                if (type == "ibn_kathir") {
                    for ((key, value) in root) {
                        try {
                            if (value is JsonPrimitive && value.isString) {
                                // Map "1" -> "1:1", "2" -> "1:2" etc.
                                val compositeKey = "$sura:$key"
                                processedMap[compositeKey] = value.content
                            }
                        } catch (e: Exception) {
                            Log.w(TAG, "Skipping invalid entry in Ibn Kathir for key $key: ${e.message}")
                        }
                    }
                    Log.d(TAG, "Parsed Ibn Kathir Surah $sura. Total ayahs: ${processedMap.size}")
                } else {
                    // For Saddi or others, use the existing references logic
                    val tempMap = mutableMapOf<String, Any>() // Holds string or JsonObject
                    
                    // First pass: load everything into tempMap
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
    
                    // Second pass: resolve and populate processedMap
                    for ((key, value) in tempMap) {
                        try {
                            var text = ""
                            
                            if (value is JsonObject) {
                                text = value["text"]?.jsonPrimitive?.content ?: ""
                                // (No longer needed to check ayah_keys for ibn_kathir here as it's handled above)
                            } else if (value is String) {
                                // It's a pointer to another key (e.g., "89:2" -> "89:1")
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
            Log.d(TAG, "Successfully loaded $type (Surah $sura). Entries: ${tafseerData.size}. Key example: ${tafseerData.keys.firstOrNull()}")
        } catch (e: Exception) {
            Log.e(TAG, "Critical error loading $type from $fileName: ${e.message}")
            e.printStackTrace()
            // Don't clear data immediately if just one surah failed, but for safety:
            tafseerData = emptyMap()
            currentTafseerType = null
            currentTafseerSurah = null
        }
    }

    private val pagesMap: Map<Int, List<Ayah>> by lazy {
        hafsData.groupBy { it.page }
    }

    fun getPageData(pageNumber: Int): List<Ayah> {
        return pagesMap[pageNumber] ?: emptyList()
    }

    fun getTafseer(sura: Int, aya: Int, type: String): String {
        loadTafseer(type, sura)
        val key = "$sura:$aya"
        val result = tafseerData[key]
        Log.d(TAG, "getTafseer: sura=$sura, aya=$aya, type=$type, key=$key, found=${result != null}")
        return result ?: "No Tafseer available for $key ($type)."
    }

    fun getSurahNameByPage(pageNumber: Int): String {
        return getPageData(pageNumber).firstOrNull()?.sura_name_ar ?: ""
    }

    fun getJuzzByPage(pageNumber: Int): Int {
        return getPageData(pageNumber).firstOrNull()?.jozz ?: 1
    }

    fun getAllSurahs(): List<SurahInfo> {
        return hafsData.distinctBy { it.sura_no }.map {
            SurahInfo(
                number = it.sura_no,
                nameEn = it.sura_name_en,
                nameAr = it.sura_name_ar,
                startPage = it.page
            )
        }
    }

    fun getAllJuzz(): List<JuzzInfo> {
        return hafsData.distinctBy { it.jozz }.map {
            JuzzInfo(
                number = it.jozz,
                startPage = it.page
            )
        }
    }
}
