package com.liaojinxuan.tilivili.data.network

import com.liaojinxuan.tilivili.data.model.VideoRecommendResponse
import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url

@Serializable
data class NavResponse(val code: Int, val message: String, val data: NavData?)
@Serializable
data class NavData(val wbi_img: WbiImg?)
@Serializable
data class WbiImg(val img_url: String, val sub_url: String)

@Serializable
data class QrGenerateResponse(val code: Int, val message: String, val data: QrGenerateData?)
@Serializable
data class QrGenerateData(val url: String, val qrcode_key: String)
@Serializable
data class QrPollResponse(val code: Int, val message: String, val data: QrPollData?)
@Serializable
data class QrPollData(val url: String, val refresh_token: String, val timestamp: Long, val code: Int, val message: String)

interface BiliApi {
    @GET("x/web-interface/nav")
    suspend fun getNav(): NavResponse

    // 改成动态 URL，传入带签名的完整链接
    @GET
    suspend fun getSignedPopularVideos(@Url url: String): VideoRecommendResponse

    @GET("https://passport.bilibili.com/x/passport-login/web/qrcode/generate")
    suspend fun getQrCode(): QrGenerateResponse

    @GET("https://passport.bilibili.com/x/passport-login/web/qrcode/poll")
    suspend fun pollQrCode(@Query("qrcode_key") key: String): retrofit2.Response<QrPollResponse>
}