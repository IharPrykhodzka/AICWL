package ru.assistant.aicwl.chat.utils

import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970

/**
 * iOS implementation of PlatformTime using NSDate.
 */
actual object PlatformTime {
    actual fun currentTimeMillis(): Long {
        return (NSDate().timeIntervalSince1970 * 1000).toLong()
    }
}
