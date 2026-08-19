package com.salesquest.sales_quest.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.salesquest.sales_quest.data.entity.DailySummaryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailySummaryDao {

    @Query("SELECT * FROM daily_summaries WHERE dateKey = :dateKey")
    suspend fun get(dateKey: String): DailySummaryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(summary: DailySummaryEntity)

    @Query("SELECT * FROM daily_summaries ORDER BY dateKey DESC")
    suspend fun getAll(): List<DailySummaryEntity>

    @Query("SELECT * FROM daily_summaries ORDER BY dateKey DESC")
    fun watchAll(): Flow<List<DailySummaryEntity>>

    @Query("DELETE FROM daily_summaries WHERE dateKey = :dateKey")
    suspend fun delete(dateKey: String)

    @Query("DELETE FROM daily_summaries")
    suspend fun clearAll()
}
