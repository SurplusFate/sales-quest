package com.salesquest.sales_quest.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.salesquest.sales_quest.data.entity.UserStatEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StatsDao {

    @Query("SELECT * FROM user_stats WHERE id = 'default'")
    suspend fun getStats(): UserStatEntity?

    @Query("SELECT * FROM user_stats WHERE id = 'default'")
    fun watchStats(): Flow<UserStatEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStats(stats: UserStatEntity)

    @Query(
        "UPDATE user_stats SET totalXp = :totalXp, currentLevel = :currentLevel, " +
            "streakDays = :streakDays, lastActiveDate = :lastActiveDate, updatedAt = :updatedAt WHERE id = 'default'"
    )
    suspend fun updateStats(
        totalXp: Int,
        currentLevel: Int,
        streakDays: Int,
        lastActiveDate: Long?,
        updatedAt: Long
    )
}
