package ru.assistant.aicwl.chat.tokens

import android.content.Context
import android.content.SharedPreferences

/**
 * Android реализация TokenStorage с использованием SharedPreferences.
 * Constructor без параметров использует application context через reflection.
 */
actual class TokenStorage {

    private val context: Context by lazy {
        getContextFromApplication()
    }

    private val preferences: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private fun getContextFromApplication(): Context {
        // Get application context via reflection
        try {
            val appContextClass = Class.forName("android.app.AppGlobals")
            val method = appContextClass.getDeclaredMethod("getInitialApplication")
            @Suppress("UNCHECKED_CAST")
            val application = method.invoke(null) as? android.app.Application
                ?: throw IllegalStateException("Application context not available")
            return application.applicationContext
        } catch (e: Exception) {
            throw IllegalStateException("Cannot get application context.", e)
        }
    }

    actual suspend fun loadTokenStatistics(): String? {
        return try {
            preferences.getString(KEY_TOKEN_STATISTICS, null)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    actual suspend fun saveTokenStatistics(jsonData: String) {
        try {
            preferences.edit().putString(KEY_TOKEN_STATISTICS, jsonData).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    actual suspend fun clearTokenStatistics() {
        try {
            preferences.edit().remove(KEY_TOKEN_STATISTICS).apply()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        private const val PREFS_NAME = "token_statistics"
        private const val KEY_TOKEN_STATISTICS = "token_stats_data"
    }
}
