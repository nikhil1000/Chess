package com.example.data

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

@Entity(tableName = "match_records")
data class MatchRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val difficultyLabel: String,
    val resultTitle: String,
    val winnerColor: String, // "WHITE", "BLACK", "DRAW"
    val totalMoves: Int,
    val totalCaptures: Int,
    val pgnSummary: String
)

@Dao
interface MatchRecordDao {
    @Query("SELECT * FROM match_records ORDER BY timestamp DESC LIMIT 30")
    fun getRecentMatches(): Flow<List<MatchRecordEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMatch(record: MatchRecordEntity)

    @Query("DELETE FROM match_records")
    suspend fun clearAllMatches()
}

@Database(
    entities = [MatchRecordEntity::class],
    version = 1,
    exportSchema = false
)
abstract class ArenaDatabase : RoomDatabase() {
    abstract fun matchRecordDao(): MatchRecordDao

    companion object {
        @Volatile
        private var INSTANCE: ArenaDatabase? = null

        fun getInstance(context: Context): ArenaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ArenaDatabase::class.java,
                    "chess_arena_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
