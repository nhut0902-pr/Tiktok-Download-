package com.example.data.local

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "download_items")
data class DownloadItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tikTokId: String,
    val title: String,
    val authorName: String,
    val authorHandle: String,
    val thumbnailUrl: String,
    val mediaType: String, // "VIDEO", "PHOTO", "AUDIO"
    val qualityLabel: String, // "FULL HD 1080P", "HD 720P", "ẢNH HD KHÔNG LOGO", "ẢNH BÌA HD", "MP3 320K"
    val resolution: String,
    val isNoWatermark: Boolean,
    val fileSizeBytes: Long,
    val localFilePath: String,
    val galleryUriString: String,
    val remoteStreamUrl: String,
    val sourceTikTokUrl: String,
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Dao
interface DownloadHistoryDao {
    @Query("SELECT * FROM download_items ORDER BY createdAt DESC")
    fun getAllDownloads(): Flow<List<DownloadItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDownload(item: DownloadItemEntity): Long

    @Query("UPDATE download_items SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setFavorite(id: Long, isFavorite: Boolean)

    @Query("DELETE FROM download_items WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM download_items")
    suspend fun clearAll()
}

@Database(entities = [DownloadItemEntity::class], version = 2, exportSchema = false)
abstract class DownloadHistoryDatabase : RoomDatabase() {
    abstract fun downloadHistoryDao(): DownloadHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: DownloadHistoryDatabase? = null

        fun getInstance(context: Context): DownloadHistoryDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DownloadHistoryDatabase::class.java,
                    "tikstudio_downloads.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
