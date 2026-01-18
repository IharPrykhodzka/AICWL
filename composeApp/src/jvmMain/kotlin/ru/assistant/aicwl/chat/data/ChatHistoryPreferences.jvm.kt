package ru.assistant.aicwl.chat.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.prefs.Preferences

/**
 * JVM implementation using java.util.prefs.Preferences.
 * Works on Desktop (Windows, macOS, Linux).
 */
actual class ChatHistoryPreferences actual constructor() {
    private companion object {
        const val KEY_CHAT_HISTORY = "chat_history_json"
    }

    private val prefs: Preferences = Preferences.userNodeForPackage(ChatHistoryPreferences::class.java)
    private val _chatHistoryFlow = MutableStateFlow(loadChatHistory())

    actual fun getChatHistoryFlow(): Flow<String?> = _chatHistoryFlow.asStateFlow()

    actual suspend fun getChatHistory(): String? = loadChatHistory()

    actual suspend fun saveChatHistory(historyJson: String) {
        prefs.put(KEY_CHAT_HISTORY, historyJson)
        prefs.flush()
        _chatHistoryFlow.value = historyJson
    }

    actual suspend fun clearChatHistory() {
        prefs.remove(KEY_CHAT_HISTORY)
        prefs.flush()
        _chatHistoryFlow.value = null
    }

    private fun loadChatHistory(): String? {
        return prefs.get(KEY_CHAT_HISTORY, null)
    }
}
