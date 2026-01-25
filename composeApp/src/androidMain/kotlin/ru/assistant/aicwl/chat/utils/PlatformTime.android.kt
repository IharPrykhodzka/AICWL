package ru.assistant.aicwl.chat.utils

/**
 * Android implementation of PlatformTime using System.currentTimeMillis().
 */
actual object PlatformTime {
    actual fun currentTimeMillis(): Long = System.currentTimeMillis()
}
