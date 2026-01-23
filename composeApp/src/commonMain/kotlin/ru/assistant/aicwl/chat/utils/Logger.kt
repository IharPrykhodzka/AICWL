package ru.assistant.aicwl.chat.utils

/**
 * Уровни логирования.
 */
enum class LogLevel(val priority: Int) {
    VERBOSE(0),
    DEBUG(1),
    INFO(2),
    WARNING(3),
    ERROR(4),
    NONE(5); // Отключает все логи

    /**
     * Проверяет, соответствует ли уровень логирования минимальному уровню.
     */
    fun isEnabled(minLevel: LogLevel): Boolean = priority >= minLevel.priority
}

/**
 * Глобальная конфигурация логирования.
 */
object LoggingConfig {
    /**
     * Минимальный уровень логирования.
     * Логи с уровнем ниже этого не будут выводиться.
     */
    var minLevel: LogLevel = LogLevel.INFO

    /**
     * Включить verbose логирование (все уровни).
     */
    fun enableVerbose() {
        minLevel = LogLevel.VERBOSE
    }

    /**
     * Включить debug логирование.
     */
    fun enableDebug() {
        minLevel = LogLevel.DEBUG
    }

    /**
     * Установить уровень INFO (по умолчанию).
     */
    fun setInfoLevel() {
        minLevel = LogLevel.INFO
    }

    /**
     * Отключить все логи (кроме ERROR).
     */
    fun setErrorOnly() {
        minLevel = LogLevel.ERROR
    }

    /**
     * Отключить все логи.
     */
    fun disableAll() {
        minLevel = LogLevel.NONE
    }

    /**
     * Проверяет, включен ли указанный уровень логирования.
     */
    fun isEnabled(level: LogLevel): Boolean = level.isEnabled(minLevel)
}

/**
 * Кроссплатформенный интерфейс логгера с поддержкой уровней.
 */
interface PlatformLogger {
    val tag: String

    /**
     * Verbose лог - наиболее подробные сообщения.
     */
    fun v(message: String) {
        if (LoggingConfig.isEnabled(LogLevel.VERBOSE)) {
            log(LogLevel.VERBOSE, message)
        }
    }

    /**
     * Debug лог - для отладки.
     */
    fun d(message: String) {
        if (LoggingConfig.isEnabled(LogLevel.DEBUG)) {
            log(LogLevel.DEBUG, message)
        }
    }

    /**
     * Info лог - информационные сообщения.
     */
    fun i(message: String) {
        if (LoggingConfig.isEnabled(LogLevel.INFO)) {
            log(LogLevel.INFO, message)
        }
    }

    /**
     * Warning лог - предупреждения.
     */
    fun w(message: String) {
        if (LoggingConfig.isEnabled(LogLevel.WARNING)) {
            log(LogLevel.WARNING, message)
        }
    }

    /**
     * Warning лог с исключением.
     */
    fun w(message: String, throwable: Throwable) {
        if (LoggingConfig.isEnabled(LogLevel.WARNING)) {
            log(LogLevel.WARNING, "$message: ${throwable.message}")
        }
    }

    /**
     * Error лог - ошибки.
     */
    fun e(message: String) {
        if (LoggingConfig.isEnabled(LogLevel.ERROR)) {
            log(LogLevel.ERROR, message)
        }
    }

    /**
     * Error лог с исключением.
     */
    fun e(message: String, throwable: Throwable) {
        if (LoggingConfig.isEnabled(LogLevel.ERROR)) {
            log(LogLevel.ERROR, "$message: ${throwable.message}")
        }
    }

    /**
     * Базовый метод логирования, переопределяется в платформенных реализациях.
     */
    fun log(level: LogLevel, message: String)
}

/**
 * Фабрика кроссплатформенного логгера.
 * Создаёт платформенно-зависимые логгеры.
 */
expect fun createLogger(tag: String): PlatformLogger
