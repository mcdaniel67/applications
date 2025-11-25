package com.example.twitterclone.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.twitterclone.util.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesManager @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private val authTokenKey = stringPreferencesKey(Constants.KEY_AUTH_TOKEN)
    private val userIdKey = intPreferencesKey(Constants.KEY_USER_ID)
    private val usernameKey = stringPreferencesKey(Constants.KEY_USERNAME)
    
    val authToken: Flow<String?> = dataStore.data.map { it[authTokenKey] }
    val userId: Flow<Int?> = dataStore.data.map { it[userIdKey] }
    val username: Flow<String?> = dataStore.data.map { it[usernameKey] }
    
    suspend fun saveAuthData(token: String, userId: Int, username: String) {
        dataStore.edit { prefs ->
            prefs[authTokenKey] = token
            prefs[userIdKey] = userId
            prefs[usernameKey] = username
        }
    }
    
    suspend fun clearAuthData() {
        dataStore.edit { prefs ->
            prefs.remove(authTokenKey)
            prefs.remove(userIdKey)
            prefs.remove(usernameKey)
        }
    }
}
