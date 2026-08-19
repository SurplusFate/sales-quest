package com.salesquest.sales_quest.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.salesquest.sales_quest.data.entity.LevelRequirementEntity

@Dao
interface LevelRequirementDao {

    @Query("SELECT * FROM level_requirements ORDER BY level ASC, conditionType ASC")
    suspend fun getAll(): List<LevelRequirementEntity>

    @Query("SELECT * FROM level_requirements WHERE level = :level")
    suspend fun getForLevel(level: Int): List<LevelRequirementEntity>

    @Query("SELECT * FROM level_requirements")
    fun watchAll(): kotlinx.coroutines.flow.Flow<List<LevelRequirementEntity>>

    @Query("DELETE FROM level_requirements WHERE level = :level")
    suspend fun deleteForLevel(level: Int)

    @Query("DELETE FROM level_requirements")
    suspend fun clearAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(requirement: LevelRequirementEntity)
}
