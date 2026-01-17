package ru.assistant.aicwl.chat.prompt.domain

import ru.assistant.aicwl.chat.prompt.SystemPromptConfig
import ru.assistant.aicwl.chat.prompt.data.PromptSettingsRepository

/**
 * Use case for updating custom main prompt.
 * Domain layer - business logic for settings operations.
 *
 * After saving to repository, synchronizes SystemPromptConfig state
 * to ensure changes are immediately reflected in system prompts.
 */
class UpdateCustomPromptUseCase(
    private val repository: PromptSettingsRepository
) {
    suspend operator fun invoke(prompt: String) {
        repository.saveCustomPrompt(prompt)
        SystemPromptConfig.refreshFromRepository(repository)
    }
}

/**
 * Use case for adding a new rule.
 *
 * After saving to repository, synchronizes SystemPromptConfig state
 * to ensure the new rule is immediately included in system prompts.
 */
class AddPromptRuleUseCase(
    private val repository: PromptSettingsRepository
) {
    suspend operator fun invoke(ruleText: String) {
        repository.addRule(ruleText)
        SystemPromptConfig.refreshFromRepository(repository)
    }
}

/**
 * Use case for removing a rule.
 *
 * After removing from repository, synchronizes SystemPromptConfig state
 * to ensure the rule is immediately excluded from system prompts.
 */
class RemovePromptRuleUseCase(
    private val repository: PromptSettingsRepository
) {
    suspend operator fun invoke(ruleId: String) {
        repository.removeRule(ruleId)
        SystemPromptConfig.refreshFromRepository(repository)
    }
}

/**
 * Use case for resetting to default prompt.
 *
 * After resetting in repository, synchronizes SystemPromptConfig state
 * to ensure the default prompt is immediately used.
 */
class ResetPromptUseCase(
    private val repository: PromptSettingsRepository
) {
    suspend operator fun invoke() {
        repository.resetCustomPrompt()
        SystemPromptConfig.refreshFromRepository(repository)
    }
}

/**
 * Use case for clearing all rules.
 *
 * After clearing in repository, synchronizes SystemPromptConfig state
 * to ensure all rules are immediately removed from system prompts.
 */
class ClearPromptRulesUseCase(
    private val repository: PromptSettingsRepository
) {
    suspend operator fun invoke() {
        repository.clearRules()
        SystemPromptConfig.refreshFromRepository(repository)
    }
}
