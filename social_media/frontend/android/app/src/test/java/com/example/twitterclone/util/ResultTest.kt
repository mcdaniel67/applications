package com.example.twitterclone.util

import org.junit.Assert.*
import org.junit.Test

class ResultTest {

    @Test
    fun `Success holds data correctly`() {
        val data = "test data"
        val result = Result.Success(data)
        
        assertTrue(result is Result.Success)
        assertEquals(data, result.data)
    }

    @Test
    fun `Error holds message and code`() {
        val message = "Error occurred"
        val code = 404
        val result = Result.Error(message, code)
        
        assertTrue(result is Result.Error)
        assertEquals(message, result.message)
        assertEquals(code, result.code)
    }

    @Test
    fun `Error works without code`() {
        val message = "Error occurred"
        val result = Result.Error(message)
        
        assertTrue(result is Result.Error)
        assertEquals(message, result.message)
        assertNull(result.code)
    }

    @Test
    fun `Loading is singleton`() {
        val loading1 = Result.Loading
        val loading2 = Result.Loading
        
        assertSame(loading1, loading2)
    }
}
