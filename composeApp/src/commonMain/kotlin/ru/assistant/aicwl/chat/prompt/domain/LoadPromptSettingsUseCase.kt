package ru.assistant.aicwl.chat.prompt.domain

import ru.assistant.aicwl.chat.prompt.data.PromptSettingsRepository
import ru.assistant.aicwl.chat.prompt.model.PromptSettings

/**
 * Use case for loading prompt settings.
 * Domain layer - business logic for settings operations.
 */
class LoadPromptSettingsUseCase(
    private val repository: PromptSettingsRepository
) {
    suspend operator fun invoke(): PromptSettings =
        repository.getSettings()
}
