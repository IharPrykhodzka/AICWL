package ru.assistant.aicwl.chat.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import platform.Foundation.NSUserDefaults

/**
 * iOS implementation using NSUserDefaults.
 */
actual class ChatHistoryPreferences actual constructor() {
    private companion object {
        const val KEY_CHAT_HISTORY = "chat_history_json"
    }

    private val userDefaults = NSUserDefaults.standardUserDefaults
    private val _chatHistoryFlow = MutableStateFlow(loadChatHistory())

    actual fun getChatHistoryFlow(): Flow<String?> = _chatHistoryFlow.asStateFlow()

    actual suspend fun getChatHistory(): String? = loadChatHistory()

    actual suspend fun saveChatHistory(historyJson: String) {
        userDefaults.setObject(historyJson, KEY_CHAT_HISTORY)
        userDefaults.synchronize()
        _chatHistoryFlow.value = historyJson
    }

    actual suspend fun clearChatHistory() {
        userDefaults.removeObjectForKey(KEY_CHAT_HISTORY)
        userDefaults.synchronize()
        _chatHistoryFlow.value = null
    }

    private fun loadChatHistory(): String? {
        return userDefaults.stringForKey(KEY_CHAT_HISTORY)
    }
}
