package ru.assistant.aicwl.chat.utils

/**
 * Platform-specific time utilities.
 * Provides access to system time in a platform-agnostic way.
 */
expect object PlatformTime {
    /**
     * Returns the current time in milliseconds since epoch.
     */
    fun currentTimeMillis(): Long
}
