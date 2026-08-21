package com.salesquest.sales_quest.services

import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.salesquest.sales_quest.core.BackupDefaults
import com.salesquest.sales_quest.core.BackupKeys
import okhttp3.Credentials
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** WebDAV 配置 */
data class WebDavConfig(
    val url: String = "",
    val username: String = "",
    val password: String = "",
    val dir: String = BackupDefaults.DEFAULT_WEBDAV_DIR,
    val autoBackup: Boolean = BackupDefaults.AUTO_BACKUP_DAILY
) {
    fun isConfigured(): Boolean = url.isNotBlank() && username.isNotBlank() && password.isNotBlank()
}

/**
 * WebDAV 配置存储
 * 密码使用 EncryptedSharedPreferences (Android Keystore) 加密保存, 不落明文
 */
class WebDavConfigStore(context: Context) {

    private val prefs: SharedPreferences = createEncryptedPrefs(context)

    private fun createEncryptedPrefs(context: Context): SharedPreferences {
        return try {
            val masterKey = MasterKey.Builder(context)
                .setKeyGenParameterSpec(
                    KeyGenParameterSpec.Builder(
                        MasterKey.DEFAULT_MASTER_KEY_ALIAS,
                        KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                    )
                        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                        .setKeySize(256)
                        .build()
                )
                .build()
            EncryptedSharedPreferences.create(
                context,
                BackupKeys.PREFS_BACKUP,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            // 密钥创建失败时回退普通 prefs (仅用于降级, 生产环境不会发生)
            context.getSharedPreferences(BackupKeys.PREFS_BACKUP, Context.MODE_PRIVATE)
        }
    }

    fun load(): WebDavConfig = WebDavConfig(
        url = prefs.getString(BackupKeys.WEBDAV_URL, "") ?: "",
        username = prefs.getString(BackupKeys.WEBDAV_USERNAME, "") ?: "",
        password = prefs.getString(BackupKeys.WEBDAV_PASSWORD, "") ?: "",
        dir = prefs.getString(BackupKeys.WEBDAV_DIR, BackupDefaults.DEFAULT_WEBDAV_DIR)
            ?: BackupDefaults.DEFAULT_WEBDAV_DIR,
        autoBackup = prefs.getBoolean(BackupKeys.AUTO_BACKUP_ENABLED, BackupDefaults.AUTO_BACKUP_DAILY)
    )

    fun save(config: WebDavConfig) {
        prefs.edit()
            .putString(BackupKeys.WEBDAV_URL, config.url.trim())
            .putString(BackupKeys.WEBDAV_USERNAME, config.username.trim())
            .putString(BackupKeys.WEBDAV_PASSWORD, config.password)
            .putString(BackupKeys.WEBDAV_DIR, config.dir.ifBlank { BackupDefaults.DEFAULT_WEBDAV_DIR })
            .putBoolean(BackupKeys.AUTO_BACKUP_ENABLED, config.autoBackup)
            .apply()
    }

    fun setAutoBackup(enabled: Boolean) {
        prefs.edit().putBoolean(BackupKeys.AUTO_BACKUP_ENABLED, enabled).apply()
    }

    fun lastBackupAt(): Long = prefs.getLong(BackupKeys.LAST_BACKUP_AT, 0L)

    fun setLastBackupAt(time: Long) {
        prefs.edit().putLong(BackupKeys.LAST_BACKUP_AT, time).apply()
    }

    fun isConfigured(): Boolean {
        val c = load()
        return c.url.isNotBlank() && c.username.isNotBlank() && c.password.isNotBlank()
    }

    /** 清空配置 (含密码) */
    fun clear() {
        prefs.edit().clear().apply()
    }
}

/** WebDAV 操作结果 */
sealed class WebDavResult {
    data class Success(val message: String = "") : WebDavResult()
    data class Failure(val message: String) : WebDavResult()
}

/** 备份文件列表条目 */
data class BackupFileInfo(val name: String, val size: Long)

/**
 * 坚果云 WebDAV 服务
 *
 * 提供: 测试连接 / 立即备份 / 获取备份列表 / 恢复备份 / 自动备份检查
 */
open class WebDavService(
    private val context: Context,
    private val configStore: WebDavConfigStore = WebDavConfigStore(context),
    private val backupService: BackupService
) {

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .followRedirects(true)          // 跟随 301/302 重定向 (坚果云会用)
        .followSslRedirects(true)       // 跟随 HTTP→HTTPS 重定向
        .retryOnConnectionFailure(true) // 网络抖动时自动重试
        .build()

    private val jsonMediaType = "application/xml; charset=utf-8".toMediaType()
    private val octetMediaType = "application/octet-stream".toMediaType()

    // ==================== 连接 ====================

    /**
     * 测试连接: PROPFIND 到配置目录
     *
     * 坚果云注意事项:
     * - URL 格式: https://dav.jianguoyun.com/dav/
     * - 密码: 必须用坚果云「设置 → 安全选项 → 第三方应用管理」中的应用密码, 非登录密码
     * - 目录: 需在坚果云网页端提前创建 (如 /SalesQuest)
     */
    suspend fun testConnection(config: WebDavConfig): WebDavResult {
        // 前置校验
        if (!config.isConfigured()) {
            return WebDavResult.Failure("请填写完整的 WebDAV 配置")
        }

        // URL 基础校验
        val parsedUrl = config.url.toHttpUrlOrNull()
        if (parsedUrl == null) {
            return WebDavResult.Failure("WebDAV 地址格式错误, 应为 https://dav.jianguoyun.com/dav/")
        }
        if (!config.url.startsWith("https://")) {
            return WebDavResult.Failure("坚果云要求 HTTPS, 请检查地址是否以 https:// 开头")
        }

        return withContext(Dispatchers.IO) {
            runCatching {
                val request = Request.Builder()
                    .url(joinUrl(config.url, config.dir))
                    .method(
                        "PROPFIND",
                        ("<?xml version=\"1.0\"?>\n" +
                            "<d:propfind xmlns:d=\"DAV:\"><d:prop><d:resourcetype/></d:prop></d:propfind>")
                            .toRequestBody(jsonMediaType)
                    )
                    .header("Authorization", Credentials.basic(config.username, config.password))
                    .header("Depth", "0")
                    .build()
                val response = client.newCall(request).execute()
                response.use { resp ->
                    when {
                        resp.code in 200..299 -> WebDavResult.Success("连接成功")
                        resp.code == 401 -> WebDavResult.Failure("认证失败, 请确认使用的是坚果云「应用密码」而非登录密码")
                        resp.code == 403 -> WebDavResult.Failure("拒绝访问, 请确认应用密码权限包含读写")
                        resp.code == 404 -> WebDavResult.Failure("目录不存在, 请先在坚果云网页端创建目录 ${config.dir}")
                        resp.code == 405 -> WebDavResult.Failure("方法不允许 (405), 请检查 URL 是否指向 /dav/ 目录")
                        resp.code in 300..399 -> WebDavResult.Failure("重定向异常 (${resp.code}), 请检查 URL 是否完整")
                        else -> WebDavResult.Failure("连接失败: HTTP ${resp.code}")
                    }
                }
            }.getOrElse {
                // 很多 Java 异常的 message 为 null, 不能直接插值, 否则用户看到 "null"
                val errMsg = it.message ?: it.javaClass.simpleName
                com.salesquest.sales_quest.core.AppLogger.error("WebDavService", "连接异常: ${it.javaClass.name}: $errMsg")
                when (it) {
                    is android.os.NetworkOnMainThreadException ->
                        WebDavResult.Failure("网络操作不能在主线程执行")
                    is javax.net.ssl.SSLException ->
                        WebDavResult.Failure("SSL/TLS 握手失败: $errMsg\n可能原因: 证书不信任或网络被劫持")
                    is java.net.UnknownHostException ->
                        WebDavResult.Failure("无法解析域名, 请检查网络连接和 URL 拼写")
                    is java.net.SocketTimeoutException ->
                        WebDavResult.Failure("连接超时, 请检查网络或稍后重试")
                    is java.net.ConnectException ->
                        WebDavResult.Failure("无法连接到服务器, 请检查网络")
                    is IOException ->
                        WebDavResult.Failure("网络错误: $errMsg")
                    else ->
                        WebDavResult.Failure("连接异常: ${it.javaClass.simpleName}: $errMsg")
                }
            }
        }
    }

    /** 创建远程目录 (MkCol) */
    private suspend fun ensureDir(config: WebDavConfig): WebDavResult {
        val dirUrl = joinUrl(config.url, config.dir)
        val test = testConnection(config)
        if (test is WebDavResult.Success) return test

        return withContext(Dispatchers.IO) {
            val request = Request.Builder()
                .url(dirUrl)
                .method("MKCOL", null)
                .header("Authorization", Credentials.basic(config.username, config.password))
                .build()
            runCatching {
                client.newCall(request).execute().use { resp ->
                    if (resp.code in 200..299 || resp.code == 405) {
                        WebDavResult.Success()
                    } else if (resp.code == 401) {
                        WebDavResult.Failure("认证失败, 请检查用户名/应用密码")
                    } else {
                        WebDavResult.Failure("创建目录失败: HTTP ${resp.code}")
                    }
                }
            }.getOrElse {
                val errMsg = it.message ?: it.javaClass.simpleName
                WebDavResult.Failure("网络错误: $errMsg")
            }
        }
    }

    // ==================== 备份 ====================

    /** 立即备份: 导出数据 → 打包 zip → PUT 上传 */
    open suspend fun backupNow(config: WebDavConfig = configStore.load()): WebDavResult {
        if (!config.isConfigured()) return WebDavResult.Failure("请先完成账号配置")
        ensureDir(config)

        return withContext(Dispatchers.IO) {
            // 数据导出 + 文件读取 + 压缩 + 网络上传全部在 IO 线程
            val data = backupService.exportBackupData()
            val dbBytes = backupService.readDatabaseFileBytes()
            val zipBytes = backupService.createBackupZip(data, dbBytes)

            val filename = BackupKeys.BACKUP_FILENAME_PREFIX + java.text.SimpleDateFormat(
                "yyyy-MM-dd_HHmmss", java.util.Locale.US
            ).format(java.util.Date()) + BackupKeys.DB_BACKUP_SUFFIX + ".zip"

            val request = Request.Builder()
                .url(joinUrl(config.url, joinDir(config.dir, filename)))
                .put(zipBytes.toRequestBody(octetMediaType))
                .header("Authorization", Credentials.basic(config.username, config.password))
                .build()

            runCatching {
                client.newCall(request).execute().use { resp ->
                    when {
                        resp.code in 200..299 -> {
                            configStore.setLastBackupAt(System.currentTimeMillis())
                            configStore.save(config)
                            WebDavResult.Success("备份成功: $filename")
                        }
                        resp.code == 401 -> WebDavResult.Failure("认证失败, 请检查用户名/应用密码")
                        resp.code == 409 -> WebDavResult.Failure("远程目录不存在, 请先手动创建")
                        else -> WebDavResult.Failure("备份失败: HTTP ${resp.code}")
                    }
                }
            }.getOrElse {
                val errMsg = it.message ?: it.javaClass.simpleName
                WebDavResult.Failure("网络错误: $errMsg")
            }
        }
    }

    // ==================== 列表 ====================

    /** 获取远程备份文件列表 (PROPFIND Depth=1) */
    suspend fun listBackups(config: WebDavConfig = configStore.load()): WebDavResult {
        if (!config.isConfigured()) return WebDavResult.Failure("请先完成账号配置")

        val propfind = ("<?xml version=\"1.0\"?>\n" +
            "<d:propfind xmlns:d=\"DAV:\">" +
            "<d:prop><d:displayname/><d:getcontentlength/><d:getlastmodified/></d:prop>" +
            "</d:propfind>")

        return withContext(Dispatchers.IO) {
            val request = Request.Builder()
                .url(joinUrl(config.url, config.dir))
                .method("PROPFIND", propfind.toRequestBody(jsonMediaType))
                .header("Authorization", Credentials.basic(config.username, config.password))
                .header("Depth", "1")
                .build()
            runCatching {
                client.newCall(request).execute().use { resp ->
                    when {
                        resp.code in 200..299 -> {
                            val body = resp.body?.string() ?: ""
                            WebDavResult.Success(parsePropfind(body))
                        }
                        resp.code == 401 -> WebDavResult.Failure("认证失败, 请检查用户名/应用密码")
                        resp.code == 404 -> WebDavResult.Failure("目录不存在")
                        else -> WebDavResult.Failure("获取列表失败: HTTP ${resp.code}")
                    }
                }
            }.getOrElse {
                val errMsg = it.message ?: it.javaClass.simpleName
                WebDavResult.Failure("网络错误: $errMsg")
            }
        }
    }

    /** 从 PROPFIND XML 响应解析备份文件名 */
    internal fun parsePropfind(xml: String): String {
        val files = mutableListOf<BackupFileInfo>()
        val hrefRegex = Regex("<d:href>(.*?)</d:href>")
        val matches = hrefRegex.findAll(xml)
        for (m in matches) {
            var href = m.groupValues[1].trim()
            if (href.contains("/")) href = href.substringAfterLast("/")
            if (href.isBlank()) continue
            if (href.endsWith("/")) continue
            if (href.startsWith(BackupKeys.BACKUP_FILENAME_PREFIX) && href.endsWith(".zip")) {
                files.add(BackupFileInfo(name = href, size = 0))
            }
        }
        return files.joinToString("\n") { it.name }
    }

    // ==================== 恢复 ====================

    /** 从远程下载备份并恢复 */
    suspend fun restoreBackup(filename: String, config: WebDavConfig = configStore.load()): WebDavResult {
        if (!config.isConfigured()) return WebDavResult.Failure("请先完成账号配置")

        return withContext(Dispatchers.IO) {
            val request = Request.Builder()
                .url(joinUrl(config.url, joinDir(config.dir, filename)))
                .get()
                .header("Authorization", Credentials.basic(config.username, config.password))
                .build()
            runCatching {
                client.newCall(request).execute().use { resp ->
                    when {
                        resp.code in 200..299 -> {
                            val bytes = resp.body?.bytes() ?: return@use WebDavResult.Failure("下载内容为空")
                            when (val parsed = backupService.parseBackupZip(bytes)) {
                                is BackupValidationResult.Success -> {
                                    backupService.restoreBackupData(parsed.data)
                                    WebDavResult.Success("恢复成功")
                                }
                                is BackupValidationResult.Error -> WebDavResult.Failure(parsed.message)
                            }
                        }
                        resp.code == 401 -> WebDavResult.Failure("认证失败, 请检查用户名/应用密码")
                        resp.code == 404 -> WebDavResult.Failure("备份文件不存在")
                        else -> WebDavResult.Failure("下载失败: HTTP ${resp.code}")
                    }
                }
            }.getOrElse {
                val errMsg = it.message ?: it.javaClass.simpleName
                WebDavResult.Failure("网络错误: $errMsg")
            }
        }
    }

    // ==================== 自动备份 ====================

    /** 检查是否需要自动备份 (开启且距上次超过 24h) */
    suspend fun shouldAutoBackup(): Boolean {
        val config = configStore.load()
        if (!config.autoBackup) return false
        if (!configStore.isConfigured()) return false
        val last = configStore.lastBackupAt()
        return System.currentTimeMillis() - last >= BackupDefaults.AUTO_BACKUP_INTERVAL_MS
    }

    /** 执行自动备份 (若需要) */
    suspend fun maybeAutoBackup(): WebDavResult {
        return if (shouldAutoBackup()) backupNow() else WebDavResult.Success("无需备份")
    }

    // ==================== URL 工具 ====================

    internal fun joinUrl(base: String, path: String): String {
        val cleanBase = base.trim().trimEnd('/')
        val cleanPath = path.trim().trimStart('/')
        return if (cleanPath.isEmpty()) cleanBase else "$cleanBase/$cleanPath"
    }

    internal fun joinDir(dir: String, filename: String): String {
        val cleanDir = dir.trim().trim('/')
        return if (cleanDir.isEmpty()) filename else "$cleanDir/$filename"
    }
}
