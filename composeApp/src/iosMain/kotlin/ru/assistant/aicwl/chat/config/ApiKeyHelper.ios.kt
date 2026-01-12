package ru.assistant.aicwl.chat.config

import platform.Foundation.NSUserDefaults

/**
 * Helper object for managing API key on iOS.
 * Can be used to set the API key programmatically.
 */
object ApiKeyHelper {
    private const val KEY = "LLM_Z_API_KEY"

    /**
     * Set the API key programmatically (stored in UserDefaults).
     * Call this before making any API requests.
     *
     * Example:
     * ```kotlin
     * ApiKeyHelper.setApiKey("your-api-key-here")
     * ```
     */
    fun setApiKey(apiKey: String) {
        NSUserDefaults.standardUserDefaults.setObject(apiKey, KEY)
        NSUserDefaults.standardUserDefaults.synchronize()
    }

    /**
     * Get the currently stored API key from UserDefaults.
     * Returns null if not set.
     */
    fun getApiKey(): String? {
        return NSUserDefaults.standardUserDefaults.stringForKey(KEY)
    }

    /**
     * Clear the stored API key from UserDefaults.
     */
    fun clearApiKey() {
        NSUserDefaults.standardUserDefaults.removeObjectForKey(KEY)
        NSUserDefaults.standardUserDefaults.synchronize()
    }
}
