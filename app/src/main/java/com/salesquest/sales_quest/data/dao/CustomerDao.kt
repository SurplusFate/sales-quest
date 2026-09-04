package com.salesquest.sales_quest.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.salesquest.sales_quest.data.entity.CustomerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {

    @Query("SELECT * FROM customers ORDER BY updatedAt DESC")
    fun watchAll(): Flow<List<CustomerEntity>>

    @Query(
        "SELECT * FROM customers WHERE nextFollowUpAt >= :start AND nextFollowUpAt < :end ORDER BY nextFollowUpAt"
    )
    fun watchTodayFollowUps(start: Long, end: Long): Flow<List<CustomerEntity>>

    @Query("SELECT * FROM customers ORDER BY updatedAt DESC")
    suspend fun getAll(): List<CustomerEntity>

    @Query("SELECT * FROM customers WHERE id = :id")
    suspend fun getById(id: String): CustomerEntity?

    @Query("SELECT * FROM customers WHERE id = :id")
    fun watchById(id: String): Flow<CustomerEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: CustomerEntity)

    @Update
    suspend fun updateCustomer(customer: CustomerEntity)

    @Query("DELETE FROM customers WHERE id = :id")
    suspend fun deleteCustomer(id: String)

    @Query("DELETE FROM customers")
    suspend fun clearAll()

    /**
     * 查询当前最大客户编号 (如 #005 → "#005"), 不依赖客户数量
     * 按数值语义排序: 先按字符串长度(位数)降序, 同长度再按字典序,
     * 避免字典序缺陷 ("#999" > "#1000") 导致编号回退/重复
     */
    @Query(
        "SELECT customerNumber FROM customers WHERE customerNumber IS NOT NULL AND customerNumber LIKE '#%' " +
            "ORDER BY LENGTH(customerNumber) DESC, customerNumber DESC LIMIT 1"
    )
    suspend fun getMaxCustomerNumber(): String?
}
