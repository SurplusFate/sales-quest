package com.salesquest.sales_quest.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.salesquest.sales_quest.data.entity.CustomerEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EventDao {

    @Query("SELECT * FROM customer_events WHERE customerId = :customerId ORDER BY eventTime DESC")
    fun watchByCustomer(customerId: String): Flow<List<CustomerEventEntity>>

    @Query("SELECT * FROM customer_events WHERE customerId = :customerId ORDER BY eventTime DESC")
    suspend fun getByCustomer(customerId: String): List<CustomerEventEntity>

    @Query("SELECT * FROM customer_events")
    suspend fun getAll(): List<CustomerEventEntity>

    @Query(
        "SELECT * FROM customer_events WHERE customerId = :customerId AND eventType = :eventType " +
            "AND eventTime >= :start AND eventTime < :end"
    )
    suspend fun getEventsToday(customerId: String, eventType: String, start: Long, end: Long): List<CustomerEventEntity>

    @Query("SELECT COUNT(*) FROM customer_events WHERE eventType = :eventType AND eventTime >= :start AND eventTime < :end")
    suspend fun countEventToday(eventType: String, start: Long, end: Long): Int

    @Query("SELECT COUNT(*) FROM customer_events WHERE eventType = :eventType AND eventTime >= :start AND eventTime < :end")
    fun watchCountEventToday(eventType: String, start: Long, end: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM customer_events WHERE eventType = :eventType AND eventTime >= :start AND eventTime < :end")
    suspend fun countEventRange(eventType: String, start: Long, end: Long): Int

    @Query("SELECT COUNT(*) FROM customer_events WHERE eventType = :eventType")
    suspend fun countEventTotal(eventType: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: CustomerEventEntity)

    @Query("DELETE FROM customer_events")
    suspend fun clearAll()
}
