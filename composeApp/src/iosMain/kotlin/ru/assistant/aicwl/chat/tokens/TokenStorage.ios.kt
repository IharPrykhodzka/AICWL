package ru.assistant.aicwl.chat.tokens

import platform.Foundation.NSUserDefaults

/**
 * iOS реализация TokenStorage с использованием UserDefaults.
 */
actual class TokenStorage {
    private val userDefaults = NSUserDefaults.standardUserDefaults

    actual suspend fun loadTokenStatistics(): String? {
        return try {
            userDefaults.stringForKey(KEY_TOKEN_STATISTICS)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    actual suspend fun saveTokenStatistics(jsonData: String) {
        try {
            userDefaults.setObject(jsonData, KEY_TOKEN_STATISTICS)
            userDefaults.synchronize()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    actual suspend fun clearTokenStatistics() {
        try {
            userDefaults.removeObjectForKey(KEY_TOKEN_STATISTICS)
            userDefaults.synchronize()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        private const val KEY_TOKEN_STATISTICS = "token_stats_data"
    }
}
