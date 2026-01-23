package ru.assistant.aicwl.chat.provider.model

import ru.assistant.aicwl.chat.provider.ProviderType

/**
 * Central registry of all available AI models across providers.
 * This is the single source of truth for model configurations.
 */
object AIModelConfig {

    // ============== Z.AI MODELS ==============
    private val zaiModels = listOf(
        UnifiedAIModel(
            providerType = ProviderType.ZAI,
            modelId = "glm-4.7",
            displayName = "GLM-4.7 (Senior)",
            description = "Most powerful, best for complex tasks",
            maxTokens = 131072,
            inputCostPerMillion = null, // Pricing not publicly available
            outputCostPerMillion = null,
            tier = ModelTier.SENIOR
        ),
        UnifiedAIModel(
            providerType = ProviderType.ZAI,
            modelId = "glm-4.7-flash",
            displayName = "GLM-4.7-Flash (Middle)",
            description = "Balanced performance and speed",
            maxTokens = 131072,
            tier = ModelTier.MIDDLE
        ),
        UnifiedAIModel(
            providerType = ProviderType.ZAI,
            modelId = "glm-4.5-air",
            displayName = "GLM-4.5-Air (Junior)",
            description = "Fastest, best for simple tasks",
            maxTokens = 98304,
            tier = ModelTier.JUNIOR
        )
    )

    // ============== OPENAI MODELS ==============
    private val openaiModels = listOf(
        UnifiedAIModel(
            providerType = ProviderType.OPENAI,
            modelId = "gpt-4o",
            displayName = "GPT-4o (Senior)",
            description = "Most capable, multimodal, fast",
            maxTokens = 128000,
            inputCostPerMillion = 5.0,
            outputCostPerMillion = 15.0,
            tier = ModelTier.SENIOR
        ),
        UnifiedAIModel(
            providerType = ProviderType.OPENAI,
            modelId = "gpt-4o-mini",
            displayName = "GPT-4o-mini (Middle)",
            description = "Fast and cost-effective",
            maxTokens = 128000,
            inputCostPerMillion = 0.15,
            outputCostPerMillion = 0.60,
            tier = ModelTier.MIDDLE
        ),
        UnifiedAIModel(
            providerType = ProviderType.OPENAI,
            modelId = "gpt-3.5-turbo",
            displayName = "GPT-3.5-Turbo (Junior)",
            description = "Fastest, most affordable",
            maxTokens = 16385,
            inputCostPerMillion = 0.50,
            outputCostPerMillion = 1.50,
            tier = ModelTier.JUNIOR
        )
    )

    // ============== ANTHROPIC MODELS ==============
    private val anthropicModels = listOf(
        UnifiedAIModel(
            providerType = ProviderType.ANTHROPIC,
            modelId = "claude-3-5-sonnet-20241022",
            displayName = "Claude 3.5 Sonnet (Senior)",
            description = "Most capable, excellent for complex tasks",
            maxTokens = 200000,
            inputCostPerMillion = 3.0,
            outputCostPerMillion = 15.0,
            tier = ModelTier.SENIOR
        ),
        UnifiedAIModel(
            providerType = ProviderType.ANTHROPIC,
            modelId = "claude-3-5-haiku-20241022",
            displayName = "Claude 3.5 Haiku (Middle)",
            description = "Fast and cost-effective",
            maxTokens = 200000,
            inputCostPerMillion = 0.25,
            outputCostPerMillion = 1.25,
            tier = ModelTier.MIDDLE
        ),
        UnifiedAIModel(
            providerType = ProviderType.ANTHROPIC,
            modelId = "claude-3-opus-20240229",
            displayName = "Claude 3 Opus (Ultra)",
            description = "Most powerful, slower",
            maxTokens = 200000,
            inputCostPerMillion = 15.0,
            outputCostPerMillion = 75.0,
            tier = ModelTier.SENIOR
        )
    )

    // ============== ALL MODELS ==============
    private val allModels = listOf(
        zaiModels,
        openaiModels,
        anthropicModels
    ).flatten()

    /**
     * Get all available models.
     */
    fun getAllModels(): List<UnifiedAIModel> = allModels

    /**
     * Get models by provider.
     */
    fun getModelsByProvider(provider: ProviderType): List<UnifiedAIModel> {
        return allModels.filter { it.providerType == provider }
    }

    /**
     * Get default model for a provider.
     */
    fun getDefaultModelForProvider(provider: ProviderType): UnifiedAIModel {
        return getModelsByProvider(provider).firstOrNull { it.tier == ModelTier.MIDDLE }
            ?: getModelsByProvider(provider).first()
    }

    /**
     * Get model by unique ID.
     */
    fun getModelByUniqueId(uniqueId: String): UnifiedAIModel? {
        return allModels.find { it.uniqueId == uniqueId }
    }

    /**
     * Get default model (currently Z.ai Middle tier).
     */
    val defaultModel: UnifiedAIModel
        get() = getDefaultModelForProvider(ProviderType.ZAI)
}
