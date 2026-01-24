package ru.assistant.aicwl.chat.provider.model

import ru.assistant.aicwl.chat.provider.ProviderType

/**
 * Unified representation of an AI model across all providers.
 * Provides a consistent interface for model selection regardless of provider.
 *
 * @property providerType The AI provider (Z.ai)
 * @property modelId Provider-specific model identifier
 * @property displayName Human-readable name for UI
 * @property description Brief description of model capabilities
 * @property maxTokens Maximum context window size
 * @property inputCostPerMillion Cost for input tokens (USD)
 * @property outputCostPerMillion Cost for output tokens (USD)
 * @property tier Model tier (Senior, Middle, Junior)
 */
data class UnifiedAIModel(
    val providerType: ProviderType,
    val modelId: String,
    val displayName: String,
    val description: String,
    val maxTokens: Int,
    val inputCostPerMillion: Double? = null,
    val outputCostPerMillion: Double? = null,
    val tier: ModelTier = ModelTier.MIDDLE
) {
    /**
     * Unique identifier combining provider and model.
     */
    val uniqueId: String = "${providerType.name}_$modelId"

    /**
     * Check if this model supports chain-of-thought reasoning.
     */
    val supportsThinking: Boolean
        get() = when (providerType) {
            ProviderType.ZAI -> modelId.startsWith("glm-4")
            ProviderType.QWEN -> false // Qwen doesn't support thinking in HuggingFace format
            ProviderType.OREAL -> false // Oreal doesn't support thinking
        }

    /**
     * Estimated cost for processing a request (in USD).
     */
    fun estimateCost(inputTokens: Int, outputTokens: Int): Double? {
        val inputCost = (inputCostPerMillion ?: return null) * inputTokens / 1_000_000
        val outputCost = (outputCostPerMillion ?: return null) * outputTokens / 1_000_000
        return inputCost + outputCost
    }
}

/**
 * Model tier for quick categorization.
 */
enum class ModelTier {
    SENIOR,      // Most capable, slower, more expensive
    MIDDLE,      // Balanced
    JUNIOR       // Fastest, least capable, cheapest
}
