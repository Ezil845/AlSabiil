package al.sabil.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import al.sabil.data.entity.AzkarEntity

@Dao
interface AzkarDao {
    @Query("SELECT * FROM azkar WHERE category = :category")
    suspend fun getAzkarByCategory(category: String): List<AzkarEntity>

    @Query("SELECT DISTINCT category FROM azkar")
    suspend fun getAllCategories(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(azkar: List<AzkarEntity>)
    
    @Query("SELECT COUNT(*) FROM azkar")
    suspend fun getAzkarCount(): Int
}
