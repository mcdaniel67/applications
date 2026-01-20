package com.upstart.core.form

/**
 * Represents the state of an async submission operation.
 */
sealed class SubmissionState {
    /** Not yet submitted */
    data object Idle : SubmissionState()

    /** Currently submitting */
    data object Loading : SubmissionState()

    /** Successfully submitted */
    data class Success(val message: String? = null) : SubmissionState()

    /** Submission failed */
    data class Error(val message: String) : SubmissionState()
}
