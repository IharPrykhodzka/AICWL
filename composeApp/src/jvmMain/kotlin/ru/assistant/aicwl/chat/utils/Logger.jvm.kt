package ru.assistant.aicwl.chat.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

actual fun createLogger(tag: String): PlatformLogger {
    return object : PlatformLogger {
        override val tag: String = tag

        override fun log(level: LogLevel, message: String) {
            val timestamp = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date())

            val (levelStr, colorCode) = when (level) {
                LogLevel.VERBOSE -> "[VERBOSE]" to "\u001B[37m"      // White
                LogLevel.DEBUG -> "[DEBUG]" to "\u001B[36m"         // Cyan
                LogLevel.INFO -> "[INFO]" to "\u001B[32m"           // Green
                LogLevel.WARNING -> "[WARN]" to "\u001B[33m"        // Yellow
                LogLevel.ERROR, LogLevel.NONE -> "[ERROR]" to "\u001B[31m"  // Red
            }
            val resetColor = "\u001B[0m"

            val output = "$resetColor[$timestamp]$colorCode $levelStr$resetColor $tag: $message"

            if (level == LogLevel.ERROR || level == LogLevel.NONE) {
                System.err.println(output)
            } else {
                println(output)
            }
        }
    }
}
