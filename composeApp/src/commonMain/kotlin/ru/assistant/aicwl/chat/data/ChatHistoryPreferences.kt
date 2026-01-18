package ru.assistant.aicwl.chat.data

import kotlinx.coroutines.flow.Flow

/**
 * Platform-agnostic expect class for storing chat history.
 * Uses expect/actual pattern for platform-specific implementations.
 *
 * Data layer - handles persistence abstraction.
 */
expect class ChatHistoryPreferences() {

    /**
     * Observe chat history JSON changes.
     */
    fun getChatHistoryFlow(): Flow<String?>

    /**
     * Get current chat history JSON or null if not set.
     */
    suspend fun getChatHistory(): String?

    /**
     * Save chat history as JSON string.
     */
    suspend fun saveChatHistory(historyJson: String)

    /**
     * Clear chat history.
     */
    suspend fun clearChatHistory()
}
