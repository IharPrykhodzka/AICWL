package ru.assistant.aicwl.chat.config

/**
 * Configuration constants for available AI models.
 * Each model has different capabilities and performance characteristics.
 */
object ModelConfig {
    /**
     * Most powerful model - slowest but highest quality responses.
     * Use for complex reasoning, code generation, and detailed analysis.
     */
    const val GLM_MODEL_SENIOR = "glm-4.7"

    /**
     * Balanced model - good quality with reasonable speed.
     * Recommended default for most use cases.
     */
    const val GLM_MODEL_MIDDLE = "glm-4.6"

    /**
     * Fastest model - lower quality but quickest responses.
     * Use for simple queries, quick summaries, and when speed matters most.
     */
    const val GLM_MODEL_JUNIOR = "glm-4.5-air"

    /**
     * Default model to use when user hasn't made a selection.
     */
    const val DEFAULT_MODEL = GLM_MODEL_MIDDLE

    /**
     * All available models in order of capability (highest to lowest).
     */
    val ALL_MODELS = listOf(
        GLM_MODEL_SENIOR,
        GLM_MODEL_MIDDLE,
        GLM_MODEL_JUNIOR
    )

    /**
     * Display names for models (for UI).
     */
    val MODEL_DISPLAY_NAMES = mapOf(
        GLM_MODEL_SENIOR to "Senior (GLM-4.7) - Most Powerful",
        GLM_MODEL_MIDDLE to "Middle (GLM-4.6) - Balanced",
        GLM_MODEL_JUNIOR to "Junior (GLM-4.5-Air) - Fastest"
    )

    /**
     * Get display name for a model ID.
     */
    fun getDisplayName(modelId: String): String {
        return MODEL_DISPLAY_NAMES[modelId] ?: modelId
    }
}
