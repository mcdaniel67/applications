package com.upstart.core.models

// Loan type enumeration
enum class LoanType(val displayName: String) {
    AUTO("Auto Loan"),
    MORTGAGE("Mortgage"),
    PERSONAL("Personal Loan"),
    STUDENT("Student Loan"),
    BUSINESS("Business Loan"),
    HOME_IMPROVEMENT("Home Improvement"),
    DEBT_CONSOLIDATION("Debt Consolidation")
}
