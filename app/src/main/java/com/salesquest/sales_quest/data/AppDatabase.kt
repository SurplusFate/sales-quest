package com.salesquest.sales_quest.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.withTransaction
import com.salesquest.sales_quest.data.dao.AchievementDao
import com.salesquest.sales_quest.data.dao.CustomerDao
import com.salesquest.sales_quest.data.dao.EventDao
import com.salesquest.sales_quest.data.dao.FollowUpDao
import com.salesquest.sales_quest.data.dao.SettingDao
import com.salesquest.sales_quest.data.dao.StatsDao
import com.salesquest.sales_quest.data.dao.TaskDao
import com.salesquest.sales_quest.data.dao.XpDao
import com.salesquest.sales_quest.data.entity.AchievementEntity
import com.salesquest.sales_quest.data.entity.CustomerEntity
import com.salesquest.sales_quest.data.entity.CustomerEventEntity
import com.salesquest.sales_quest.data.entity.DailyTaskEntity
import com.salesquest.sales_quest.data.entity.FollowUpEntity
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
        SettingEntity::class
    ],
    version = 1,
    exportSchema = false
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
        const val DB_NAME = "sales_quest.db"

        fun build(context: Context): AppDatabase {
            return Room.databaseBuilder(context.applicationContext, AppDatabase::class.java, DB_NAME)
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}
