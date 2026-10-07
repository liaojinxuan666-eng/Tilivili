package com.liaojinxuan.tilivili

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import androidx.tv.material3.darkColorScheme

class MainActivity : ComponentActivity() {
    companion object {
        init {
            System.loadLibrary("tilivili_rust")
        }
    }

    external fun helloRust(): String
    external fun wbiSign(rawQuery: String): String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val signResult = try {
            wbiSign("foo=114&bar=514")
        } catch (e: Exception) {
            "Rust 签名失败: ${e.message}"
        }

        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                TVApp(signResult)
            }
        }
    }
}

@Composable
fun TVApp(rustSign: String) {
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
                // 这里我们统一展示 Rust 返回的签名，验证 JNI 链路
                Text(
                    text = "Rust Wbi 签名结果:\n\n$rustSign",
                    color = Color.White,
                    fontSize = 24.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}