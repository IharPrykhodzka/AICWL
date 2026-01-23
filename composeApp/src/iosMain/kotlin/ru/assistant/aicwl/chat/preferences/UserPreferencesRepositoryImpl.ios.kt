package ru.assistant.aicwl.chat.preferences

import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import platform.Foundation.NSUserDefaults
import ru.assistant.aicwl.chat.provider.ProviderType
import ru.assistant.aicwl.chat.provider.model.AIModelConfig
import ru.assistant.aicwl.chat.provider.model.UnifiedAIModel

/**
 * iOS implementation of UserPreferencesRepository using NSUserDefaults.
 */
actual class UserPreferencesRepositoryImpl actual constructor() : UserPreferencesRepository {

    private val userDefaults: NSUserDefaults = NSUserDefaults.standardUserDefaults

    override fun getSelectedProviderFlow(): Flow<ProviderType> = callbackFlow {
        // Note: NSUserDefaults doesn't provide a built-in observation mechanism in Kotlin/Native
        // We'll use a simple polling approach or manual notification
        // For a production app, you might want to use Darwin notifications

        trySend(getSelectedProviderSync())

        awaitClose {
            // Cleanup if needed
        }
    }.distinctUntilChanged()

    override suspend fun getSelectedProvider(): ProviderType {
        return getSelectedProviderSync()
    }

    private fun getSelectedProviderSync(): ProviderType {
        val providerName = userDefaults.stringForKey(KEY_SELECTED_PROVIDER)
        return ProviderType.entries.find { it.name == providerName } ?: ProviderType.DEFAULT
    }

    override suspend fun setSelectedProvider(provider: ProviderType) {
        userDefaults.setObject(provider.name, KEY_SELECTED_PROVIDER)
        userDefaults.synchronize()
    }

    override suspend fun getSelectedModel(provider: ProviderType): String? {
        val key = "${KEY_SELECTED_MODEL}_${provider.name}"
        return userDefaults.stringForKey(key)
    }

    override suspend fun setSelectedModel(provider: ProviderType, modelId: String) {
        val key = "${KEY_SELECTED_MODEL}_${provider.name}"
        userDefaults.setObject(modelId, key)
        userDefaults.synchronize()
    }

    override suspend fun getDefaultModel(): UnifiedAIModel {
        val provider = getSelectedProvider()
        val modelId = getSelectedModel(provider)
        return if (modelId != null) {
            AIModelConfig.getModelsByProvider(provider)
                .find { it.modelId == modelId }
                ?: AIModelConfig.getDefaultModelForProvider(provider)
        } else {
            AIModelConfig.getDefaultModelForProvider(provider)
        }
    }

    private companion object {
        const val KEY_SELECTED_PROVIDER = "selected_provider"
        const val KEY_SELECTED_MODEL = "selected_model"
    }
}
