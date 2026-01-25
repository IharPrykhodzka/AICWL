package ru.assistant.aicwl.chat.utils

/**
 * JVM implementation of PlatformTime using System.currentTimeMillis().
 */
actual object PlatformTime {
    actual fun currentTimeMillis(): Long = System.currentTimeMillis()
}
