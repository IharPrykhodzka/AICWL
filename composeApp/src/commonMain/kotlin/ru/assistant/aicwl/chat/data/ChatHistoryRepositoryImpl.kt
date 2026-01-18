package ru.assistant.aicwl.chat.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Implementation of ChatHistoryRepository.
 * Data layer - handles persistence and data transformation.
 *
 * Follows Clean Architecture:
 * - Implements domain layer interface
 * - Uses preferences for platform-specific storage
 * - Handles serialization/deserialization
 * - Maps between DTOs and domain models
 */
class ChatHistoryRepositoryImpl(
    private val preferences: ChatHistoryPreferences,
    private val promptSettingsRepository: ru.assistant.aicwl.chat.prompt.data.PromptSettingsRepository
) : ChatHistoryRepository {

    override fun getChatHistoryFlow(): Flow<ChatHistoryData?> {
        return preferences.getChatHistoryFlow().map { json ->
            json?.let { ChatHistoryData.deserialize(it) }
        }
    }

    override suspend fun getChatHistory(): ChatHistoryData? {
        val json = preferences.getChatHistory()
        return json?.let { ChatHistoryData.deserialize(it) }
    }

    override suspend fun saveChatHistory(history: ChatHistoryData) {
        val json = ChatHistoryData.serialize(history)
        preferences.saveChatHistory(json)
    }

    override suspend fun clearChatHistory() {
        preferences.clearChatHistory()
    }

    override suspend fun isChatHistoryEnabled(): Boolean {
        val settings = promptSettingsRepository.getSettings()
        return settings.saveChatHistory
    }

    override suspend fun setChatHistoryEnabled(enabled: Boolean) {
        val currentSettings = promptSettingsRepository.getSettings()
        val updatedSettings = currentSettings.copy(saveChatHistory = enabled)
        promptSettingsRepository.saveSettings(updatedSettings)
    }
}
