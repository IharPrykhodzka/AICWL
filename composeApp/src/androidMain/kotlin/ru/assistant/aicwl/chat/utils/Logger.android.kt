package ru.assistant.aicwl.chat.utils

import android.util.Log

actual fun createLogger(tag: String): PlatformLogger {
    return object : PlatformLogger {
        override val tag: String = tag

        override fun log(level: LogLevel, message: String) {
            when (level) {
                LogLevel.VERBOSE -> Log.v(tag, message)
                LogLevel.DEBUG -> Log.d(tag, message)
                LogLevel.INFO -> Log.i(tag, message)
                LogLevel.WARNING -> Log.w(tag, message)
                LogLevel.ERROR, LogLevel.NONE -> Log.e(tag, message)
            }
        }
    }
}
