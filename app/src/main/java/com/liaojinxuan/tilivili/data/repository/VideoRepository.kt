package com.liaojinxuan.tilivili.data.repository

import com.liaojinxuan.tilivili.data.model.VideoItem
import com.liaojinxuan.tilivili.data.network.RetrofitClient

object VideoRepository {
    suspend fun getHomeVideos(): List<VideoItem> {
        return try {
            val response = RetrofitClient.api.getRecommendVideos()
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