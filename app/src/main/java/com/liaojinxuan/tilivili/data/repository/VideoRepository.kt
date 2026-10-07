package com.liaojinxuan.tilivili.data.repository

import android.content.Context
import com.liaojinxuan.tilivili.MainActivity
import com.liaojinxuan.tilivili.data.model.VideoItem
import com.liaojinxuan.tilivili.data.network.RetrofitClient

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

    suspend fun getHomeVideos(context: Context): List<VideoItem> {
        return try {
            ensureWbiKeys(context)
            
            // 排行榜接口不需要签名也能返回部分数据，但我们先把通道打通
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