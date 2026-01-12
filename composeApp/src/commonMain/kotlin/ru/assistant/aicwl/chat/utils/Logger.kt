package ru.assistant.aicwl.chat.utils

/**
 * Cross-platform logger interface.
 */
interface PlatformLogger {
    fun d(message: String)
    fun i(message: String)
    fun w(message: String)
    fun e(message: String)
    fun e(message: String, throwable: Throwable)
}

/**
 * Cross-platform logger factory.
 * Creates platform-specific loggers.
 */
expect fun createLogger(tag: String): PlatformLogger
