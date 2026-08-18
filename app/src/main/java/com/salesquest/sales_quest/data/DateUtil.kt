package com.salesquest.sales_quest.data

import java.util.Calendar
import java.util.TimeZone

/**
 * 日期工具 (与 drift 版 _dateKey 等保持一致)
 */
object DateUtil {

    /** yyyy-MM-dd */
    fun dateKey(time: Long = System.currentTimeMillis()): String {
        return formatDateKey(Calendar.getInstance().apply { this.timeInMillis = time })
    }

    /** yyyy-MM-dd → UTC 0 点时间戳 (Material DatePicker 使用 UTC millis) */
    fun utcMillis(dateKey: String): Long {
        val parts = dateKey.split("-").map { it.toInt() }
        val c = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        c.clear()
        c.set(parts[0], parts[1] - 1, parts[2])
        return c.timeInMillis
    }

    /** UTC 时间戳 → yyyy-MM-dd */
    fun dateKeyFromUtc(utcMillis: Long): String {
        return formatDateKey(Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { this.timeInMillis = utcMillis })
    }

    private fun formatDateKey(c: Calendar): String {
        val y = c.get(Calendar.YEAR)
        val m = c.get(Calendar.MONTH) + 1
        val d = c.get(Calendar.DAY_OF_MONTH)
        return "%04d-%02d-%02d".format(y, m, d)
    }

    /** 当天 0 点时间戳 */
    fun dayStart(time: Long = System.currentTimeMillis()): Long {
        val c = Calendar.getInstance().apply { this.timeInMillis = time }
        c.set(Calendar.HOUR_OF_DAY, 0)
        c.set(Calendar.MINUTE, 0)
        c.set(Calendar.SECOND, 0)
        c.set(Calendar.MILLISECOND, 0)
        return c.timeInMillis
    }

    /** 当天结束时间戳 (次日 0 点) */
    fun dayEnd(time: Long = System.currentTimeMillis()): Long {
        return dayStart(time) + 24L * 60 * 60 * 1000
    }

    /** 前一天 0 点时间戳 */
    fun yesterdayStart(time: Long = System.currentTimeMillis()): Long {
        return dayStart(time) - 24L * 60 * 60 * 1000
    }
}
