package ru.assistant.aicwl.chat.provider

import ru.assistant.aicwl.chat.data.unified.UnifiedChatRequest
import ru.assistant.aicwl.chat.data.unified.UnifiedChatResponse
import ru.assistant.aicwl.chat.provider.model.UnifiedAIModel

/**
 * Abstract interface for AI provider implementations.
 * Follows Strategy Pattern - each provider implements the same interface.
 *
 * This abstraction allows the application to switch between AI providers
 * without changing business logic or UI code.
 *
 * Clean Architecture compliance:
 * - This interface lives in the Domain layer
 * - Implementations are in the Data layer
 * - No dependencies on UI or framework-specific code
 */
interface AIProvider {

    /**
     * Get the provider type.
     */
    fun getProviderType(): ProviderType

    /**
     * Get human-readable provider name.
     */
    fun getProviderName(): String

    /**
     * Get all available models for this provider.
     */
    fun getAvailableModels(): List<UnifiedAIModel>

    /**
     * Get the default model for this provider.
     */
    fun getDefaultModel(): UnifiedAIModel

    /**
     * Check if the provider is properly configured (API key set).
     */
    fun isConfigured(): Boolean

    /**
     * Send a chat completion request.
     *
     * @param request Unified request containing messages and parameters
     * @return Result containing response or error
     *
     * Implementations must handle:
     * - Converting UnifiedChatRequest to provider-specific format
     * - Making HTTP request to provider API
     * - Converting response back to UnifiedChatResponse
     * - Error handling and retry logic
     */
    suspend fun sendChatRequest(request: UnifiedChatRequest): Result<UnifiedChatResponse>

    /**
     * Validate that a model ID is supported by this provider.
     */
    fun isValidModel(modelId: String): Boolean {
        return getAvailableModels().any { it.modelId == modelId }
    }

    /**
     * Get the API endpoint for debugging/logging (should mask sensitive parts).
     */
    fun getEndpointInfo(): String

    /**
     * Close the provider and release resources (HTTP client, connections, etc.).
     * Should be called when the provider is no longer needed.
     */
    fun close() {
        // Default implementation does nothing
        // Providers with resources to clean up should override this
    }
}
