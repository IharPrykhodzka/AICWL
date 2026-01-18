package ru.assistant.aicwl.chat.prompt.ui

import ru.assistant.aicwl.chat.prompt.data.PromptPreferences
import ru.assistant.aicwl.chat.prompt.data.PromptSettingsRepository
import ru.assistant.aicwl.chat.prompt.data.PromptSettingsRepositoryImpl
import ru.assistant.aicwl.chat.prompt.domain.*
import ru.assistant.aicwl.chat.data.ChatHistoryRepository
import ru.assistant.aicwl.chat.data.ChatHistoryRepositoryImpl
import ru.assistant.aicwl.chat.data.ChatHistoryPreferences

/**
 * Factory for creating PromptSettingsViewModel with dependencies.
 * Simplified DI - in production use Kodein/Koin.
 */
object PromptSettingsViewModelFactory {
    private var defaultMainPrompt: String = ""
    private var repository: PromptSettingsRepository? = null
    private var chatHistoryRepository: ChatHistoryRepository? = null

    fun initialize(
        defaultPrompt: String,
        preferences: PromptPreferences,
        chatHistoryPrefs: ChatHistoryPreferences
    ) {
        defaultMainPrompt = defaultPrompt
        repository = PromptSettingsRepositoryImpl(preferences)
        chatHistoryRepository = ChatHistoryRepositoryImpl(chatHistoryPrefs, repository!!)
    }

    fun create(coroutineScope: kotlinx.coroutines.CoroutineScope?): PromptSettingsViewModel {
        val repo = repository ?: throw IllegalStateException(
            "PromptSettingsViewModelFactory not initialized. Call initialize() first."
        )
        val chatHistoryRepo = chatHistoryRepository ?: throw IllegalStateException(
            "ChatHistoryRepository not initialized. Call initialize() first."
        )

        return PromptSettingsViewModel(
            loadSettingsUseCase = LoadPromptSettingsUseCase(repo),
            updateCustomPromptUseCase = UpdateCustomPromptUseCase(repo),
            addRuleUseCase = AddPromptRuleUseCase(repo),
            removeRuleUseCase = RemovePromptRuleUseCase(repo),
            resetPromptUseCase = ResetPromptUseCase(repo),
            clearRulesUseCase = ClearPromptRulesUseCase(repo),
            toggleChatHistoryUseCase = ToggleChatHistoryUseCase(repo),
            clearChatHistoryUseCase = ClearChatHistoryUseCase(chatHistoryRepo),
            defaultMainPrompt = defaultMainPrompt,
            coroutineScope = coroutineScope
        )
    }

    fun getRepository(): PromptSettingsRepository =
        repository ?: throw IllegalStateException("Repository not initialized")

    fun getChatHistoryRepository(): ChatHistoryRepository =
        chatHistoryRepository ?: throw IllegalStateException("ChatHistoryRepository not initialized")
}
