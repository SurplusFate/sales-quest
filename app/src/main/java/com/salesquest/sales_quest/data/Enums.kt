package com.salesquest.sales_quest.data

/**
 * V1.0.0 核心指标 - 只有三个每日核心数据
 */
enum class CoreMetric(val code: String, val label: String) {
    MEET("MEET", "见人"),
    QUERY("QUERY", "查询"),
    DEAL("DEAL", "成交")
}

/**
 * 任务类型 (V1 只有基础任务)
 */
enum class TaskTier(val label: String) {
    BASIC("基础任务"),
    ADVANCED("进阶任务"),
    CHALLENGE("挑战任务")
}
