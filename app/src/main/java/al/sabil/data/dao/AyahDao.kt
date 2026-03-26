package al.sabil.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import al.sabil.data.entity.AyahEntity

@Dao
interface AyahDao {
    @Query("SELECT * FROM ayahs WHERE page = :page ORDER BY id ASC")
    suspend fun getAyahsByPage(page: Int): List<AyahEntity>

    @Query("SELECT * FROM ayahs WHERE aya_text_emlaey LIKE '%' || :query || '%' OR aya_text LIKE '%' || :query || '%'")
    suspend fun searchAyahs(query: String): List<AyahEntity>

    @Query("SELECT * FROM ayahs ORDER BY id ASC")
    suspend fun getAllAyahs(): List<AyahEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(ayahs: List<AyahEntity>)

    @Query("SELECT COUNT(*) FROM ayahs")
    suspend fun getAyahCount(): Int
}
