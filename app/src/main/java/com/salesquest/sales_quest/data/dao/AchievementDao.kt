package com.salesquest.sales_quest.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.salesquest.sales_quest.data.entity.AchievementEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AchievementDao {

    @Query("SELECT * FROM achievements ORDER BY unlockedAt DESC")
    fun watchAll(): Flow<List<AchievementEntity>>

    @Query("SELECT * FROM achievements")
    suspend fun getAll(): List<AchievementEntity>

    @Query("SELECT COUNT(*) FROM achievements WHERE achievementId = :achievementId")
    suspend fun isUnlocked(achievementId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun unlock(achievement: AchievementEntity)

    @Query("DELETE FROM achievements")
    suspend fun clearAll()
}
