package com.upstart.features.personalInfo

import com.upstart.core.validation.FieldValidation

// UI state for personal info step
data class PersonalInfoState(
    val firstName: String = "",
    val lastName: String = "",
    val email: String = "",
    val firstNameValidation: FieldValidation = FieldValidation.Valid,
    val lastNameValidation: FieldValidation = FieldValidation.Valid,
    val emailValidation: FieldValidation = FieldValidation.Valid
) {
    val isValid: Boolean
        get() = firstNameValidation.isValid && lastNameValidation.isValid && emailValidation.isValid
}

// User intents for personal info
sealed class PersonalInfoIntent {
    data class UpdateFirstName(val firstName: String) : PersonalInfoIntent()
    data class UpdateLastName(val lastName: String) : PersonalInfoIntent()
    data class UpdateEmail(val email: String) : PersonalInfoIntent()
}
