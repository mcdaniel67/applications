package com.upstart.core.validation

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ValidatorsTest {

    @Test
    fun `validateRequired returns Valid for non-blank value`() {
        val result = Validators.validateRequired("test", "Field")
        assertTrue(result.isValid)
        assertEquals(null, result.errorMessage)
    }

    @Test
    fun `validateRequired returns Invalid for blank value`() {
        val result = Validators.validateRequired("", "Field")
        assertFalse(result.isValid)
        assertEquals("Field is required", result.errorMessage)
    }

    @Test
    fun `validateRequired returns Invalid for whitespace-only value`() {
        val result = Validators.validateRequired("   ", "Field")
        assertFalse(result.isValid)
        assertEquals("Field is required", result.errorMessage)
    }

    @Test
    fun `validateMinLength returns Valid when value meets minimum length`() {
        val result = Validators.validateMinLength("ab", 2, "Name")
        assertTrue(result.isValid)
    }

    @Test
    fun `validateMinLength returns Valid when value exceeds minimum length`() {
        val result = Validators.validateMinLength("abcdef", 2, "Name")
        assertTrue(result.isValid)
    }

    @Test
    fun `validateMinLength returns Invalid when value is too short`() {
        val result = Validators.validateMinLength("a", 2, "Name")
        assertFalse(result.isValid)
        assertEquals("Name must be at least 2 characters", result.errorMessage)
    }

    @Test
    fun `validateMinLength returns Invalid when value is blank`() {
        val result = Validators.validateMinLength("", 2, "Name")
        assertFalse(result.isValid)
        assertEquals("Name is required", result.errorMessage)
    }

    @Test
    fun `validateEmail returns Valid for valid email`() {
        val validEmails = listOf(
            "test@example.com",
            "user.name@example.co.uk",
            "user+tag@example.com",
            "user_name@example.com"
        )

        validEmails.forEach { email ->
            val result = Validators.validateEmail(email)
            assertTrue(result.isValid, "Expected $email to be valid")
        }
    }

    @Test
    fun `validateEmail returns Invalid for invalid email`() {
        val invalidEmails = listOf(
            "invalid",
            "@example.com",
            "user@",
            "user@.com",
            "user @example.com",
            "user@example"
        )

        invalidEmails.forEach { email ->
            val result = Validators.validateEmail(email)
            assertFalse(result.isValid, "Expected $email to be invalid")
        }
    }

    @Test
    fun `validateEmail returns Invalid for blank email`() {
        val result = Validators.validateEmail("")
        assertFalse(result.isValid)
        assertEquals("Email is required", result.errorMessage)
    }

    @Test
    fun `validateRange returns Valid when value is within range`() {
        val result = Validators.validateRange(5000f, 1000f, 50000f, "loan amount")
        assertTrue(result.isValid)
    }

    @Test
    fun `validateRange returns Valid when value equals minimum`() {
        val result = Validators.validateRange(1000f, 1000f, 50000f, "loan amount")
        assertTrue(result.isValid)
    }

    @Test
    fun `validateRange returns Valid when value equals maximum`() {
        val result = Validators.validateRange(50000f, 1000f, 50000f, "loan amount")
        assertTrue(result.isValid)
    }

    @Test
    fun `validateRange returns Invalid when value is below minimum`() {
        val result = Validators.validateRange(500f, 1000f, 50000f, "loan amount")
        assertFalse(result.isValid)
        assertEquals("Minimum loan amount is 1000", result.errorMessage)
    }

    @Test
    fun `validateRange returns Invalid when value exceeds maximum`() {
        val result = Validators.validateRange(60000f, 1000f, 50000f, "loan amount")
        assertFalse(result.isValid)
        assertEquals("Maximum loan amount is 50000", result.errorMessage)
    }
}
