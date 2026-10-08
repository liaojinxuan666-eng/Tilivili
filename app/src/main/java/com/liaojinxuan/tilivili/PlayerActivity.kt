package com.liaojinxuan.tilivili

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.darkColorScheme

class PlayerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 拿到传过来的 URL
        val videoUrl = intent.getStringExtra("video_url") ?: ""
        
        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                PlayerScreen(videoUrl)
            }
        }
    }
}

@Composable
fun PlayerScreen(videoUrl: String) {
    val context = LocalContext.current
    
    // 创建 ExoPlayer
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(videoUrl))
            prepare()
            playWhenReady = true
        }
    }

    // 页面销毁时释放播放器
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer.release()
        }
    }

    // 把 ExoPlayer 的 UI 嵌入 Compose
    AndroidView(
        factory = { ctx ->
            PlayerView(ctx).apply {
                player = exoPlayer
                useController = true // 显示默认的播放控制条
            }
        },
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    )
}