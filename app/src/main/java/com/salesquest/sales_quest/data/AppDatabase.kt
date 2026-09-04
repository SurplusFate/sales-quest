package com.salesquest.sales_quest.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.room.withTransaction
import androidx.sqlite.db.SupportSQLiteDatabase
import com.salesquest.sales_quest.data.dao.AchievementDao
import com.salesquest.sales_quest.data.dao.CustomerDao
import com.salesquest.sales_quest.data.dao.DailySummaryDao
import com.salesquest.sales_quest.data.dao.EventDao
import com.salesquest.sales_quest.data.dao.ExecutionRecordDao
import com.salesquest.sales_quest.data.dao.FollowUpDao
import com.salesquest.sales_quest.data.dao.LevelRequirementDao
import com.salesquest.sales_quest.data.dao.SettingDao
import com.salesquest.sales_quest.data.dao.StatsDao
import com.salesquest.sales_quest.data.dao.TaskDao
import com.salesquest.sales_quest.data.dao.XpDao
import com.salesquest.sales_quest.data.entity.AchievementEntity
import com.salesquest.sales_quest.data.entity.CustomerEntity
import com.salesquest.sales_quest.data.entity.CustomerEventEntity
import com.salesquest.sales_quest.data.entity.DailySummaryEntity
import com.salesquest.sales_quest.data.entity.DailyTaskEntity
import com.salesquest.sales_quest.data.entity.ExecutionRecordEntity
import com.salesquest.sales_quest.data.entity.FollowUpEntity
import com.salesquest.sales_quest.data.entity.LevelRequirementEntity
import com.salesquest.sales_quest.data.entity.SettingEntity
import com.salesquest.sales_quest.data.entity.UserStatEntity
import com.salesquest.sales_quest.data.entity.XpRecordEntity

@Database(
    entities = [
        CustomerEntity::class,
        CustomerEventEntity::class,
        XpRecordEntity::class,
        FollowUpEntity::class,
        DailyTaskEntity::class,
        UserStatEntity::class,
        AchievementEntity::class,
        SettingEntity::class,
        LevelRequirementEntity::class,
        DailySummaryEntity::class,
        ExecutionRecordEntity::class
    ],
    version = 4,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun customerDao(): CustomerDao
    abstract fun eventDao(): EventDao
    abstract fun xpDao(): XpDao
    abstract fun followUpDao(): FollowUpDao
    abstract fun taskDao(): TaskDao
    abstract fun statsDao(): StatsDao
    abstract fun achievementDao(): AchievementDao
    abstract fun settingDao(): SettingDao
    abstract fun levelRequirementDao(): LevelRequirementDao
    abstract fun dailySummaryDao(): DailySummaryDao
    abstract fun executionRecordDao(): ExecutionRecordDao

    /** 清空所有数据并重置统计 (设置页-清除所有数据) */
    suspend fun clearAllData() {
        withTransaction {
            customerDao().clearAll()
            eventDao().clearAll()
            xpDao().clearAll()
            followUpDao().clearAll()
            taskDao().clearAll()
            achievementDao().clearAll()
            settingDao().clearAll()
            levelRequirementDao().clearAll()
            dailySummaryDao().clearAll()
            executionRecordDao().clearAll()
            statsDao().updateStats(
                totalXp = 0,
                currentLevel = 1,
                streakDays = 0,
                lastActiveDate = null,
                updatedAt = System.currentTimeMillis()
            )
        }
    }

    companion object {
        const val VERSION = 4
        const val DB_NAME = "sales_quest.db"

        /** v1 → v2: 新增 level_requirements 与 daily_summaries 表 */
        val MIGRATION_1_2: Migration = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `level_requirements` (" +
                        "`id` TEXT NOT NULL, " +
                        "`level` INTEGER NOT NULL, " +
                        "`conditionType` TEXT NOT NULL, " +
                        "`threshold` INTEGER NOT NULL, " +
                        "`createdAt` INTEGER NOT NULL, " +
                        "PRIMARY KEY(`id`))"
                )
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `daily_summaries` (" +
                        "`dateKey` TEXT NOT NULL, " +
                        "`good` TEXT NOT NULL, " +
                        "`problems` TEXT NOT NULL, " +
                        "`customerFeedback` TEXT NOT NULL, " +
                        "`discovery` TEXT NOT NULL, " +
                        "`improvement` TEXT NOT NULL, " +
                        "`updatedAt` INTEGER NOT NULL, " +
                        "PRIMARY KEY(`dateKey`))"
                )
            }
        }

        /** v2 → v3: 新增 customerNumber 字段 (UNIQUE), 编号不再依赖客户数量 */
        val MIGRATION_2_3: Migration = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE customers ADD COLUMN customerNumber TEXT")
                db.execSQL("CREATE UNIQUE INDEX index_customers_customerNumber ON customers(customerNumber) WHERE customerNumber IS NOT NULL")
                // 仅将"以 # 开头且名字唯一"的存量客户迁移为编号; 名字重复的 # 客户保留 NULL,
                // 否则同名客户会同时写入相同 customerNumber, 触发唯一约束导致升级崩溃
                db.execSQL(
                    "UPDATE customers SET customerNumber = name " +
                        "WHERE name LIKE '#%' AND customerNumber IS NULL " +
                        "AND (SELECT COUNT(*) FROM customers c2 WHERE c2.name = customers.name) = 1"
                )
            }
        }

        /** v3 → v4: 新增 execution_records 表 (分段执行记录) */
        val MIGRATION_3_4: Migration = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `execution_records` (" +
                        "`id` TEXT NOT NULL, " +
                        "`dateKey` TEXT NOT NULL, " +
                        "`recordTime` INTEGER, " +
                        "`timePrecision` TEXT NOT NULL, " +
                        "`periodLabel` TEXT, " +
                        "`peopleSeen` INTEGER NOT NULL, " +
                        "`queries` INTEGER NOT NULL, " +
                        "`deals` INTEGER NOT NULL, " +
                        "`createdAt` INTEGER NOT NULL, " +
                        "`updatedAt` INTEGER NOT NULL, " +
                        "PRIMARY KEY(`id`))"
                )
                db.execSQL(
                    "CREATE INDEX IF NOT EXISTS `index_execution_records_dateKey` " +
                        "ON `execution_records` (`dateKey`)"
                )
            }
        }

        fun build(context: Context): AppDatabase {
            return Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, DB_NAME)
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                .build()
        }
    }
}
