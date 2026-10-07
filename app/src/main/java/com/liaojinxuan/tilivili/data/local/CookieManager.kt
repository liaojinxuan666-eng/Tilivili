package com.liaojinxuan.tilivili.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

val Context.dataStore by preferencesDataStore(name = "tilivili_settings")

object CookieManager {
    private val COOKIE_KEY = stringPreferencesKey("bili_cookie")

    suspend fun saveCookie(context: Context, cookie: String) {
        try {
            context.dataStore.edit { it[COOKIE_KEY] = cookie }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun getCookie(context: Context): String {
        return try {
            val prefs = context.dataStore.data.first()
            prefs[COOKIE_KEY] ?: ""
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }
}