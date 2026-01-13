package ru.assistant.aicwl.chat.utils

/**
 * Кроссплатформенный интерфейс логгера.
 */
interface PlatformLogger {
    fun d(message: String)
    fun i(message: String)
    fun w(message: String)
    fun e(message: String)
    fun e(message: String, throwable: Throwable)
}

/**
 * Фабрика кроссплатформенного логгера.
 * Создаёт платформенно-зависимые логгеры.
 */
expect fun createLogger(tag: String): PlatformLogger
