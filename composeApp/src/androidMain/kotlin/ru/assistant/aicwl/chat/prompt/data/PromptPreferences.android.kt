package ru.assistant.aicwl.chat.prompt.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

/**
 * Android implementation using SharedPreferences.
 */
actual class PromptPreferences actual constructor() {
    private companion object {
        const val PREFS_NAME = "prompt_settings"
        const val KEY_CUSTOM_PROMPT = "custom_main_prompt"
        const val KEY_ADDITIONAL_RULES = "additional_rules"
        const val KEY_SAVE_CHAT_HISTORY = "save_chat_history"
    }

    private lateinit var prefs: SharedPreferences

    fun initialize(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    actual fun getCustomPromptFlow(): Flow<String?> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == KEY_CUSTOM_PROMPT) {
                trySend(prefs.getString(KEY_CUSTOM_PROMPT, null))
            }
        }

        prefs.registerOnSharedPreferenceChangeListener(listener)
        trySend(prefs.getString(KEY_CUSTOM_PROMPT, null))

        awaitClose {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }.distinctUntilChanged()

    actual suspend fun getCustomPrompt(): String? =
        prefs.getString(KEY_CUSTOM_PROMPT, null)

    actual suspend fun saveCustomPrompt(prompt: String) {
        prefs.edit().putString(KEY_CUSTOM_PROMPT, prompt).apply()
    }

    actual suspend fun clearCustomPrompt() {
        prefs.edit().remove(KEY_CUSTOM_PROMPT).apply()
    }

    actual fun getAdditionalRulesFlow(): Flow<String> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == KEY_ADDITIONAL_RULES) {
                trySend(prefs.getString(KEY_ADDITIONAL_RULES, "") ?: "")
            }
        }

        prefs.registerOnSharedPreferenceChangeListener(listener)
        trySend(prefs.getString(KEY_ADDITIONAL_RULES, "") ?: "")

        awaitClose {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }.distinctUntilChanged()

    actual suspend fun getAdditionalRules(): String =
        prefs.getString(KEY_ADDITIONAL_RULES, "") ?: ""

    actual suspend fun saveAdditionalRules(rulesJson: String) {
        prefs.edit().putString(KEY_ADDITIONAL_RULES, rulesJson).apply()
    }

    actual suspend fun clearAdditionalRules() {
        prefs.edit().remove(KEY_ADDITIONAL_RULES).apply()
    }

    actual fun getSaveChatHistoryFlow(): Flow<Boolean> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == KEY_SAVE_CHAT_HISTORY) {
                trySend(prefs.getBoolean(KEY_SAVE_CHAT_HISTORY, true))
            }
        }

        prefs.registerOnSharedPreferenceChangeListener(listener)
        trySend(prefs.getBoolean(KEY_SAVE_CHAT_HISTORY, true))

        awaitClose {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }.distinctUntilChanged()

    actual suspend fun getSaveChatHistory(): Boolean =
        prefs.getBoolean(KEY_SAVE_CHAT_HISTORY, true)

    actual suspend fun setSaveChatHistory(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SAVE_CHAT_HISTORY, enabled).apply()
    }
}
