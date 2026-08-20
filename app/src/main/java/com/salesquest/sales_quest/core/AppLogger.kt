package com.salesquest.sales_quest.core

import android.util.Log
import java.text.SimpleDateFormat
import java.util.Collections
import java.util.Date
import java.util.Locale
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicLong

/** 日志级别 */
enum class LogLevel(val label: String) {
    DEBUG("DEBUG"),
    INFO("INFO"),
    WARNING("WARN"),
    ERROR("ERROR"),
    FATAL("FATAL")
}

/** 单条日志记录 */
data class LogEntry(
    val sequence: Long,
    val timestamp: Long,
    val level: LogLevel,
    val tag: String,
    val message: String,
    val stackTrace: String? = null,
    val metadata: Map<String, String>? = null
) {
    fun toFormattedString(): String {
        val sdf = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault())
        val ts = sdf.format(Date(timestamp))
        val meta = if (metadata != null && metadata.isNotEmpty()) " | $metadata" else ""
        val st = if (stackTrace != null) "\n  StackTrace:\n$stackTrace" else ""
        return "$ts [${level.label}] $tag: $message$meta$st"
    }
}

/** 全局应用日志服务 */
object AppLogger {

    private const val MAX_ENTRIES = 2000
    private val _entries = CopyOnWriteArrayList<LogEntry>()
    private val _listeners = CopyOnWriteArrayList<() -> Unit>()
    private val _sequence = AtomicLong(0)

    val entries: List<LogEntry> get() = Collections.unmodifiableList(_entries)

    fun debug(tag: String, message: String, metadata: Map<String, String>? = null) =
        log(LogLevel.DEBUG, tag, message, metadata = metadata)

    fun info(tag: String, message: String, metadata: Map<String, String>? = null) =
        log(LogLevel.INFO, tag, message, metadata = metadata)

    fun warning(tag: String, message: String, metadata: Map<String, String>? = null) =
        log(LogLevel.WARNING, tag, message, metadata = metadata)

    fun error(tag: String, message: String, stackTrace: String? = null, metadata: Map<String, String>? = null) =
        log(LogLevel.ERROR, tag, message, stackTrace, metadata)

    fun fatal(tag: String, message: String, stackTrace: String? = null, metadata: Map<String, String>? = null) =
        log(LogLevel.FATAL, tag, message, stackTrace, metadata)

    fun log(level: LogLevel, tag: String, message: String, stackTrace: String? = null, metadata: Map<String, String>? = null) {
        val entry = LogEntry(
            sequence = _sequence.incrementAndGet(),
            timestamp = System.currentTimeMillis(),
            level = level,
            tag = tag,
            message = message,
            stackTrace = stackTrace,
            metadata = metadata
        )
        _entries.add(entry)
        while (_entries.size > MAX_ENTRIES) {
            _entries.removeAt(0)
        }
        when (level) {
            LogLevel.DEBUG -> Log.d(tag, message)
            LogLevel.INFO -> Log.i(tag, message)
            LogLevel.WARNING -> Log.w(tag, message)
            LogLevel.ERROR -> Log.e(tag, message)
            LogLevel.FATAL -> Log.wtf(tag, message)
        }
        _listeners.forEach { it() }
    }

    fun addListener(l: () -> Unit) {
        _listeners.add(l)
    }

    fun removeListener(l: () -> Unit) {
        _listeners.remove(l)
    }

    fun exportPlainText(): String =
        _entries.joinToString("\n") { it.toFormattedString() }

    fun clear() {
        _entries.clear()
        _listeners.forEach { it() }
    }
}
