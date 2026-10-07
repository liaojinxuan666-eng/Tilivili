package com.liaojinxuan.tilivili.data.network

import com.liaojinxuan.tilivili.data.model.VideoRecommendResponse
import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Query

@Serializable
data class NavResponse(
    val code: Int,
    val message: String,
    val data: NavData?
)

@Serializable
data class NavData(
    val wbi_img: WbiImg?
)

@Serializable
data class WbiImg(
    val img_url: String,
    val sub_url: String
)

interface BiliApi {
    // 获取 Wbi 签名所需的 img_key 和 sub_key
    @GET("x/web-interface/nav")
    suspend fun getNav(): NavResponse

    // 获取排行榜视频（无需登录，用于测试网络层）
    @GET("x/web-interface/ranking/v2")
    suspend fun getRankingVideos(
        @Query("rid") rid: Int = 0,
        @Query("type") type: String = "all"
    ): VideoRecommendResponse
}