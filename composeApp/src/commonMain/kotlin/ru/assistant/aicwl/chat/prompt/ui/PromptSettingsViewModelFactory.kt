package ru.assistant.aicwl.chat.prompt.ui

import ru.assistant.aicwl.chat.prompt.data.PromptPreferences
import ru.assistant.aicwl.chat.prompt.data.PromptSettingsRepository
import ru.assistant.aicwl.chat.prompt.data.PromptSettingsRepositoryImpl
import ru.assistant.aicwl.chat.prompt.domain.*

/**
 * Factory for creating PromptSettingsViewModel with dependencies.
 * Simplified DI - in production use Kodein/Koin.
 */
object PromptSettingsViewModelFactory {
    private var defaultMainPrompt: String = ""
    private var repository: PromptSettingsRepository? = null

    fun initialize(defaultPrompt: String, preferences: PromptPreferences) {
        defaultMainPrompt = defaultPrompt
        repository = PromptSettingsRepositoryImpl(preferences)
    }

    fun create(coroutineScope: kotlinx.coroutines.CoroutineScope?): PromptSettingsViewModel {
        val repo = repository ?: throw IllegalStateException(
            "PromptSettingsViewModelFactory not initialized. Call initialize() first."
        )

        return PromptSettingsViewModel(
            loadSettingsUseCase = LoadPromptSettingsUseCase(repo),
            updateCustomPromptUseCase = UpdateCustomPromptUseCase(repo),
            addRuleUseCase = AddPromptRuleUseCase(repo),
            removeRuleUseCase = RemovePromptRuleUseCase(repo),
            resetPromptUseCase = ResetPromptUseCase(repo),
            clearRulesUseCase = ClearPromptRulesUseCase(repo),
            defaultMainPrompt = defaultMainPrompt,
            coroutineScope = coroutineScope
        )
    }

    fun getRepository(): PromptSettingsRepository =
        repository ?: throw IllegalStateException("Repository not initialized")
}
