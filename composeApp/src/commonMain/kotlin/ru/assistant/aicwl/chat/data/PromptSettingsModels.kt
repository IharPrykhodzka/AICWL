package ru.assistant.aicwl.chat.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

/**
 * Domain model representing custom prompt settings.
 * This is independent of data layer DTOs and storage mechanisms.
 */
@Serializable
data class PromptSettings(
    val customMainPrompt: String = "",
    val additionalRules: List<String> = emptyList()
) {
    /**
     * Checks if custom prompt is enabled (not empty).
     */
    fun isCustomPromptEnabled(): Boolean = customMainPrompt.isNotBlank()

    /**
     * Checks if there are any additional rules.
     */
    fun hasAdditionalRules(): Boolean = additionalRules.isNotEmpty()

    companion object {
        private val json = Json {
            ignoreUnknownKeys = true
            isLenient = true
            encodeDefaults = true
        }

        /**
         * Serializes settings to JSON string.
         */
        fun serialize(settings: PromptSettings): String {
            return json.encodeToString(settings)
        }

        /**
         * Deserializes settings from JSON string.
         * Returns default settings if parsing fails.
         */
        fun deserialize(jsonString: String): PromptSettings {
            return try {
                json.decodeFromString<PromptSettings>(jsonString)
            } catch (e: Exception) {
                PromptSettings() // Return default settings on error
            }
        }
    }
}

/**
 * Validation result for prompt settings.
 */
sealed class PromptValidationResult {
    data object Valid : PromptValidationResult()
    data class Invalid(val reason: String) : PromptValidationResult()
}

/**
 * Validator for prompt settings.
 */
object PromptSettingsValidator {
    /**
     * Validates the custom main prompt.
     * Checks for required JSON format structure.
     */
    fun validateMainPrompt(prompt: String): PromptValidationResult {
        if (prompt.isBlank()) {
            return PromptValidationResult.Valid // Empty is valid (will use default)
        }

        // Check if prompt contains essential JSON instructions
        val requiredKeywords = listOf("JSON", "content", "format", "ответ")
        val missingKeywords = requiredKeywords.filter { keyword ->
            !prompt.contains(keyword, ignoreCase = true)
        }

        if (missingKeywords.isNotEmpty()) {
            return PromptValidationResult.Invalid(
                "Промпт должен содержать инструкции о JSON формате и необходимые поля. " +
                        "Отсутствуют ключевые слова: ${missingKeywords.joinToString(", ")}"
            )
        }

        // Check if prompt is too short
        if (prompt.length < 50) {
            return PromptValidationResult.Invalid("Промпт слишком короткий. Минимум 50 символов.")
        }

        // Check for basic JSON structure instructions
        if (!prompt.contains("{", ignoreCase = true) || !prompt.contains("}", ignoreCase = true)) {
            return PromptValidationResult.Invalid(
                "Промпт должен содержать пример JSON структуры с фигурными скобками {}"
            )
        }

        return PromptValidationResult.Valid
    }

    /**
     * Validates an additional rule.
     */
    fun validateRule(rule: String): PromptValidationResult {
        if (rule.isBlank()) {
            return PromptValidationResult.Invalid("Правило не может быть пустым")
        }

        if (rule.length < 10) {
            return PromptValidationResult.Invalid("Правило слишком короткое. Минимум 10 символов.")
        }

        if (rule.length > 500) {
            return PromptValidationResult.Invalid("Правило слишком длинное. Максимум 500 символов.")
        }

        return PromptValidationResult.Valid
    }
}
