package ru.assistant.aicwl.chat.data

import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing chat history persistence.
 * Follows Clean Architecture - Domain layer defines contract.
 *
 * This repository is responsible for:
 * - Saving chat history when enabled
 * - Loading chat history on app start
 * - Clearing chat history
 * - Checking if history persistence is enabled
 */
interface ChatHistoryRepository {
    /**
     * Flow of chat history for reactive updates.
     */
    fun getChatHistoryFlow(): Flow<ChatHistoryData?>

    /**
     * Get current chat history synchronously.
     */
    suspend fun getChatHistory(): ChatHistoryData?

    /**
     * Save chat history.
     */
    suspend fun saveChatHistory(history: ChatHistoryData)

    /**
     * Clear chat history.
     */
    suspend fun clearChatHistory()

    /**
     * Check if chat history persistence is enabled.
     */
    suspend fun isChatHistoryEnabled(): Boolean

    /**
     * Set chat history persistence enabled state.
     */
    suspend fun setChatHistoryEnabled(enabled: Boolean)
}
