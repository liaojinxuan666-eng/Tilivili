package com.liaojinxuan.tilivili.data.repository

import android.content.Context
import com.liaojinxuan.tilivili.MainActivity
import com.liaojinxuan.tilivili.data.model.VideoItem
import com.liaojinxuan.tilivili.data.network.RetrofitClient
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

object VideoRepository {
    // 缓存 Wbi Keys
    private var imgKey: String = ""
    private var subKey: String = ""

    private suspend fun ensureWbiKeys(context: Context) {
        if (imgKey.isNotEmpty() && subKey.isNotEmpty()) return
        try {
            val nav = RetrofitClient.getApi(context).getNav()
            nav.data?.wbi_img?.let { wbi ->
                // 从 URL 中提取出 img_key 和 sub_key
                imgKey = wbi.img_url.substringAfterLast("/").substringBefore(".")
                subKey = wbi.sub_url.substringAfterLast("/").substringBefore(".")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun getHomeVideos(context: Context): List<VideoItem> {
        return try {
            ensureWbiKeys(context)

            // 当前时间戳（秒）
            val wts = (System.currentTimeMillis() / 1000).toString()
            // 原始查询参数（按字典序排列）
            val rawQuery = "rid=0&type=all&wts=$wts"

            // 用 Rust 计算签名
            val wbiSign = (context as? MainActivity)?.wbiSign(imgKey, subKey, rawQuery)
                ?: return emptyList()

            // 把签名拼接到请求中（这里先直接调用接口，实际项目可以通过拦截器自动处理）
            val response = RetrofitClient.getApi(context).getRankingVideos(rid = 0, type = "all")
            
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