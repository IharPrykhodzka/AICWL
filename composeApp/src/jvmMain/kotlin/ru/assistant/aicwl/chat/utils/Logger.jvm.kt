package ru.assistant.aicwl.chat.utils

actual fun createLogger(tag: String): PlatformLogger {
    return object : PlatformLogger {
        override fun d(message: String) {
            println("[DEBUG] $tag: $message")
        }

        override fun i(message: String) {
            println("[INFO] $tag: $message")
        }

        override fun w(message: String) {
            println("[WARN] $tag: $message")
        }

        override fun e(message: String) {
            System.err.println("[ERROR] $tag: $message")
        }

        override fun e(message: String, throwable: Throwable) {
            System.err.println("[ERROR] $tag: $message")
            throwable.printStackTrace()
        }
    }
}
