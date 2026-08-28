package dev.ryanness.lighteuchre.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "progress")
data class ProgressEntity(
    @PrimaryKey val id: Int = 0,
    val highestUnlockedLesson: Int,
    val completedLessons: String, // comma-separated lesson ids
    val gamesPlayed: Int,
    val gamesWon: Int,
)

@Dao
interface ProgressDao {
    @Query("SELECT * FROM progress WHERE id = 0")
    fun observe(): Flow<ProgressEntity?>

    @Query("SELECT * FROM progress WHERE id = 0")
    suspend fun get(): ProgressEntity?

    @Upsert
    suspend fun upsert(entity: ProgressEntity)
}

@Database(entities = [ProgressEntity::class], version = 1, exportSchema = false)
abstract class ProgressDatabase : RoomDatabase() {
    abstract fun progressDao(): ProgressDao
}
