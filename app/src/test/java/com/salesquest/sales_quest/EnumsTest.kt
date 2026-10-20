package com.salesquest.sales_quest

import com.salesquest.sales_quest.data.CoreMetric
import org.junit.Assert.assertEquals
import org.junit.Test

/** 对应 legacy/test/widget_test.dart: 枚举语义 */
class EnumsTest {

    @Test
    fun coreMetric_labels() {
        assertEquals("见人", CoreMetric.MEET.label)
        assertEquals("查询", CoreMetric.QUERY.label)
        assertEquals("成交", CoreMetric.DEAL.label)
    }
}
