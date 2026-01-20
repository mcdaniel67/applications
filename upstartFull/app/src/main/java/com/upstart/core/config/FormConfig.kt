package com.upstart.core.config

object FormConfig {
    // Loan amount configuration
    const val MIN_LOAN_AMOUNT = 1000f
    const val MAX_LOAN_AMOUNT = 50000f
    const val LOAN_AMOUNT_SLIDER_STEP_INTERVAL = 100
    val LOAN_SLIDER_STEPS = ((MAX_LOAN_AMOUNT.toInt() - MIN_LOAN_AMOUNT.toInt()) / LOAN_AMOUNT_SLIDER_STEP_INTERVAL) - 1

    // Validation rules
    const val MIN_NAME_LENGTH = 2

    // Currency formatting
    val CURRENCY_FORMAT_REGEX = Regex("(\\d)(?=(\\d{3})+$)")
}
