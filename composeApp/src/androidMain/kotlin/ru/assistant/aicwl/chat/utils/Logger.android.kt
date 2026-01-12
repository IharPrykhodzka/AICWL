package ru.assistant.aicwl.chat.utils

import android.util.Log

actual fun createLogger(tag: String): PlatformLogger {
    return object : PlatformLogger {
        override fun d(message: String) {
            Log.d(tag, message)
        }

        override fun i(message: String) {
            Log.i(tag, message)
        }

        override fun w(message: String) {
            Log.w(tag, message)
        }

        override fun e(message: String) {
            Log.e(tag, message)
        }

        override fun e(message: String, throwable: Throwable) {
            Log.e(tag, message, throwable)
        }
    }
}
