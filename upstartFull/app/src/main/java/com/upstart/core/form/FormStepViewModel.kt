package com.upstart.core.form

import kotlinx.coroutines.flow.StateFlow

/**
 * Interface that all form step ViewModels must implement.
 * Provides a uniform approach to validation and submission.
 */
interface FormStepViewModel {
    /**
     * The current submission state for this step.
     */
    val submissionState: StateFlow<SubmissionState>

    /**
     * Validates all fields in this form step.
     * Updates the validation state and returns whether all fields are valid.
     *
     * @return true if all fields are valid, false otherwise
     */
    fun validateAll(): Boolean

    /**
     * Submits this step's data to the backend.
     * Updates submissionState throughout the operation.
     */
    suspend fun submit()

    /**
     * Whether all fields in this step are currently valid.
     */
    val isValid: Boolean
}
