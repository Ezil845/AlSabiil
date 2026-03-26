package al.sabil.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import al.sabil.data.entity.AyahEntity
import al.sabil.data.entity.AzkarEntity
import al.sabil.data.entity.BookmarkEntity
import al.sabil.data.dao.AyahDao
import al.sabil.data.dao.AzkarDao
import al.sabil.data.dao.BookmarkDao

@Database(entities = [BookmarkEntity::class, AyahEntity::class, AzkarEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun bookmarkDao(): BookmarkDao
    abstract fun ayahDao(): AyahDao
    abstract fun azkarDao(): AzkarDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "alsabiil_database"
                )
                .fallbackToDestructiveMigration()
                .build().also { INSTANCE = it }
            }
        }
    }
}
