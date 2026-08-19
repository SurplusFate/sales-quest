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
 * 运营商
 */
enum class Operator(val code: String, val label: String) {
    MOBILE("MOBILE", "移动"),
    UNICOM("UNICOM", "联通"),
    TELECOM("TELECOM", "电信"),
    UNKNOWN("UNKNOWN", "不清楚");

    companion object {
        fun fromCode(code: String): Operator =
            entries.firstOrNull { it.code == code } ?: UNKNOWN
    }
}

/**
 * 客户状态 (简化版 - 只有值得跟进的客户才进客户池)
 */
enum class CustomerStage(val code: String, val label: String) {
    NEW("NEW", "待跟进"),
    CONTACTED("CONTACTED", "已联系"),
    QUERIED("QUERIED", "已查询"),
    WON("WON", "已成交"),
    FOLLOW_UP("FOLLOW_UP", "跟进中");

    companion object {
        fun fromCode(code: String): CustomerStage =
            entries.firstOrNull { it.code == code } ?: NEW
    }
}

/**
 * 跟进时间选项
 */
enum class FollowUpOption(val label: String) {
    TODAY("今天"),
    TOMORROW("明天"),
    THREE_DAYS("3天后"),
    SEVEN_DAYS("7天后"),
    CUSTOM("自定义")
}

/**
 * 任务类型 (V1 只有基础任务)
 */
enum class TaskTier(val label: String) {
    BASIC("基础任务"),
    ADVANCED("进阶任务"),
    CHALLENGE("挑战任务")
}
