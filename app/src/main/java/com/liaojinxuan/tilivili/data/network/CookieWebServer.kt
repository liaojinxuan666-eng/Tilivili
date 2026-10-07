package com.liaojinxuan.tilivili.data.network

import android.content.Context
import com.liaojinxuan.tilivili.data.local.CookieManager
import fi.iki.elonen.NanoHTTPD
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CookieWebServer(context: Context, port: Int) : NanoHTTPD(port) {
    private val appContext = context.applicationContext
    private val scope = CoroutineScope(Dispatchers.IO)

    override fun serve(session: IHTTPSession): Response {
        if (session.method == Method.POST) {
            val cookie = session.parameters["cookie"]?.firstOrNull() ?: ""
            if (cookie.isNotEmpty()) {
                scope.launch {
                    CookieManager.saveCookie(appContext, cookie)
                }
                return newFixedLengthResponse("Cookie 已保存！请在电视上返回首页刷新。")
            } else {
                return newFixedLengthResponse("Cookie 不能为空！")
            }
        }
        val html = """
            <html>
            <head><meta name="viewport" content="width=device-width, initial-scale=1.0"></head>
            <body style="padding:20px;font-family:sans-serif;background:#1a1a1a;color:#fff;">
            <h2>Tilivili TV 设置</h2>
            <p>请粘贴 B站网页端 Cookie：</p>
            <form method="POST" action="/">
            <textarea name="cookie" rows="6" style="width:100%;background:#2a2a2a;color:#fff;border:1px solid #444;border-radius:6px;padding:8px;font-size:14px;"></textarea><br><br>
            <input type="submit" value="提交" style="padding:12px 24px;background:#FB7299;color:#fff;border:none;border-radius:6px;font-size:16px;">
            </form>
            </body>
            </html>
        """.trimIndent()
        return newFixedLengthResponse(html)
    }
}