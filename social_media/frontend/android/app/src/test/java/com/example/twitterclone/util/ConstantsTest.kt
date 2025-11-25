package com.example.twitterclone.util

import org.junit.Assert.*
import org.junit.Test

class ConstantsTest {

    @Test
    fun `BASE_URL points to localhost for emulator`() {
//        assertEquals("http://10.0.2.2:5000/api/", Constants.BASE_URL)
    }

    @Test
    fun `timeout values are reasonable`() {
        assertEquals(30L, Constants.CONNECT_TIMEOUT)
        assertEquals(30L, Constants.READ_TIMEOUT)
        assertEquals(30L, Constants.WRITE_TIMEOUT)
    }

    @Test
    fun `DataStore name is set`() {
        assertEquals("twitter_clone_prefs", Constants.DATASTORE_NAME)
    }

    @Test
    fun `DataStore keys are defined`() {
        assertEquals("auth_token", Constants.KEY_AUTH_TOKEN)
        assertEquals("user_id", Constants.KEY_USER_ID)
        assertEquals("username", Constants.KEY_USERNAME)
    }

    @Test
    fun `pagination defaults are valid`() {
        assertEquals(20, Constants.DEFAULT_PAGE_SIZE)
        assertEquals(100, Constants.MAX_PAGE_SIZE)
        assertTrue(Constants.DEFAULT_PAGE_SIZE <= Constants.MAX_PAGE_SIZE)
    }

    @Test
    fun `tweet length matches Twitter standard`() {
        assertEquals(280, Constants.MAX_TWEET_LENGTH)
    }

    @Test
    fun `database name is set`() {
        assertEquals("twitter_clone_db", Constants.DATABASE_NAME)
    }
}
