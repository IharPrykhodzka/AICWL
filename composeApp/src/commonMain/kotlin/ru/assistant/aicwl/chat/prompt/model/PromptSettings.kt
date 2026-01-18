package ru.assistant.aicwl.chat.prompt.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * Domain model representing additional prompt rule.
 */
@Serializable
data class PromptRuleData(
    val id: String,
    val text: String,
    val enabled: Boolean = true
)

/**
 * Domain model representing all prompt settings.
 */
@Serializable
data class PromptSettings(
    val customMainPrompt: String? = null,
    val additionalRules: List<PromptRuleData> = emptyList(),
    val saveChatHistory: Boolean = true  // Default to true for better UX
) {
    /**
     * Checks if a custom main prompt is set.
     */
    fun hasCustomPrompt(): Boolean = !customMainPrompt.isNullOrBlank()

    /**
     * Gets the main prompt to use, falling back to default if custom is not set.
     */
    fun getEffectivePrompt(defaultPrompt: String): String =
        if (hasCustomPrompt()) customMainPrompt!! else defaultPrompt

    companion object {
        private val json = Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
        }

        fun serialize(settings: PromptSettings): String =
            json.encodeToString(settings)

        fun deserialize(serialized: String): PromptSettings? =
            try {
                json.decodeFromString<PromptSettings>(serialized)
            } catch (e: Exception) {
                null
            }
    }
}
