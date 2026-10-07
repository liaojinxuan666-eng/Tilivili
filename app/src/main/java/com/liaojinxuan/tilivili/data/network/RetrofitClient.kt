package com.liaojinxuan.tilivili.data.network

import android.content.Context
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.liaojinxuan.tilivili.data.local.CookieManager
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL = "https://app.bilibili.com/"
    private val json = Json { ignoreUnknownKeys = true }

    fun getApi(context: Context): BiliApi {
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .addInterceptor(WbiInterceptor(context))
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            })
            .build()

        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(BiliApi::class.java)
    }

    private class WbiInterceptor(val context: Context) : Interceptor {
        override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
            val original = chain.request()
            val cookie = runBlocking { CookieManager.getCookie(context) }

            val requestBuilder = original.newBuilder()
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                // 补充 Passport 接口必需的 Referer 和 Origin
                .header("Referer", "https://www.bilibili.com/")
                .header("Origin", "https://www.bilibili.com")

            if (cookie.isNotEmpty()) {
                requestBuilder.header("Cookie", cookie)
            }

            return chain.proceed(requestBuilder.build())
        }
    }
}