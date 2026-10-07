package com.liaojinxuan.tilivili.data.repository

import android.content.Context
import com.liaojinxuan.tilivili.MainActivity
import com.liaojinxuan.tilivili.data.model.VideoItem
import com.liaojinxuan.tilivili.data.network.RetrofitClient
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

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

    suspend fun getHomeVideos(context: Context): List<VideoItem> {
        return try {
            ensureWbiKeys(context)

            // 热门接口不需要签名，但为了以后扩展（如搜索、评论），我们先在这里练一下签名拼接
            // 这里直接请求热门接口
            val response = RetrofitClient.getApi(context).getPopularVideos(ps = 20, pn = 1)
            
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