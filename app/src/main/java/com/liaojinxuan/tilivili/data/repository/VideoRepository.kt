package com.liaojinxuan.tilivili.data.repository

import android.content.Context
import com.liaojinxuan.tilivili.MainActivity
import com.liaojinxuan.tilivili.data.model.VideoRecommendResponse
import com.liaojinxuan.tilivili.data.network.RetrofitClient
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object VideoRepository {
    private var imgKey: String = ""
    private var subKey: String = ""

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

    suspend fun getHomeVideos(context: Context): VideoRecommendResponse {
        return try {
            ensureWbiKeys(context)
            
            // 1. 准备参数（B站要求所有参数按字典序排序）
            val wts = (System.currentTimeMillis() / 1000).toString()
            val params = "pn=1&ps=20&wts=$wts"
            
            // 2. 调用 Rust 计算签名（Rust 会在内部对参数做 URL 编码和拼接）
            val wbiSign = (context as? MainActivity)?.wbiSign(imgKey, subKey, params)
            
            if (wbiSign.isNullOrEmpty()) {
                return VideoRecommendResponse(code = -1, message = "Rust 签名计算失败", data = null)
            }

            // 3. 拼接最终请求 URL（需要 URL 编码）
            val encodedParams = params.split("&").joinToString("&") { 
                val kv = it.split("=")
                "${kv[0]}=${URLEncoder.encode(kv[1], StandardCharsets.UTF_8.toString())}"
            }
            val url = "x/web-interface/popular?$encodedParams&w_rid=$wbiSign"

            RetrofitClient.getApi(context).getSignedPopularVideos(url)
        } catch (e: Exception) {
            e.printStackTrace()
            VideoRecommendResponse(code = -1, message = e.message ?: "未知错误", data = null)
        }
    }
}