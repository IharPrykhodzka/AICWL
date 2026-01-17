package ru.assistant.aicwl.chat.data

/**
 * Platform-agnostic interface for persistent storage of prompt settings.
 * This abstraction allows platform-specific implementations using expect/actual.
 */
interface PromptSettingsStorage {
    /**
     * Saves prompt settings to persistent storage.
     * @param settings The settings to save
     * @return Result indicating success or failure
     */
    suspend fun saveSettings(settings: PromptSettings): Result<Unit>

    /**
     * Loads prompt settings from persistent storage.
     * @return Result containing settings or default settings if not found
     */
    suspend fun loadSettings(): Result<PromptSettings>

    /**
     * Clears all saved settings, reverting to defaults.
     * @return Result indicating success or failure
     */
    suspend fun clearSettings(): Result<Unit>

    companion object {
        // Storage key for persisting settings
        const val STORAGE_KEY = "prompt_settings_v1"
    }
}
