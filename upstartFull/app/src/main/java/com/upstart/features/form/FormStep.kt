package com.upstart.features.form

// Represents each step in the multi-step form
enum class FormStep(val displayName: String) {
    LOAN_DETAILS("Loan Details"),
    PERSONAL_INFO("Personal Information"),
    RESULT("Result");

    companion object {
        fun fromIndex(index: Int): FormStep? = entries.getOrNull(index)
    }
}
