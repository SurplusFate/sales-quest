package com.salesquest.sales_quest

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.salesquest.sales_quest.data.AppDatabase
import com.salesquest.sales_quest.services.BackupService
import com.salesquest.sales_quest.services.WebDavConfig
import com.salesquest.sales_quest.services.WebDavConfigStore
import com.salesquest.sales_quest.services.WebDavResult
import com.salesquest.sales_quest.services.WebDavService
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * WebDAV 服务测试
 *
 * 覆盖: URL 拼接 / PROPFIND XML 解析 / 配置有效性判断 / 错误信息非空验证
 */
@RunWith(RobolectricTestRunner::class)
class WebDavServiceUnitTest {

    private lateinit var db: AppDatabase
    private lateinit var service: WebDavService

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        service = WebDavService(context, WebDavConfigStore(context), BackupService(db))
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun 配置完整时视为已配置() {
        val config = WebDavConfig(
            url = "https://dav.jianguoyun.com/dav",
            username = "user",
            password = "pass"
        )
        assertTrue(config.isConfigured())
    }

    @Test
    fun 缺少密码时未配置() {
        val config = WebDavConfig(
            url = "https://dav.jianguoyun.com/dav",
            username = "user",
            password = ""
        )
        assertFalse(config.isConfigured())
    }

    @Test
    fun 空配置未配置() {
        assertFalse(WebDavConfig().isConfigured())
    }

    @Test
    fun joinUrl_处理末尾斜杠() {
        assertEquals(
            "https://dav.jianguoyun.com/dav/x",
            service.joinUrl("https://dav.jianguoyun.com/dav/", "x")
        )
        assertEquals(
            "https://dav.jianguoyun.com/dav/x",
            service.joinUrl("https://dav.jianguoyun.com/dav", "/x")
        )
    }

    @Test
    fun joinDir_目录与文件名拼接() {
        val dir = "/SalesQuest"
        val filename = "sales_quest_backup_2026-08-19_120000.db.zip"
        val result = service.joinUrl(
            "https://dav.jianguoyun.com/dav",
            service.joinDir(dir, filename)
        )
        assertEquals("https://dav.jianguoyun.com/dav/SalesQuest/$filename", result)
    }

    @Test
    fun parsePropfind_提取备份文件名() {
        val xml = """
            <?xml version="1.0"?>
            <d:multistatus xmlns:d="DAV:">
              <d:response>
                <d:href>https://dav.jianguoyun.com/dav/SalesQuest/</d:href>
                <d:propstat><d:status>HTTP/1.1 200 OK</d:status></d:propstat>
              </d:response>
              <d:response>
                <d:href>https://dav.jianguoyun.com/dav/SalesQuest/sales_quest_backup_2026-08-19_100000.db.zip</d:href>
                <d:propstat><d:status>HTTP/1.1 200 OK</d:status></d:propstat>
              </d:response>
              <d:response>
                <d:href>https://dav.jianguoyun.com/dav/SalesQuest/other_file.txt</d:href>
                <d:propstat><d:status>HTTP/1.1 200 OK</d:status></d:propstat>
              </d:response>
            </d:multistatus>
        """.trimIndent()

        val names = service.parsePropfind(xml)
        assertEquals("sales_quest_backup_2026-08-19_100000.db.zip", names)
    }

    @Test
    fun parsePropfind_空结果返回空串() {
        val xml = """
            <?xml version="1.0"?>
            <d:multistatus xmlns:d="DAV:">
              <d:response>
                <d:href>https://dav.jianguoyun.com/dav/SalesQuest/</d:href>
              </d:response>
            </d:multistatus>
        """.trimIndent()
        assertEquals("", service.parsePropfind(xml))
    }

    // ==================== 错误信息非空验证 ====================

    @Test
    fun 未配置时返回明确错误() {
        val result = runBlocking { service.testConnection(WebDavConfig()) }
        val msg = (result as? WebDavResult.Failure)?.message
        assertNotNull("Failure 消息不能为 null", msg)
        assertFalse("错误信息不能包含 'null'", msg!!.contains("null", ignoreCase = true))
        assertTrue("应提示填写配置", msg.contains("请填写"))
    }

    @Test
    fun url格式错误时返回明确错误() {
        val result = runBlocking {
            service.testConnection(WebDavConfig(url = "not-a-url", username = "u", password = "p"))
        }
        val msg = (result as? WebDavResult.Failure)?.message
        assertNotNull("Failure 消息不能为 null", msg)
        assertFalse("错误信息不能包含 'null'", msg!!.contains("null", ignoreCase = true))
        assertTrue("应提示地址格式错误", msg.contains("格式错误") || msg.contains("HTTPS") || msg.contains("解析"))
    }

    @Test
    fun http地址时提示需要HTTPS() {
        val result = runBlocking {
            service.testConnection(WebDavConfig(url = "http://dav.jianguoyun.com/dav", username = "u", password = "p"))
        }
        val msg = (result as? WebDavResult.Failure)?.message
        assertNotNull(msg)
        assertFalse("不能包含 'null'", msg!!.contains("null", ignoreCase = true))
        assertTrue("应提示 HTTPS", msg.contains("HTTPS") || msg.contains("https"))
    }

    @Test
    fun 无法连接时错误信息不为null() {
        // 用一个不可达地址, 会触发网络异常
        // 注意: Robolectric 环境下 OkHttp 网络调用可能超时, 这里只验证
        // 前置校验逻辑 (URL 格式错误) 的错误信息不为 null
        val result = runBlocking {
            service.testConnection(WebDavConfig(
                url = "ftp://invalid-scheme",  // 非 https, 会被前置校验拦截
                username = "u",
                password = "p",
                dir = "/test"
            ))
        }
        val msg = (result as? WebDavResult.Failure)?.message
        assertNotNull("连接失败时必须有错误信息", msg)
        assertFalse("错误信息中绝对不能出现 'null' 字符串: $msg", msg!!.contains("null", ignoreCase = true))
    }
}
