package com.salesquest.sales_quest.services

/**
 * 销售漏斗校验器 — 全项目统一的漏斗约束来源
 * 规则: 0 <= 成交 <= 查询 <= 见人
 */
object FunnelValidator {

    /** 抛异常版本 (服务层使用), 校验失败抛出 IllegalArgumentException */
    fun validate(peopleSeen: Int, queries: Int, deals: Int) {
        errorOrNull(peopleSeen, queries, deals)?.let { throw IllegalArgumentException(it) }
    }

    /** 返回错误文案, 合法时返回 null (UI 层使用) */
    fun errorOrNull(peopleSeen: Int, queries: Int, deals: Int): String? = when {
        peopleSeen < 0 || queries < 0 || deals < 0 -> "数字不能为负数"
        queries > peopleSeen -> "查询数不能大于见人数"
        deals > queries -> "成交数不能大于查询数"
        else -> null
    }
}
