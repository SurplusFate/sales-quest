package com.salesquest.sales_quest.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 执行记录实体 — 记录一天中不同执行阶段的新增数据
 *
 * 核心规则:
 * - 每条记录只保存 "新增量" (delta), 不是当天累计
 * - 当天累计 = 该日期所有记录之和
 * - recordTime 由 App 自动记录 (正常记录使用当前时间)
 * - timePrecision 区分精确时间 / 模糊时段 / 当天总量
 *
 * @param id 主键
 * @param dateKey 日期 yyyy-MM-dd (设备本地日期)
 * @param recordTime 记录时间戳 (EXACT/PERIOD 使用, DAILY_TOTAL 为 null)
 * @param timePrecision 时间精度: EXACT / PERIOD / DAILY_TOTAL
 * @param periodLabel 时段标签 (PERIOD 使用, 如 "上午" / "下午"; 其他为 null)
 * @param peopleSeen 新增见人数
 * @param queries 新增查询数
 * @param deals 新增成交数
 * @param createdAt 创建时间
 * @param updatedAt 更新时间
 */
@Entity(
    tableName = "execution_records",
    indices = [Index("dateKey")]
)
data class ExecutionRecordEntity(
    @PrimaryKey val id: String,
    val dateKey: String,
    val recordTime: Long?,
    val timePrecision: String,
    val periodLabel: String?,
    val peopleSeen: Int,
    val queries: Int,
    val deals: Int,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
