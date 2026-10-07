package com.liaojinxuan.tilivili.data.repository

import android.content.Context
import com.liaojinxuan.tilivili.data.model.VideoRecommendResponse
import com.liaojinxuan.tilivili.data.network.RetrofitClient

object VideoRepository {
    private var imgKey: String = ""
    private var subKey: String = ""

    // 获取 Wbi 签名所需的 Key
    private suspend fun ensureWbiKeys(context: Context) {
        if (imgKey.isNotEmpty() && subKey.isNotEmpty()) return
        try {
            val nav = RetrofitClient.getApi(context).getNav()
            nav.data?.wbi_img?.let { wbi ->
                imgKey = wbi.img_url.substringAfterLast("/").substringBefore(".")
                subKey = wbi.sub_url.substringAfterLast("/").substringBefore(".")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // 注意：这里返回的是完整的响应对象，而不是 List<VideoItem>
    suspend fun getHomeVideos(context: Context): VideoRecommendResponse {
        return try {
            ensureWbiKeys(context)
            // 请求综合热门视频（此接口对无登录状态非常宽容）
            RetrofitClient.getApi(context).getPopularVideos(ps = 20, pn = 1)
        } catch (e: Exception) {
            e.printStackTrace()
            // 发生异常时返回一个错误码包装的响应
            VideoRecommendResponse(code = -1, message = e.message ?: "未知错误", data = null)
        }
    }
}