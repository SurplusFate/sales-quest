package com.salesquest.sales_quest

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.salesquest.sales_quest.data.AppDatabase
import com.salesquest.sales_quest.services.BackupService
import com.salesquest.sales_quest.services.WebDavConfig
import com.salesquest.sales_quest.services.WebDavConfigStore
import com.salesquest.sales_quest.services.WebDavService
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * WebDAV 服务纯逻辑测试
 *
 * 覆盖: URL 拼接 / PROPFIND XML 解析 / 配置有效性判断
 * (网络请求部分不做单元测试)
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
}
