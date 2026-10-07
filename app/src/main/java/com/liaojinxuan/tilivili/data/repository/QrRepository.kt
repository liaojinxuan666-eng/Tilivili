package com.liaojinxuan.tilivili.data.repository

import android.content.Context
import com.liaojinxuan.tilivili.data.local.CookieManager
import com.liaojinxuan.tilivili.data.network.QrPollData
import com.liaojinxuan.tilivili.data.network.RetrofitClient

object QrRepository {
    private suspend fun extractAndSaveCookies(context: Context, response: retrofit2.Response<*>) {
        val setCookieHeaders = response.headers().values("Set-Cookie")
        val cookieBuilder = StringBuilder()
        setCookieHeaders.forEach { header ->
            if (header.contains("SESSDATA") || header.contains("bili_jct") || header.contains("DedeUserID")) {
                cookieBuilder.append(header.substringBefore(";")).append("; ")
            }
        }
        val finalCookie = cookieBuilder.toString().trim()
        if (finalCookie.isNotEmpty()) {
            CookieManager.saveCookie(context, finalCookie)
        }
    }

    suspend fun getQrCodeUrl(context: Context): String? {
        return try {
            val response = RetrofitClient.getApi(context).getQrCode()
            response.data?.url
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun pollStatus(context: Context, qrcodeKey: String): QrPollData? {
        return try {
            val response = RetrofitClient.getApi(context).pollQrCode(qrcodeKey)
            if (response.isSuccessful) {
                val body = response.body()
                if (body?.data?.code == 0) {
                    extractAndSaveCookies(context, response)
                }
                body?.data
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}