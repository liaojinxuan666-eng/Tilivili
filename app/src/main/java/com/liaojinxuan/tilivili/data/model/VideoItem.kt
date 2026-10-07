package com.liaojinxuan.tilivili.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VideoItem(
    val aid: Long,
    val bvid: String = "",
    val title: String,
    val pic: String,
    val duration: Int,
    @SerialName("owner")
    val owner: Owner
)

@Serializable
data class Owner(
    val mid: Long,
    val name: String,
    val face: String
)

@Serializable
data class VideoRecommendResponse(
    val code: Int,
    val message: String,
    val data: RecommendData?
)

@Serializable
data class RecommendData(
    val item: List<VideoItem> = emptyList()
)