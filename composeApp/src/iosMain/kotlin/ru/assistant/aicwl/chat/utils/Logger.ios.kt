package ru.assistant.aicwl.chat.utils

import platform.Foundation.NSLog

actual fun createLogger(tag: String): PlatformLogger {
    return object : PlatformLogger {
        override fun d(message: String) {
            NSLog("[DEBUG] [$tag] $message")
        }

        override fun i(message: String) {
            NSLog("[INFO] [$tag] $message")
        }

        override fun w(message: String) {
            NSLog("[WARN] [$tag] $message")
        }

        override fun e(message: String) {
            NSLog("[ERROR] [$tag] $message")
        }

        override fun e(message: String, throwable: Throwable) {
            NSLog("[ERROR] [$tag] $message: ${throwable.message}")
        }
    }
}
