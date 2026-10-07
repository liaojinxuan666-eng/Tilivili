package com.liaojinxuan.tilivili.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first

val Context.dataStore by preferencesDataStore(name = "tilivili_settings")

object CookieManager {
    private val COOKIE_KEY = stringPreferencesKey("bili_cookie")

    // 保存 Cookie
    suspend fun saveCookie(context: Context, cookie: String) {
        context.dataStore.edit { it[COOKIE_KEY] = cookie }
    }

    // 读取 Cookie
    suspend fun getCookie(context: Context): String {
        val prefs = context.dataStore.data.first()
        return prefs[COOKIE_KEY] ?: ""
    }
}