package ru.assistant.aicwl.chat.prompt.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import ru.assistant.aicwl.chat.prompt.model.PromptRuleData
import ru.assistant.aicwl.chat.prompt.model.PromptSettings

/**
 * Implementation of PromptSettingsRepository.
 * Data layer - bridges platform-specific storage to domain models.
 */
class PromptSettingsRepositoryImpl(
    private val preferences: PromptPreferences
) : PromptSettingsRepository {

    override fun getSettingsFlow(): Flow<PromptSettings> =
        combine(
            preferences.getCustomPromptFlow(),
            preferences.getAdditionalRulesFlow()
        ) { customPrompt, rulesJson ->
            PromptSettings(
                customMainPrompt = customPrompt,
                additionalRules = deserializeRules(rulesJson)
            )
        }

    override suspend fun getSettings(): PromptSettings {
        val customPrompt = preferences.getCustomPrompt()
        val rulesJson = preferences.getAdditionalRules()
        return PromptSettings(
            customMainPrompt = customPrompt,
            additionalRules = deserializeRules(rulesJson)
        )
    }

    override suspend fun saveSettings(settings: PromptSettings) {
        if (settings.hasCustomPrompt()) {
            preferences.saveCustomPrompt(settings.customMainPrompt!!)
        } else {
            preferences.clearCustomPrompt()
        }

        val rulesJson = serializeRules(settings.additionalRules)
        if (rulesJson.isNotEmpty()) {
            preferences.saveAdditionalRules(rulesJson)
        } else {
            preferences.clearAdditionalRules()
        }
    }

    override suspend fun saveCustomPrompt(prompt: String) {
        if (prompt.isNotBlank()) {
            preferences.saveCustomPrompt(prompt)
        } else {
            preferences.clearCustomPrompt()
        }
    }

    override suspend fun addRule(ruleText: String) {
        val currentRules = deserializeRules(preferences.getAdditionalRules())
        val newRule = PromptRuleData(
            id = generateRuleId(),
            text = ruleText,
            enabled = true
        )
        val updatedRules = currentRules + newRule
        preferences.saveAdditionalRules(serializeRules(updatedRules))
    }

    override suspend fun removeRule(ruleId: String) {
        val currentRules = deserializeRules(preferences.getAdditionalRules())
        val updatedRules = currentRules.filter { it.id != ruleId }
        preferences.saveAdditionalRules(serializeRules(updatedRules))
    }

    override suspend fun clearRules() {
        preferences.clearAdditionalRules()
    }

    override suspend fun resetCustomPrompt() {
        preferences.clearCustomPrompt()
    }

    private fun serializeRules(rules: List<PromptRuleData>): String =
        PromptSettings.serialize(PromptSettings(additionalRules = rules))

    private fun deserializeRules(json: String): List<PromptRuleData> =
        if (json.isBlank()) {
            emptyList()
        } else {
            PromptSettings.deserialize(json)?.additionalRules ?: emptyList()
        }

    private fun generateRuleId(): String =
        "rule_${hashCode()}_${(0..9999).random()}"
}
