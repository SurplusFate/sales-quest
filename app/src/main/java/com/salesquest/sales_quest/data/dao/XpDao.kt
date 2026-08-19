package com.salesquest.sales_quest.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.salesquest.sales_quest.data.entity.XpRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface XpDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertXp(record: XpRecordEntity)

    @Query("SELECT COALESCE(SUM(xp), 0) FROM xp_records")
    suspend fun getTotalXp(): Int

    @Query("SELECT * FROM xp_records")
    suspend fun getAll(): List<XpRecordEntity>

    @Query("SELECT COALESCE(SUM(xp), 0) FROM xp_records")
    fun watchTotalXp(): Flow<Int>

    @Query(
        "SELECT * FROM xp_records WHERE customerId = :customerId AND actionType = :actionType " +
            "AND createdAt >= :start AND createdAt < :end"
    )
    suspend fun getXpForToday(customerId: String, actionType: String, start: Long, end: Long): List<XpRecordEntity>

    @Query("SELECT COALESCE(SUM(xp), 0) FROM xp_records WHERE createdAt >= :start AND createdAt < :end")
    suspend fun getXpToday(start: Long, end: Long): Int

    @Query("SELECT COALESCE(SUM(xp), 0) FROM xp_records WHERE createdAt >= :start AND createdAt < :end")
    fun watchXpToday(start: Long, end: Long): Flow<Int>

    @Query("SELECT * FROM xp_records ORDER BY createdAt DESC LIMIT :limit")
    fun watchRecent(limit: Int = 20): Flow<List<XpRecordEntity>>

    @Query("DELETE FROM xp_records")
    suspend fun clearAll()
}
