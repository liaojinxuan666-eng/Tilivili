package com.liaojinxuan.tilivili

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import androidx.tv.material3.darkColorScheme
import coil.compose.AsyncImage
import com.liaojinxuan.tilivili.data.model.VideoItem
import com.liaojinxuan.tilivili.data.repository.VideoRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.ui.platform.LocalContext

class MainActivity : ComponentActivity() {
    companion object {
        init {
            try {
                System.loadLibrary("tilivili_rust")
            } catch (e: UnsatisfiedLinkError) {
                e.printStackTrace()
            }
        }
    }

    external fun helloRust(): String
    external fun wbiSign(rawQuery: String): String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                TVApp()
            }
        }
    }
}

@Composable
fun TVApp() {
    var currentTab by remember { mutableStateOf(0) }
    val tabs = listOf("首页", "搜索", "动态", "设置")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F0F))
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            // 左侧导航栏
            Column(
                modifier = Modifier
                    .width(200.dp)
                    .fillMaxHeight()
                    .background(Color(0xFF1A1A1A))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Tilivili",
                    color = Color(0xFFFB7299),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                tabs.forEachIndexed { index, title ->
                    Button(
                        onClick = { currentTab = index },
                        colors = if (currentTab == index) {
                            ButtonDefaults.colors(containerColor = Color(0xFFFB7299))
                        } else {
                            ButtonDefaults.colors(containerColor = Color(0xFF2A2A2A))
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = title, fontSize = 18.sp)
                    }
                }
            }

            // 右侧内容区
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                when (currentTab) {
                    0 -> HomeScreen()
                    1 -> Text("这里是搜索页面", color = Color.White, fontSize = 32.sp)
                    2 -> Text("这里是动态页面", color = Color.White, fontSize = 32.sp)
                    3 -> Text("这里是设置页面", color = Color.White, fontSize = 32.sp)
                }
            }
        }
    }
}

@Composable
fun HomeScreen() {
    val context = LocalContext.current.applicationContext
    var videos by remember { mutableStateOf<List<VideoItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        isLoading = true
        videos = withContext(Dispatchers.IO) {
            VideoRepository.getHomeVideos(context) // 👈 传入 context
        }
        isLoading = false
    }

    if (isLoading) {
        Text("正在加载首页数据...", color = Color.White, fontSize = 24.sp)
    } else if (videos.isEmpty()) {
        Text("获取数据失败（可能需要 Wbi 签名或 Cookie）", color = Color.Gray, fontSize = 18.sp)
    } else {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            items(videos) { video ->
                VideoCard(video)
            }
        }
    }
}

@Composable
fun VideoCard(video: VideoItem) {
    Column(
        modifier = Modifier
            .width(240.dp)
            .padding(8.dp)
    ) {
        // 封面图
        AsyncImage(
            model = video.pic.replace("http://", "https://"),
            contentDescription = video.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(135.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF2A2A2A))
        )
        Spacer(modifier = Modifier.height(8.dp))
        // 标题
        Text(
            text = video.title,
            color = Color.White,
            fontSize = 14.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(4.dp))
        // UP主
        Text(
            text = video.owner.name,
            color = Color.Gray,
            fontSize = 12.sp
        )
    }
}