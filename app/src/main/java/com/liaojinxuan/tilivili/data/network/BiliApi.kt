package com.liaojinxuan.tilivili.data.network

import com.liaojinxuan.tilivili.data.model.VideoRecommendResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface BiliApi {
    // 获取首页推荐视频接口（注意：此接口可能需要 Wbi 签名，我们暂时用最简化的匿名接口测试）
    @GET("x/v2/feed/index")
    suspend fun getRecommendVideos(
        @Query("idx") idx: Int = 1,
        @Query("login_event") loginEvent: Int = 0,
        @Query("mobi_app") mobiApp: String = "android",
        @Query("platform") platform: String = "android"
    ): VideoRecommendResponse
}