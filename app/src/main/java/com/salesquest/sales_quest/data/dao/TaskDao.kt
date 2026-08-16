package com.salesquest.sales_quest.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.salesquest.sales_quest.data.entity.DailyTaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {

    @Query("SELECT * FROM daily_tasks WHERE date = :date ORDER BY target DESC")
    fun watchByDate(date: String): Flow<List<DailyTaskEntity>>

    @Query("SELECT * FROM daily_tasks WHERE date = :date")
    suspend fun getByDate(date: String): List<DailyTaskEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTask(task: DailyTaskEntity)

    @Query("UPDATE daily_tasks SET progress = :progress, completed = :completed WHERE id = :id")
    suspend fun updateProgress(id: String, progress: Int, completed: Boolean)

    @Query("DELETE FROM daily_tasks WHERE date = :date")
    suspend fun deleteByDate(date: String)

    @Query("DELETE FROM daily_tasks")
    suspend fun clearAll()
}
