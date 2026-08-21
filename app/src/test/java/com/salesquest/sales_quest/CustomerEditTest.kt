package com.salesquest.sales_quest

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.salesquest.sales_quest.core.AppContainer
import com.salesquest.sales_quest.core.SaveCustomerParams
import com.salesquest.sales_quest.data.AppDatabase
import com.salesquest.sales_quest.data.CustomerStage
import com.salesquest.sales_quest.data.Operator
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * P0 客户编辑修复测试
 *
 * 覆盖:
 * - 编辑单字段时保留其他字段 (name / broadband / subCards / camera / operator / stage)
 * - 删除客户后重建不复用旧编号
 * - 连续创建客户编号唯一且递增
 */
@RunWith(RobolectricTestRunner::class)
class CustomerEditTest {

    private lateinit var db: AppDatabase

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        AppContainer.initForTest(db)
    }

    @After
    fun tearDown() {
        db.close()
    }

    // ================================================================
    // 编辑单字段: 保留其他字段
    // ================================================================

    @Test
    fun editOnlyName_preservesBroadbandSubCardsCameraOperatorStage() = runTest {
        val savedId = AppContainer.saveCustomer(
            SaveCustomerParams(
                name = "张三",
                broadband = true,
                subCards = 2,
                camera = true,
                operator = Operator.MOBILE,
                stage = CustomerStage.WON
            )
        )

        // 仅修改 name, 其余字段为 null (应保留原值)
        AppContainer.saveCustomer(SaveCustomerParams(id = savedId, name = "张三改"))

        val updated = db.customerDao().getById(savedId)
        assertNotNull(updated)
        assertEquals("张三改", updated!!.name)
        assertEquals(true, updated.broadband)
        assertEquals(2, updated.subCards)
        assertEquals(true, updated.camera)
        assertEquals("MOBILE", updated.operator)
        assertEquals("WON", updated.salesStage)
    }

    @Test
    fun editOnlyBroadband_preservesNameSubCardsCamera() = runTest {
        val savedId = AppContainer.saveCustomer(
            SaveCustomerParams(
                name = "李四",
                broadband = true,
                subCards = 3,
                camera = true
            )
        )

        // 仅修改 broadband, 其余字段为 null (应保留原值)
        AppContainer.saveCustomer(SaveCustomerParams(id = savedId, broadband = false))

        val updated = db.customerDao().getById(savedId)
        assertNotNull(updated)
        assertEquals(false, updated!!.broadband)
        assertEquals(3, updated.subCards)
        assertEquals(true, updated.camera)
        assertEquals("李四", updated.name)
    }

    @Test
    fun editOnlySubCards_preservesBroadbandCamera() = runTest {
        val savedId = AppContainer.saveCustomer(
            SaveCustomerParams(
                broadband = true,
                subCards = 2,
                camera = true
            )
        )

        // 仅修改 subCards, 其余字段为 null (应保留原值)
        AppContainer.saveCustomer(SaveCustomerParams(id = savedId, subCards = 5))

        val updated = db.customerDao().getById(savedId)
        assertNotNull(updated)
        assertEquals(5, updated!!.subCards)
        assertEquals(true, updated.broadband)
        assertEquals(true, updated.camera)
    }

    @Test
    fun editOnlyCamera_preservesBroadbandSubCards() = runTest {
        val savedId = AppContainer.saveCustomer(
            SaveCustomerParams(
                broadband = true,
                subCards = 2,
                camera = true
            )
        )

        // 仅修改 camera, 其余字段为 null (应保留原值)
        AppContainer.saveCustomer(SaveCustomerParams(id = savedId, camera = false))

        val updated = db.customerDao().getById(savedId)
        assertNotNull(updated)
        assertEquals(false, updated!!.camera)
        assertEquals(true, updated.broadband)
        assertEquals(2, updated.subCards)
    }

    // ================================================================
    // 客户编号: 删除后不复用旧编号
    // ================================================================

    @Test
    fun deleteCustomerThenRecreate_doesNotReuseOldNumber() = runTest {
        // 创建 3 个无名称客户 (自动编号 #001, #002, #003)
        val id1 = AppContainer.saveCustomer(SaveCustomerParams())
        val id2 = AppContainer.saveCustomer(SaveCustomerParams())
        val id3 = AppContainer.saveCustomer(SaveCustomerParams())

        // 确认第 3 个客户编号为 #003
        val c3 = db.customerDao().getById(id3)
        assertNotNull(c3)
        assertEquals("#003", c3!!.customerNumber)

        // 删除 #003
        AppContainer.deleteCustomer(id3)

        // 再创建一个无名称客户 — 应为 #004, 而非 #003
        val id4 = AppContainer.saveCustomer(SaveCustomerParams())
        val c4 = db.customerDao().getById(id4)
        assertNotNull(c4)
        assertEquals("#004", c4!!.customerNumber)
    }

    // ================================================================
    // 客户编号: 连续创建唯一且递增
    // ================================================================

    @Test
    fun consecutiveCreation_producesUniqueAscendingNumbers() = runTest {
        val ids = (1..5).map { AppContainer.saveCustomer(SaveCustomerParams()) }

        val numbers = ids.map { id ->
            val c = db.customerDao().getById(id)
            assertNotNull(c)
            c!!.customerNumber
        }

        val expected = listOf("#001", "#002", "#003", "#004", "#005")
        assertEquals(expected, numbers)

        // 唯一性
        assertEquals(5, numbers.toSet().size)
        // 升序 (expected 已是升序, assertEquals 已验证顺序)
        assertTrue("编号应升序", numbers.filterNotNull() == numbers.filterNotNull().sorted())
    }

    // ================================================================
    // 编辑 operator / stage: 保留其他字段
    // ================================================================

    @Test
    fun editOnlyOperator_preservesBroadbandSubCards() = runTest {
        val savedId = AppContainer.saveCustomer(
            SaveCustomerParams(
                operator = Operator.MOBILE,
                broadband = true,
                subCards = 2
            )
        )

        // 仅修改 operator, 其余字段为 null (应保留原值)
        AppContainer.saveCustomer(SaveCustomerParams(id = savedId, operator = Operator.UNICOM))

        val updated = db.customerDao().getById(savedId)
        assertNotNull(updated)
        assertEquals("UNICOM", updated!!.operator)
        assertEquals(true, updated.broadband)
        assertEquals(2, updated.subCards)
    }

    @Test
    fun editOnlyStage_preservesBroadband() = runTest {
        val savedId = AppContainer.saveCustomer(
            SaveCustomerParams(
                stage = CustomerStage.NEW,
                broadband = true
            )
        )

        // 仅修改 stage, 其余字段为 null (应保留原值)
        AppContainer.saveCustomer(SaveCustomerParams(id = savedId, stage = CustomerStage.WON))

        val updated = db.customerDao().getById(savedId)
        assertNotNull(updated)
        assertEquals("WON", updated!!.salesStage)
        assertEquals(true, updated.broadband)
    }
}
