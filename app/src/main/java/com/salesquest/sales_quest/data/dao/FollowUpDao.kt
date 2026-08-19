package com.salesquest.sales_quest.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.salesquest.sales_quest.data.entity.FollowUpEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FollowUpDao {

    @Query("SELECT * FROM follow_ups WHERE customerId = :customerId ORDER BY scheduledAt DESC")
    fun watchByCustomer(customerId: String): Flow<List<FollowUpEntity>>

    @Query(
        "SELECT * FROM follow_ups WHERE scheduledAt >= :start AND scheduledAt < :end AND completed = 0 " +
            "ORDER BY scheduledAt"
    )
    fun watchToday(start: Long, end: Long): Flow<List<FollowUpEntity>>

    @Query(
        "SELECT * FROM follow_ups WHERE scheduledAt >= :from AND completed = 0 ORDER BY scheduledAt"
    )
    fun watchUpcoming(from: Long): Flow<List<FollowUpEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFollowUp(followUp: FollowUpEntity)

    @Query("SELECT * FROM follow_ups")
    suspend fun getAll(): List<FollowUpEntity>

    @Query("UPDATE follow_ups SET completed = 1, completedAt = :completedAt WHERE id = :id")
    suspend fun markCompleted(id: String, completedAt: Long)

    @Query("DELETE FROM follow_ups")
    suspend fun clearAll()
}
