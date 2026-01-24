package ru.assistant.aicwl.chat.provider

import ru.assistant.aicwl.chat.config.AppConfig
import ru.assistant.aicwl.chat.provider.oreal.OrealProvider
import ru.assistant.aicwl.chat.provider.qwen.QwenProvider
import ru.assistant.aicwl.chat.provider.zai.ZAIProvider
import ru.assistant.aicwl.chat.utils.createLogger

/**
 * Factory for creating AI provider instances.
 * Follows Factory Pattern to encapsulate provider creation logic.
 *
 * This factory is the single point for obtaining provider instances,
 * ensuring proper initialization and configuration.
 *
 * Lifecycle management:
 * - Providers are cached and reused for efficiency
 * - Call cleanup() when shutting down the application
 */
object AIProviderFactory {

    private val logger = createLogger("AIProviderFactory")

    // Cache for provider instances
    private val providerCache = mutableMapOf<ProviderType, AIProvider>()

    /**
     * Create or get cached provider instance for the specified type.
     *
     * @param providerType The type of provider to create
     * @return Configured AIProvider instance
     * @throws IllegalArgumentException if provider type is not supported
     */
    fun createProvider(providerType: ProviderType): AIProvider {
        return providerCache.getOrPut(providerType) {
            logger.d("Creating new provider instance: $providerType")
            when (providerType) {
                ProviderType.ZAI -> createZAIProvider()
                ProviderType.QWEN -> createQwenProvider()
                ProviderType.OREAL -> createOrealProvider()
            }
        }
    }

    /**
     * Create Z.ai provider instance.
     */
    private fun createZAIProvider(): ZAIProvider {
        return ZAIProvider(
            apiKey = AppConfig.zApiKey,
            endpoint = AppConfig.zApiEndpoint
        )
    }

    /**
     * Create Qwen provider instance.
     */
    private fun createQwenProvider(): QwenProvider {
        return QwenProvider(
            apiKey = AppConfig.qwenApiKey,
            endpoint = AppConfig.qwenApiEndpoint
        )
    }

    /**
     * Create Oreal provider instance.
     */
    private fun createOrealProvider(): OrealProvider {
        return OrealProvider(
            apiKey = AppConfig.orealApiKey,
            endpoint = AppConfig.orealApiEndpoint
        )
    }

    /**
     * Get all supported provider types.
     */
    fun getSupportedProviders(): List<ProviderType> {
        return ProviderType.entries.toList()
    }

    /**
     * Check if a provider is configured (has valid API key).
     */
    fun isProviderConfigured(providerType: ProviderType): Boolean {
        return try {
            createProvider(providerType).isConfigured()
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Get all configured providers.
     */
    fun getConfiguredProviders(): List<ProviderType> {
        return getSupportedProviders().filter { isProviderConfigured(it) }
    }

    /**
     * Clear the provider cache and close all cached providers.
     * Call this when shutting down the application.
     */
    fun cleanup() {
        logger.i("Cleaning up ${providerCache.size} provider(s)")
        providerCache.values.forEach { provider ->
            try {
                provider.close()
            } catch (e: Exception) {
                logger.w("Error closing provider: ${provider.getProviderType()}", e)
            }
        }
        providerCache.clear()
    }

    /**
     * Reset the factory (clear cache and create new instances on next request).
     * Useful for testing or when configuration changes.
     */
    fun reset() {
        logger.i("Resetting provider factory")
        cleanup()
    }
}
