package com.upstart.core.utils

import org.junit.Test
import kotlin.test.assertEquals

class CurrencyFormatterTest {

    @Test
    fun `formatCurrency adds thousand separators`() {
        val formatted = CurrencyFormatter.formatCurrency(12345f)
        assertEquals("$12,345", formatted)
    }

    @Test
    fun `formatCurrency handles values without separators`() {
        val formatted = CurrencyFormatter.formatCurrency(750f)
        assertEquals("$750", formatted)
    }

    @Test
    fun `formatCurrency rounds to nearest whole dollar`() {
        val formatted = CurrencyFormatter.formatCurrency(1999.6f)
        assertEquals("$2,000", formatted)
    }
}
