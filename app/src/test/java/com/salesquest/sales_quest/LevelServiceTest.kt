package com.salesquest.sales_quest

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.salesquest.sales_quest.core.AppLevels
import com.salesquest.sales_quest.core.LevelConditionType
import com.salesquest.sales_quest.core.LevelRequirement
import com.salesquest.sales_quest.data.AppDatabase
import com.salesquest.sales_quest.data.entity.LevelRequirementEntity
import com.salesquest.sales_quest.services.LevelService
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * 等级晋级条件服务测试 (v2)
 *
 * 核心: 升级必须同时满足 XP + 全部晋级条件 (累计见人/查询/成交等)
 */
@RunWith(RobolectricTestRunner::class)
class LevelServiceTest {

    private lateinit var db: AppDatabase
    private lateinit var service: LevelService

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        service = LevelService(db)
    }

    @After
    fun tearDown() {
        db.close()
    }

    private fun req(level: Int, type: LevelConditionType, threshold: Int) =
        LevelRequirement(level, type, threshold)

    // ================================================================
    // 纯函数: evaluateCurrentLevel
    // ================================================================

    @Test
    fun XP达标但累计见人未达标_不能升到下一级() {
        val reqs = listOf(
            req(2, LevelConditionType.XP, 100),
            req(3, LevelConditionType.XP, 300),
            req(3, LevelConditionType.TOTAL_MEET, 50),
            req(3, LevelConditionType.TOTAL_QUERY, 10),
            req(4, LevelConditionType.XP, 600),
            req(4, LevelConditionType.TOTAL_DEAL, 5)
        )
        // XP 1000 足以到达 Lv3 的 XP 门槛, 但累计见人 10 < 50, 查询 5 < 10; Lv4 需成交 5 也未满足
        val level = LevelService.evaluateCurrentLevel(reqs, totalXp = 1000, totalMeet = 10, totalQuery = 5, totalDeal = 0, streakDays = 0)
        assertEquals(2, level)
    }

    @Test
    fun XP与全部累计条件满足_可以晋级() {
        val reqs = listOf(
            req(2, LevelConditionType.XP, 100),
            req(3, LevelConditionType.XP, 300),
            req(3, LevelConditionType.TOTAL_MEET, 50),
            req(3, LevelConditionType.TOTAL_QUERY, 10),
            req(4, LevelConditionType.XP, 600),
            req(4, LevelConditionType.TOTAL_DEAL, 5)
        )
        val level = LevelService.evaluateCurrentLevel(reqs, totalXp = 1000, totalMeet = 50, totalQuery = 10, totalDeal = 4, streakDays = 0)
        assertEquals(3, level)
    }

    @Test
    fun 无条件等级_仅按XP门槛升级() {
        // 无条件的等级: 只依赖 XP
        val reqs = listOf(req(2, LevelConditionType.XP, 100))
        val level = LevelService.evaluateCurrentLevel(reqs, totalXp = 100, totalMeet = 0, totalQuery = 0, totalDeal = 0, streakDays = 0)
        assertEquals(2, level)
    }

    @Test
    fun 默认晋级条件生效_等级3需XP加累计见人查询() {
        val level = LevelService.evaluateCurrentLevel(
            AppLevels.defaultRequirements,
            totalXp = 500,
            totalMeet = 49,
            totalQuery = 9,
            totalDeal = 0,
            streakDays = 0
        )
        // Lv3 需要 XP 300 + 见人 50 + 查询 10, 均差一点
        assertEquals(2, level)
    }

    @Test
    fun 默认晋级条件_满足后升级到3() {
        val level = LevelService.evaluateCurrentLevel(
            AppLevels.defaultRequirements,
            totalXp = 500,
            totalMeet = 50,
            totalQuery = 10,
            totalDeal = 0,
            streakDays = 0
        )
        assertEquals(3, level)
    }

    @Test
    fun buildProgress_返回距离下一级的各条件进度() {
        val reqs = listOf(
            req(2, LevelConditionType.XP, 100),
            req(3, LevelConditionType.XP, 300),
            req(3, LevelConditionType.TOTAL_MEET, 50)
        )
        val progress = LevelService.buildProgress(reqs, totalXp = 300, totalMeet = 30, totalQuery = 0, totalDeal = 0, streakDays = 0)
        assertEquals(2, progress.currentLevel.level)
        assertEquals(3, progress.nextLevel?.level)
        assertFalse(progress.isMaxLevel)
        assertEquals(2, progress.requirements.size)
        // XP 条件已达标
        assertTrue(progress.requirements.first { it.type == LevelConditionType.XP }.met)
        // 累计见人未达标
        assertFalse(progress.requirements.first { it.type == LevelConditionType.TOTAL_MEET }.met)
        assertEquals(30, progress.requirements.first { it.type == LevelConditionType.TOTAL_MEET }.current)
        assertEquals(50, progress.requirements.first { it.type == LevelConditionType.TOTAL_MEET }.threshold)
    }

    @Test
    fun 最高等级_isMaxLevel为true且无下一级() {
        val reqs = listOf(req(2, LevelConditionType.XP, 100), req(8, LevelConditionType.XP, 6000))
        val progress = LevelService.buildProgress(reqs, totalXp = 10000, totalMeet = 0, totalQuery = 0, totalDeal = 0, streakDays = 0)
        assertEquals(8, progress.currentLevel.level)
        assertTrue(progress.isMaxLevel)
        assertEquals(null, progress.nextLevel)
    }

    // ================================================================
    // DB 集成: 配置的条件生效
    // ================================================================

    @Test
    fun 数据库配置条件_升级判定生效() = runTest {
        db.levelRequirementDao().insert(
            LevelRequirementEntity(id = "t1", level = 3, conditionType = "TOTAL_DEAL", threshold = 5)
        )
        db.levelRequirementDao().insert(
            LevelRequirementEntity(id = "t2", level = 3, conditionType = "XP", threshold = 300)
        )
        val reqs = service.getRequirements()

        // Lv3 有自定义配置 → 使用配置条件 (2 条: TOTAL_DEAL=5, XP=300)
        val lv3Reqs = reqs.filter { it.level == 3 }
        assertEquals(2, lv3Reqs.size)
        assertTrue(lv3Reqs.all { it.level == 3 })
        assertTrue(lv3Reqs.any { it.conditionType == LevelConditionType.TOTAL_DEAL && it.threshold == 5 })
        assertTrue(lv3Reqs.any { it.conditionType == LevelConditionType.XP && it.threshold == 300 })

        // Lv2 无自定义配置 → fallback 到默认条件
        val lv2Reqs = reqs.filter { it.level == 2 }
        assertEquals(AppLevels.defaultRequirements.filter { it.level == 2 }, lv2Reqs)

        // Lv4 无自定义配置 → fallback 到默认条件
        val lv4Reqs = reqs.filter { it.level == 4 }
        assertEquals(AppLevels.defaultRequirements.filter { it.level == 4 }, lv4Reqs)

        // 升级判定: XP 500 达 Lv3 XP 门槛, 但 deal 4 < 5 → 不晋级
        val level = LevelService.evaluateCurrentLevel(reqs, totalXp = 500, totalMeet = 0, totalQuery = 0, totalDeal = 4, streakDays = 0)
        assertEquals(2, level)
        // deal 5 >= 5 → 晋级到 Lv3
        val level2 = LevelService.evaluateCurrentLevel(reqs, totalXp = 500, totalMeet = 0, totalQuery = 0, totalDeal = 5, streakDays = 0)
        assertEquals(3, level2)
    }

    @Test
    fun getProgress_从数据库汇总统计() = runTest {
        db.statsDao().insertStats(
            com.salesquest.sales_quest.data.entity.UserStatEntity(totalXp = 400, currentLevel = 1, streakDays = 3)
        )
        db.settingDao().setInt(com.salesquest.sales_quest.core.SettingsKeys.TOTAL_MEETS, 60)
        db.settingDao().setInt(com.salesquest.sales_quest.core.SettingsKeys.TOTAL_QUERIES, 12)

        val progress = service.getProgress()
        assertEquals(400, progress.totalXp)
        // 默认条件下 Lv3 需 XP300+见人50+查询10, 全部满足
        assertEquals(3, progress.currentLevel.level)
    }

    @Test
    fun getNextLevelRequirements_返回下一级条件() = runTest {
        db.statsDao().insertStats(
            com.salesquest.sales_quest.data.entity.UserStatEntity(totalXp = 50, currentLevel = 1, streakDays = 0)
        )
        val next = service.getNextLevelRequirements()
        assertEquals(2, next.first().level)
        assertTrue(next.any { it.conditionType == LevelConditionType.XP })
    }

    // ================================================================
    // P0 等级判定: 每个条件单独不足时不晋级
    // ================================================================

    @Test
    fun 测试一_XP达标_累计查询不足_不得晋级() {
        // Lv3 需要 XP 300 + 见人 50 + 查询 10
        // XP 500 达标, 见人 60 达标, 查询 9 < 10 → 不晋级
        val level = LevelService.evaluateCurrentLevel(
            AppLevels.defaultRequirements,
            totalXp = 500, totalMeet = 60, totalQuery = 9, totalDeal = 0, streakDays = 0
        )
        assertEquals(2, level)
    }

    @Test
    fun 测试二_XP达标_累计见人不足_不得晋级() {
        // XP 500 达标, 见人 49 < 50 → 不晋级
        val level = LevelService.evaluateCurrentLevel(
            AppLevels.defaultRequirements,
            totalXp = 500, totalMeet = 49, totalQuery = 10, totalDeal = 0, streakDays = 0
        )
        assertEquals(2, level)
    }

    @Test
    fun 测试三_XP达标_累计成交不足_不得晋级() {
        // Lv5 需要 XP 1200 + 累计查询 100 + 累计成交 5
        // XP 1300 达标, 查询 100 达标, 成交 4 < 5 → 不晋级到 Lv5
        val level = LevelService.evaluateCurrentLevel(
            AppLevels.defaultRequirements,
            totalXp = 1300, totalMeet = 200, totalQuery = 100, totalDeal = 4, streakDays = 30
        )
        assertEquals(4, level)
    }

    @Test
    fun 测试四_所有条件全部满足_正常晋级() {
        // Lv3 全部满足: XP 300 + 见人 50 + 查询 10
        val level3 = LevelService.evaluateCurrentLevel(
            AppLevels.defaultRequirements,
            totalXp = 500, totalMeet = 50, totalQuery = 10, totalDeal = 0, streakDays = 0
        )
        assertEquals(3, level3)

        // Lv5 全部满足: XP 1200 + 查询 100 + 成交 5
        val level5 = LevelService.evaluateCurrentLevel(
            AppLevels.defaultRequirements,
            totalXp = 1300, totalMeet = 200, totalQuery = 100, totalDeal = 5, streakDays = 30
        )
        assertEquals(5, level5)
    }

    @Test
    fun 测试五_LevelService判定与buildProgress显示状态一致() {
        // 用相同数据同时验证 evaluateCurrentLevel 和 buildProgress
        val reqs = AppLevels.defaultRequirements
        val xp = 500
        val meet = 60
        val query = 9  // 查询不足
        val deal = 0
        val streak = 0

        val evaluatedLevel = LevelService.evaluateCurrentLevel(reqs, xp, meet, query, deal, streak)
        val progress = LevelService.buildProgress(reqs, xp, meet, query, deal, streak)

        // evaluateCurrentLevel 和 buildProgress 必须返回相同的当前等级
        assertEquals(evaluatedLevel, progress.currentLevel.level)

        // 查询条件在进度中应显示为未达标
        val queryReq = progress.requirements.firstOrNull { it.type == LevelConditionType.TOTAL_QUERY }
        if (queryReq != null) {
            assertFalse(queryReq.met)
        }
    }
}
