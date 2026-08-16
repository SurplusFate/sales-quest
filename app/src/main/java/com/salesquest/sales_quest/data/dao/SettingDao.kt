package com.salesquest.sales_quest.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.salesquest.sales_quest.data.entity.SettingEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class SettingDao {

    @Query("SELECT value FROM settings WHERE key = :key")
    abstract suspend fun get(key: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun set(setting: SettingEntity)

    @Query("SELECT * FROM settings")
    abstract suspend fun getAll(): List<SettingEntity>

    @Query("SELECT * FROM settings")
    abstract fun watchAll(): Flow<List<SettingEntity>>

    @Query("SELECT value FROM settings WHERE key = :key")
    abstract fun watchValue(key: String): Flow<String?>

    @Query("DELETE FROM settings WHERE key = :key")
    abstract suspend fun remove(key: String)

    @Query("DELETE FROM settings")
    abstract suspend fun clearAll()

    suspend fun getInt(key: String): Int {
        val v = get(key)
        return v?.toIntOrNull() ?: 0
    }

    suspend fun setInt(key: String, value: Int) {
        set(SettingEntity(key = key, value = value.toString()))
    }
}
