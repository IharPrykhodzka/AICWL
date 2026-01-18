package ru.assistant.aicwl.chat.prompt.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import platform.Foundation.NSUserDefaults

/**
 * iOS implementation using NSUserDefaults.
 */
actual class PromptPreferences actual constructor() {
    private companion object {
        const val KEY_CUSTOM_PROMPT = "custom_main_prompt"
        const val KEY_ADDITIONAL_RULES = "additional_rules"
        const val KEY_SAVE_CHAT_HISTORY = "save_chat_history"
    }

    private val userDefaults = NSUserDefaults.standardUserDefaults

    // In-memory flow for iOS since NSUserDefaults doesn't have built-in observation
    private val customPromptFlow = MutableStateFlow(getCustomPromptSync())
    private val additionalRulesFlow = MutableStateFlow(getAdditionalRulesSync())
    private val saveChatHistoryFlow = MutableStateFlow(getSaveChatHistorySync())

    actual fun getCustomPromptFlow(): Flow<String?> =
        customPromptFlow.asStateFlow()

    actual suspend fun getCustomPrompt(): String? = getCustomPromptSync()

    private fun getCustomPromptSync(): String? =
        userDefaults.stringForKey(KEY_CUSTOM_PROMPT)

    actual suspend fun saveCustomPrompt(prompt: String) {
        userDefaults.setObject(prompt, KEY_CUSTOM_PROMPT)
        customPromptFlow.value = prompt
    }

    actual suspend fun clearCustomPrompt() {
        userDefaults.removeObjectForKey(KEY_CUSTOM_PROMPT)
        customPromptFlow.value = null
    }

    actual fun getAdditionalRulesFlow(): Flow<String> =
        additionalRulesFlow.asStateFlow()

    actual suspend fun getAdditionalRules(): String = getAdditionalRulesSync()

    private fun getAdditionalRulesSync(): String =
        userDefaults.stringForKey(KEY_ADDITIONAL_RULES) ?: ""

    actual suspend fun saveAdditionalRules(rulesJson: String) {
        userDefaults.setObject(rulesJson, KEY_ADDITIONAL_RULES)
        additionalRulesFlow.value = rulesJson
    }

    actual suspend fun clearAdditionalRules() {
        userDefaults.removeObjectForKey(KEY_ADDITIONAL_RULES)
        additionalRulesFlow.value = ""
    }

    actual fun getSaveChatHistoryFlow(): Flow<Boolean> =
        saveChatHistoryFlow.asStateFlow()

    actual suspend fun getSaveChatHistory(): Boolean = getSaveChatHistorySync()

    private fun getSaveChatHistorySync(): Boolean {
        // NSUserDefaults doesn't have direct boolean support, use number
        val value = userDefaults.objectForKey(KEY_SAVE_CHAT_HISTORY)
        return when (value) {
            is Number -> value.toInt() == 1
            else -> true  // Default to true
        }
    }

    actual suspend fun setSaveChatHistory(enabled: Boolean) {
        userDefaults.setObject(if (enabled) 1 else 0, KEY_SAVE_CHAT_HISTORY)
        saveChatHistoryFlow.value = enabled
    }
}
