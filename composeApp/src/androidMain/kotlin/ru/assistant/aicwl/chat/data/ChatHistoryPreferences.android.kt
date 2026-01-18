package ru.assistant.aicwl.chat.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * Android implementation using SharedPreferences.
 */
actual class ChatHistoryPreferences actual constructor() {
    private companion object {
        const val PREFS_NAME = "chat_history"
        const val KEY_CHAT_HISTORY = "chat_history_json"
    }

    private lateinit var prefs: SharedPreferences

    /**
     * Initialize with Android context.
     * Must be called before using preferences.
     */
    fun initialize(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    actual fun getChatHistoryFlow(): Flow<String?> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == KEY_CHAT_HISTORY) {
                trySend(prefs.getString(KEY_CHAT_HISTORY, null))
            }
        }

        prefs.registerOnSharedPreferenceChangeListener(listener)
        trySend(prefs.getString(KEY_CHAT_HISTORY, null))

        awaitClose {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }.distinctUntilChanged()

    actual suspend fun getChatHistory(): String? =
        prefs.getString(KEY_CHAT_HISTORY, null)

    actual suspend fun saveChatHistory(historyJson: String) {
        prefs.edit().putString(KEY_CHAT_HISTORY, historyJson).apply()
    }

    actual suspend fun clearChatHistory() {
        prefs.edit().remove(KEY_CHAT_HISTORY).apply()
    }
}
