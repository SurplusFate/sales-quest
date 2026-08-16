package com.salesquest.sales_quest

import com.salesquest.sales_quest.data.CoreMetric
import com.salesquest.sales_quest.data.CustomerStage
import com.salesquest.sales_quest.data.Operator
import org.junit.Assert.assertEquals
import org.junit.Test

/** 对应 legacy/test/widget_test.dart: 枚举语义 */
class EnumsTest {

    @Test
    fun customerStage_fromCode() {
        assertEquals(CustomerStage.NEW, CustomerStage.fromCode("NEW"))
        assertEquals(CustomerStage.WON, CustomerStage.fromCode("WON"))
        assertEquals(CustomerStage.NEW, CustomerStage.fromCode("INVALID"))
    }

    @Test
    fun coreMetric_labels() {
        assertEquals("见人", CoreMetric.MEET.label)
        assertEquals("查询", CoreMetric.QUERY.label)
        assertEquals("成交", CoreMetric.DEAL.label)
    }

    @Test
    fun operator_fromCode() {
        assertEquals(Operator.MOBILE, Operator.fromCode("MOBILE"))
        assertEquals(Operator.UNKNOWN, Operator.fromCode("UNKNOWN"))
    }
}
