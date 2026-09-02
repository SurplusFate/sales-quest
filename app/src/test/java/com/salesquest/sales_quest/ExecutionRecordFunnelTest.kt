package com.salesquest.sales_quest

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.salesquest.sales_quest.core.AppContainer
import com.salesquest.sales_quest.core.SettingsKeys
import com.salesquest.sales_quest.data.AppDatabase
import com.salesquest.sales_quest.services.ExecutionRecordService
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * 分段执行记录销售漏斗校验测试
 *
 * 覆盖: 单条记录违规 / 累加结果违规 (兜底回滚) / 基准记录创建 / 编辑重算 / 删除重算
 */
@RunWith(RobolectricTestRunner::class)
class ExecutionRecordFunnelTest {

    private lateinit var db: AppDatabase
    private lateinit var service: ExecutionRecordService

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        AppContainer.initForTest(db)
        service = ExecutionRecordService(db, null, {})
    }

    @After
    fun tearDown() {
        db.close()
    }

    private val dateKey = "2026-09-02"

    private suspend fun assertFunnelRejected(block: suspend () -> Unit) {
        var thrown = false
        try {
            block()
        } catch (e: IllegalArgumentException) {
            thrown = true
        }
        assertTrue("应抛出 IllegalArgumentException", thrown)
    }

    @Test
    fun addRecord_单条记录违反漏斗约束时拒绝() = runTest {
        // 1 人 / 2 查询 / 3 成交 — 违反 成交<=查询<=见人
        assertFunnelRejected {
            service.addRecord(dateKey, null, ExecutionRecordService.PRECISION_EXACT, null, 1, 2, 3)
        }
        // settings 未被污染
        assertEquals(0, db.settingDao().getInt(SettingsKeys.peopleSeen(dateKey)))
        assertEquals(0, db.settingDao().getInt(SettingsKeys.queries(dateKey)))
        assertEquals(0, db.settingDao().getInt(SettingsKeys.deals(dateKey)))
        assertEquals(0, service.getRecords(dateKey).size)
    }

    @Test
    fun addRecord_合法记录成功并写入settings() = runTest {
        service.addRecord(dateKey, null, ExecutionRecordService.PRECISION_EXACT, null, 5, 3, 2)

        assertEquals(5, db.settingDao().getInt(SettingsKeys.peopleSeen(dateKey)))
        assertEquals(3, db.settingDao().getInt(SettingsKeys.queries(dateKey)))
        assertEquals(2, db.settingDao().getInt(SettingsKeys.deals(dateKey)))
        assertEquals(1, service.getRecords(dateKey).size)
    }

    @Test
    fun addRecord_settings已有数据时自动创建基准记录() = runTest {
        db.settingDao().setInt(SettingsKeys.peopleSeen(dateKey), 10)
        db.settingDao().setInt(SettingsKeys.queries(dateKey), 5)
        db.settingDao().setInt(SettingsKeys.deals(dateKey), 2)

        service.addRecord(dateKey, null, ExecutionRecordService.PRECISION_EXACT, null, 1, 1, 1)

        val records = service.getRecords(dateKey)
        assertEquals(2, records.size)
        assertTrue(records.any { it.timePrecision == ExecutionRecordService.PRECISION_DAILY_TOTAL })
        // 累加: 10+1 / 5+1 / 2+1
        assertEquals(11, db.settingDao().getInt(SettingsKeys.peopleSeen(dateKey)))
        assertEquals(6, db.settingDao().getInt(SettingsKeys.queries(dateKey)))
        assertEquals(3, db.settingDao().getInt(SettingsKeys.deals(dateKey)))
    }

    @Test
    fun addRecord_累加结果违反漏斗时拦截并回滚() = runTest {
        // 模拟历史污染: settings 已是非法数据 (1/2/3)
        db.settingDao().setInt(SettingsKeys.peopleSeen(dateKey), 1)
        db.settingDao().setInt(SettingsKeys.queries(dateKey), 2)
        db.settingDao().setInt(SettingsKeys.deals(dateKey), 3)

        // 单条记录合法 (1/1/1), 但累加 2/3/4 违规 → 兜底拦截并整体回滚
        assertFunnelRejected {
            service.addRecord(dateKey, null, ExecutionRecordService.PRECISION_EXACT, null, 1, 1, 1)
        }
        // settings 保持原值 (事务回滚)
        assertEquals(1, db.settingDao().getInt(SettingsKeys.peopleSeen(dateKey)))
        assertEquals(2, db.settingDao().getInt(SettingsKeys.queries(dateKey)))
        assertEquals(3, db.settingDao().getInt(SettingsKeys.deals(dateKey)))
        // 基准记录与新记录均未写入
        assertEquals(0, service.getRecords(dateKey).size)
    }

    @Test
    fun updateRecord_修改为违规数据时拒绝() = runTest {
        val id = service.addRecord(dateKey, null, ExecutionRecordService.PRECISION_EXACT, null, 5, 3, 2)

        assertFunnelRejected {
            service.updateRecord(id, 5, 9, 2)
        }
        // 原值保留
        assertEquals(5, db.settingDao().getInt(SettingsKeys.peopleSeen(dateKey)))
        assertEquals(3, db.settingDao().getInt(SettingsKeys.queries(dateKey)))
        assertEquals(2, db.settingDao().getInt(SettingsKeys.deals(dateKey)))
    }

    @Test
    fun updateRecord_合法修改后重算settings() = runTest {
        val id = service.addRecord(dateKey, null, ExecutionRecordService.PRECISION_EXACT, null, 5, 3, 2)

        service.updateRecord(id, 8, 4, 3)

        assertEquals(8, db.settingDao().getInt(SettingsKeys.peopleSeen(dateKey)))
        assertEquals(4, db.settingDao().getInt(SettingsKeys.queries(dateKey)))
        assertEquals(3, db.settingDao().getInt(SettingsKeys.deals(dateKey)))
    }

    @Test
    fun deleteRecord_删除后重算settings() = runTest {
        service.addRecord(dateKey, null, ExecutionRecordService.PRECISION_EXACT, null, 5, 3, 2)
        val id2 = service.addRecord(dateKey, null, ExecutionRecordService.PRECISION_EXACT, null, 3, 2, 1)
        // 累加 8/5/3
        assertEquals(8, db.settingDao().getInt(SettingsKeys.peopleSeen(dateKey)))

        service.deleteRecord(id2)

        assertEquals(5, db.settingDao().getInt(SettingsKeys.peopleSeen(dateKey)))
        assertEquals(3, db.settingDao().getInt(SettingsKeys.queries(dateKey)))
        assertEquals(2, db.settingDao().getInt(SettingsKeys.deals(dateKey)))
    }
}
