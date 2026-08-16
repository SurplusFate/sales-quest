package com.salesquest.sales_quest.data

import java.util.concurrent.atomic.AtomicLong

/**
 * ID 生成器 (与 drift 版微秒时间戳 + 随机后缀逻辑对齐)
 */
object IdGenerator {

    private val counter = AtomicLong(0)

    fun gen(prefix: String): String {
        val now = System.nanoTime()
        val seq = counter.incrementAndGet()
        return "$prefix${now}$seq"
    }
}
