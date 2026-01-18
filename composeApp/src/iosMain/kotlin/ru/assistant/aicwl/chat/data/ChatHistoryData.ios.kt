package ru.assistant.aicwl.chat.data

import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970

/**
 * iOS implementation using NSDate.
 * Returns milliseconds since Unix epoch.
 */
actual fun currentTimeMillis(): Long {
    val currentTime = NSDate()
    return (currentTime.timeIntervalSince1970 * 1000).toLong()
}
