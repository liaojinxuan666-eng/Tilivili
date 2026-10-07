package com.liaojinxuan.tilivili

import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.asImageBitmap
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
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.MultiFormatWriter
import com.liaojinxuan.tilivili.data.model.VideoItem
import com.liaojinxuan.tilivili.data.network.CookieWebServer
import com.liaojinxuan.tilivili.data.repository.QrRepository
import com.liaojinxuan.tilivili.data.repository.VideoRepository
import fi.iki.elonen.NanoHTTPD
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
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
        modifier = Modifier.fillMaxSize().background(Color(0xFF0F0F0F))
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
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

            Box(
                modifier = Modifier.weight(1f).fillMaxHeight().padding(24.dp),
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
        val response = withContext(Dispatchers.IO) {
            VideoRepository.getHomeVideos(context)
        }
        // 处理返回数据
        if (response.code != 0) {
            errorMsg = "接口报错: code=${response.code}\nmsg=${response.message}"
        } else {
            videos = response.data?.item ?: emptyList()
            if (videos.isEmpty()) errorMsg = "接口返回数据为空"
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
    Column(modifier = Modifier.width(240.dp).padding(8.dp)) {
        AsyncImage(
            model = video.pic.replace("http://", "https://"),
            contentDescription = video.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth().height(135.dp)
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
        Text(text = video.owner.name, color = Color.Gray, fontSize = 12.sp)
    }
}

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    var ipAddress by remember { mutableStateOf("获取中...") }
    var serverMsg by remember { mutableStateOf("等待连接...") }
    val server = remember { CookieWebServer(context.applicationContext, 8080) }

    var showQrCode by remember { mutableStateOf(false) }
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var qrStatusMsg by remember { mutableStateOf("请使用 B站 App 扫码") }
    var qrcodeKey by remember { mutableStateOf("") }
    var isLoggedIn by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            ipAddress = getLocalIpAddress()
            try {
                server.start(NanoHTTPD.SOCKET_READ_TIMEOUT, false)
                serverMsg = "服务器已启动"
            } catch (e: Exception) {
                serverMsg = "服务器启动失败: ${e.message}"
            }
        }
    }
    DisposableEffect(Unit) {
        onDispose { try { server.stop() } catch (e: Exception) { e.printStackTrace() } }
    }

    LaunchedEffect(showQrCode) {
        if (showQrCode) {
            val url = withContext(Dispatchers.IO) { QrRepository.getQrCodeUrl(context) }
            if (url != null) {
                qrBitmap = generateQrCodeBitmap(url, 400)
                qrcodeKey = url.substringAfterLast("qrcode_key=").substringBefore("&")

                while (true) {
                    delay(2000)
                    val pollData = withContext(Dispatchers.IO) { QrRepository.pollStatus(context, qrcodeKey) }
                    if (pollData != null) {
                        when (pollData.code) {
                            0 -> {
                                qrStatusMsg = "登录成功！"
                                isLoggedIn = true
                                delay(1500)
                                showQrCode = false
                                break
                            }
                            86090 -> qrStatusMsg = "已扫码，请在手机上确认"
                            86038 -> {
                                qrStatusMsg = "二维码已过期，请刷新"
                                break
                            }
                        }
                    }
                }
            } else {
                qrStatusMsg = "获取二维码失败"
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!showQrCode) {
            Text("请选择登录方式：", color = Color.White, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { showQrCode = true; isLoggedIn = false },
                colors = ButtonDefaults.colors(containerColor = Color(0xFFFB7299)),
                modifier = Modifier.padding(8.dp)
            ) {
                Text("B站扫码登录", fontSize = 20.sp)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("或使用手机浏览器访问：", color = Color.Gray, fontSize = 16.sp)
            Text("http://$ipAddress:8080", color = Color(0xFFFB7299), fontSize = 24.sp, fontWeight = FontWeight.Bold)
        } else {
            Text(qrStatusMsg, color = Color.White, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(16.dp))
            qrBitmap?.let {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = "QR Code",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(300.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { showQrCode = false },
                colors = ButtonDefaults.colors(containerColor = Color(0xFF2A2A2A))
            ) {
                Text("取消")
            }
        }
    }
}

fun generateQrCodeBitmap(content: String, size: Int): Bitmap {
    val hints = mapOf(
        EncodeHintType.CHARACTER_SET to "UTF-8",
        EncodeHintType.MARGIN to 1
    )
    val bitMatrix = MultiFormatWriter().encode(content, BarcodeFormat.QR_CODE, size, size, hints)
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    for (x in 0 until size) {
        for (y in 0 until size) {
            bitmap.setPixel(x, y, if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE)
        }
    }
    return bitmap
}

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
    } catch (e: Exception) { e.printStackTrace() }
    return "127.0.0.1"
}