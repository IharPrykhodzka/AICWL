package ru.assistant.aicwl.chat.utils

import platform.Foundation.NSLog

actual fun createLogger(tag: String): PlatformLogger {
    return object : PlatformLogger {
        override val tag: String = tag

        override fun log(level: LogLevel, message: String) {
            val prefix = when (level) {
                LogLevel.VERBOSE -> "VERBOSE"
                LogLevel.DEBUG -> "DEBUG"
                LogLevel.INFO -> "INFO"
                LogLevel.WARNING -> "WARN"
                LogLevel.ERROR, LogLevel.NONE -> "ERROR"
            }
            NSLog("[$prefix] [$tag] $message")
        }
    }
}
