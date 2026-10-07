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
import androidx.compose.ui.platform.LocalContext
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
import com.liaojinxuan.tilivili.data.network.CookieWebServer
import com.liaojinxuan.tilivili.data.repository.VideoRepository
import fi.iki.elonen.NanoHTTPD
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.Inet4Address
import java.net.NetworkInterface

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

    // 注意：这里的签名已经更新为三个参数，与 Rust 端保持一致
    external fun helloRust(): String
    external fun wbiSign(imgKey: String, subKey: String, rawQuery: String): String

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
                    3 -> SettingsScreen()
                }
            }
        }
    }
}

@Composable
fun HomeScreen() {
    val context = LocalContext.current
    var videos by remember { mutableStateOf<List<VideoItem>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMsg by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        isLoading = true
        try {
            videos = withContext(Dispatchers.IO) {
                VideoRepository.getHomeVideos(context)
            }
            if (videos.isEmpty()) {
                errorMsg = "获取数据失败（可能需要 Cookie 或 Wbi 签名）"
            }
        } catch (e: Exception) {
            errorMsg = "请求异常: ${e.message}"
        }
        isLoading = false
    }

    if (isLoading) {
        Text("正在加载首页数据...", color = Color.White, fontSize = 24.sp)
    } else if (errorMsg.isNotEmpty()) {
        Text(errorMsg, color = Color.Gray, fontSize = 18.sp)
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
        AsyncImage(
            model = video.pic.replace("http://", "https://"), // 👈 确认这里是 model，不是 modelp
            contentDescription = video.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .height(135.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF2A2A2A))
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = video.title,
            color = Color.White,
            fontSize = 14.sp,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = video.owner.name,
            color = Color.Gray,
            fontSize = 12.sp
        )
    }
}

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    var ipAddress by remember { mutableStateOf("获取中...") }
    var serverMsg by remember { mutableStateOf("等待连接...") }
    val server = remember { CookieWebServer(context.applicationContext, 8080) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            ipAddress = getLocalIpAddress()
            try {
                server.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false)
                serverMsg = "服务器已启动，请访问上述地址"
            } catch (e: Exception) {
                serverMsg = "服务器启动失败: ${e.message}"
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            try {
                server.stop()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("请在手机浏览器中访问以下地址，输入 Cookie：", color = Color.White, fontSize = 24.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text("http://$ipAddress:8080", color = Color(0xFFFB7299), fontSize = 36.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text(serverMsg, color = Color.Gray, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(16.dp))
        Text("请确保手机和电视连接在同一 WiFi 下", color = Color.Gray, fontSize = 16.sp)
    }
}

// 获取局域网 IP 地址
fun getLocalIpAddress(): String {
    try {
        val en = NetworkInterface.getNetworkInterfaces()
        while (en.hasMoreElements()) {
            val intf = en.nextElement()
            val enumIpAddr = intf.inetAddresses
            while (enumIpAddr.hasMoreElements()) {
                val inetAddress = enumIpAddr.nextElement()
                if (!inetAddress.isLoopbackAddress && inetAddress is Inet4Address) {
                    val ip = inetAddress.hostAddress ?: ""
                    if (ip.startsWith("192.168.") || ip.startsWith("10.") || ip.startsWith("172.")) {
                        return ip
                    }
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return "127.0.0.1"
}