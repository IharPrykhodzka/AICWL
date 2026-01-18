package ru.assistant.aicwl.chat.prompt.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import java.util.prefs.Preferences

/**
 * JVM implementation using java.util.prefs.Preferences.
 */
actual class PromptPreferences actual constructor() {
    private companion object {
        const val KEY_CUSTOM_PROMPT = "custom_main_prompt"
        const val KEY_ADDITIONAL_RULES = "additional_rules"
        const val KEY_SAVE_CHAT_HISTORY = "save_chat_history"
    }

    private val prefs: Preferences = Preferences.userNodeForPackage(PromptPreferences::class.java)

    // In-memory flow for JVM since Preferences doesn't have built-in observation
    private val customPromptFlow = MutableStateFlow(getCustomPromptSync())
    private val additionalRulesFlow = MutableStateFlow(getAdditionalRulesSync())
    private val saveChatHistoryFlow = MutableStateFlow(getSaveChatHistorySync())

    actual fun getCustomPromptFlow(): Flow<String?> =
        customPromptFlow.asStateFlow()

    actual suspend fun getCustomPrompt(): String? = getCustomPromptSync()

    private fun getCustomPromptSync(): String? =
        prefs.get(KEY_CUSTOM_PROMPT, null)

    actual suspend fun saveCustomPrompt(prompt: String) {
        prefs.put(KEY_CUSTOM_PROMPT, prompt)
        prefs.flush()
        customPromptFlow.value = prompt
    }

    actual suspend fun clearCustomPrompt() {
        prefs.remove(KEY_CUSTOM_PROMPT)
        prefs.flush()
        customPromptFlow.value = null
    }

    actual fun getAdditionalRulesFlow(): Flow<String> =
        additionalRulesFlow.asStateFlow()

    actual suspend fun getAdditionalRules(): String = getAdditionalRulesSync()

    private fun getAdditionalRulesSync(): String =
        prefs.get(KEY_ADDITIONAL_RULES, "") ?: ""

    actual suspend fun saveAdditionalRules(rulesJson: String) {
        prefs.put(KEY_ADDITIONAL_RULES, rulesJson)
        prefs.flush()
        additionalRulesFlow.value = rulesJson
    }

    actual suspend fun clearAdditionalRules() {
        prefs.remove(KEY_ADDITIONAL_RULES)
        prefs.flush()
        additionalRulesFlow.value = ""
    }

    actual fun getSaveChatHistoryFlow(): Flow<Boolean> =
        saveChatHistoryFlow.asStateFlow()

    actual suspend fun getSaveChatHistory(): Boolean = getSaveChatHistorySync()

    private fun getSaveChatHistorySync(): Boolean =
        prefs.getBoolean(KEY_SAVE_CHAT_HISTORY, true)

    actual suspend fun setSaveChatHistory(enabled: Boolean) {
        prefs.putBoolean(KEY_SAVE_CHAT_HISTORY, enabled)
        prefs.flush()
        saveChatHistoryFlow.value = enabled
    }
}
