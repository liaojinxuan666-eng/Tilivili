package com.liaojinxuan.tilivili.data.repository

import android.content.Context
import com.liaojinxuan.tilivili.data.model.VideoItem
import com.liaojinxuan.tilivili.data.network.RetrofitClient

object VideoRepository {
    // 增加 Context 参数，用于读取 Cookie
    suspend fun getHomeVideos(context: Context): List<VideoItem> {
        return try {
            val response = RetrofitClient.getApi(context).getRecommendVideos()
            if (response.code == 0) {
                response.data?.item ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}