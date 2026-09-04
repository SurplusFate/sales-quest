package com.salesquest.sales_quest.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.salesquest.sales_quest.data.entity.ExecutionRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class ExecutionRecordDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insert(record: ExecutionRecordEntity)

    @Update
    abstract suspend fun update(record: ExecutionRecordEntity)

    @Query("DELETE FROM execution_records WHERE id = :id")
    abstract suspend fun delete(id: String)

    @Query("SELECT * FROM execution_records WHERE id = :id")
    abstract suspend fun getById(id: String): ExecutionRecordEntity?

    @Query("SELECT * FROM execution_records WHERE dateKey = :dateKey ORDER BY COALESCE(recordTime, 0) ASC, createdAt ASC")
    abstract suspend fun getByDate(dateKey: String): List<ExecutionRecordEntity>

    @Query("SELECT * FROM execution_records WHERE dateKey = :dateKey ORDER BY COALESCE(recordTime, 0) ASC, createdAt ASC")
    abstract fun watchByDate(dateKey: String): Flow<List<ExecutionRecordEntity>>

    @Query("SELECT COUNT(*) FROM execution_records WHERE dateKey = :dateKey")
    abstract suspend fun countByDate(dateKey: String): Int

    @Query("SELECT DISTINCT dateKey FROM execution_records ORDER BY dateKey DESC")
    abstract suspend fun getAllDates(): List<String>

    @Query("DELETE FROM execution_records WHERE dateKey = :dateKey")
    abstract suspend fun deleteByDateKey(dateKey: String)

    @Query("DELETE FROM execution_records")
    abstract suspend fun clearAll()
}
