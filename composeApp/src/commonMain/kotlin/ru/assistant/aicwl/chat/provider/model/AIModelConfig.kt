package ru.assistant.aicwl.chat.provider.model

import ru.assistant.aicwl.chat.provider.ProviderType

/**
 * Central registry of all available AI models across providers.
 * This is the single source of truth for model configurations.
 */
object AIModelConfig {

    // ============== Z.AI MODELS ==============
    // Цены обновлены согласно https://open.bigmodel.cn/pricing
    // Prices per 1M tokens (USD)
    private val zaiModels = listOf(
        // === 🔴 SENIOR TIER - Most powerful, best for complex tasks ===
        UnifiedAIModel(
            providerType = ProviderType.ZAI,
            modelId = "glm-4.7",
            displayName = "🔴 GLM-4.7",
            description = "Most powerful, best for complex tasks",
            maxTokens = 131072,
            inputCostPerMillion = 0.6,
            outputCostPerMillion = 2.2,
            tier = ModelTier.SENIOR
        ),

        // === 🟡 MIDDLE TIER - Balanced performance and speed ===
        UnifiedAIModel(
            providerType = ProviderType.ZAI,
            modelId = "glm-4.5v",
            displayName = "🟡 GLM-4.5V",
            description = "Vision model with balanced performance",
            maxTokens = 131072,
            inputCostPerMillion = 0.6,
            outputCostPerMillion = 1.8,
            tier = ModelTier.MIDDLE
        ),

        // === 🟢 JUNIOR TIER - Fastest, best for simple tasks ===
        UnifiedAIModel(
            providerType = ProviderType.ZAI,
            modelId = "glm-4.5-air",
            displayName = "🟢 GLM-4.5-Air",
            description = "Fastest, best for simple tasks",
            maxTokens = 98304,
            inputCostPerMillion = 0.2,
            outputCostPerMillion = 1.1,
            tier = ModelTier.JUNIOR
        ),

        // === 🆓 FREE MODELS ===
        UnifiedAIModel(
            providerType = ProviderType.ZAI,
            modelId = "glm-4.7-flash",
            displayName = "🆓 GLM-4.7-Flash",
            description = "Free model - limited time offer",
            maxTokens = 131072,
            inputCostPerMillion = 0.0,
            outputCostPerMillion = 0.0,
            tier = ModelTier.MIDDLE
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
