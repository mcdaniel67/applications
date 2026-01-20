package com.upstart.core.utils

import com.upstart.core.config.FormConfig
import kotlin.math.roundToInt

object CurrencyFormatter {
    /**
     * Formats a float amount as a currency string with commas for thousands separators.
     * Example: 25000f -> "$25,000"
     */
    fun formatCurrency(amount: Float): String {
        val rounded = amount.roundToInt()
        val formatted = rounded.toString().replace(FormConfig.CURRENCY_FORMAT_REGEX, "$1,")
        return "$$formatted"
    }
}
