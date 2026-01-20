package com.upstart.core.validation

// Field validation result
data class FieldValidation(
    val isValid: Boolean = true,
    val errorMessage: String? = null
) {
    companion object {
        val Valid = FieldValidation(isValid = true, errorMessage = null)
        fun Invalid(message: String) = FieldValidation(isValid = false, errorMessage = message)
    }
}

// Common validators
object Validators {
    fun validateRequired(value: String, fieldName: String): FieldValidation {
        return if (value.isBlank()) {
            FieldValidation.Invalid("$fieldName is required")
        } else {
            FieldValidation.Valid
        }
    }

    fun validateMinLength(value: String, minLength: Int, fieldName: String): FieldValidation {
        return when {
            value.isBlank() -> FieldValidation.Invalid("$fieldName is required")
            value.length < minLength -> FieldValidation.Invalid("$fieldName must be at least $minLength characters")
            else -> FieldValidation.Valid
        }
    }

    fun validateEmail(email: String): FieldValidation {
        val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
        return when {
            email.isBlank() -> FieldValidation.Invalid("Email is required")
            !emailRegex.matches(email) -> FieldValidation.Invalid("Please enter a valid email")
            else -> FieldValidation.Valid
        }
    }

    fun validateRange(value: Float, min: Float, max: Float, fieldName: String): FieldValidation {
        return when {
            value < min -> FieldValidation.Invalid("Minimum $fieldName is ${min.toInt()}")
            value > max -> FieldValidation.Invalid("Maximum $fieldName is ${max.toInt()}")
            else -> FieldValidation.Valid
        }
    }
}
