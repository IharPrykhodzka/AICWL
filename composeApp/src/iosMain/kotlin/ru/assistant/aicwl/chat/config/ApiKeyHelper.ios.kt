package ru.assistant.aicwl.chat.config

import platform.Foundation.NSUserDefaults

/**
 * Вспомогательный объект для управления API-ключом на iOS.
 * Может использоваться для программной установки API-ключа.
 */
object ApiKeyHelper {
    private const val KEY = "LLM_Z_API_KEY"

    /**
     * Устанавливает API-ключ программно (сохраняется в UserDefaults).
     * Вызовите этот метод перед выполнением любых API-запросов.
     *
     * Пример:
     * ```kotlin
     * ApiKeyHelper.setApiKey("your-api-key-here")
     * ```
     */
    fun setApiKey(apiKey: String) {
        NSUserDefaults.standardUserDefaults.setObject(apiKey, KEY)
        NSUserDefaults.standardUserDefaults.synchronize()
    }

    /**
     * Возвращает текущий сохранённый API-ключ из UserDefaults.
     * Возвращает null, если ключ не установлен.
     */
    fun getApiKey(): String? {
        return NSUserDefaults.standardUserDefaults.stringForKey(KEY)
    }

    /**
     * Удаляет сохранённый API-ключ из UserDefaults.
     */
    fun clearApiKey() {
        NSUserDefaults.standardUserDefaults.removeObjectForKey(KEY)
        NSUserDefaults.standardUserDefaults.synchronize()
    }
}
