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
            inputCostPerMillion = 10.0,
            outputCostPerMillion = 10.0,
            tier = ModelTier.SENIOR
        ),
        UnifiedAIModel(
            providerType = ProviderType.ZAI,
            modelId = "glm-4.7-flash",
            displayName = "GLM-4.7-Flash (Middle)",
            description = "Balanced performance and speed",
            maxTokens = 131072,
            inputCostPerMillion = 0.5,
            outputCostPerMillion = 0.5,
            tier = ModelTier.MIDDLE
        ),
        UnifiedAIModel(
            providerType = ProviderType.ZAI,
            modelId = "glm-4.5-air",
            displayName = "GLM-4.5-Air (Junior)",
            description = "Fastest, best for simple tasks",
            maxTokens = 98304,
            inputCostPerMillion = 0.1,
            outputCostPerMillion = 0.1,
            tier = ModelTier.JUNIOR
        )
    )

    // ============== QWEN MODELS ==============
    private val qwenModels = listOf(
        UnifiedAIModel(
            providerType = ProviderType.QWEN,
            modelId = "gangchen/Qwen2.5-0.5B-Instruct-Gensyn-Swarm-zealous_scurrying_cat:featherless-ai",
            displayName = "Qwen2.5-0.5B (Junior)",
            description = "Lightweight model via HuggingFace",
            maxTokens = 32768,
            inputCostPerMillion = null,
            outputCostPerMillion = null,
            tier = ModelTier.JUNIOR
        )
    )

    // ============== OREAL MODELS ==============
    private val orealModels = listOf(
        UnifiedAIModel(
            providerType = ProviderType.OREAL,
            modelId = "internlm/OREAL-7B-SFT:featherless-ai",
            displayName = "OREAL-7B-SFT (Middle)",
            description = "InternLM OREAL model via HuggingFace",
            maxTokens = 32768,
            inputCostPerMillion = null,
            outputCostPerMillion = null,
            tier = ModelTier.MIDDLE
        )
    )

    // ============== ALL MODELS ==============
    private val allModels = listOf(
        zaiModels,
        qwenModels,
        orealModels
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
