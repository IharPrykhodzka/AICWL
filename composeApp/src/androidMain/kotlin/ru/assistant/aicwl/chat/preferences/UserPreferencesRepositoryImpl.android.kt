package ru.assistant.aicwl.chat.preferences

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import ru.assistant.aicwl.chat.provider.ProviderType
import ru.assistant.aicwl.chat.provider.model.AIModelConfig
import ru.assistant.aicwl.chat.provider.model.UnifiedAIModel

/**
 * Android implementation of UserPreferencesRepository using SharedPreferences.
 * Constructor without parameters uses application context via reflection.
 */
actual class UserPreferencesRepositoryImpl actual constructor() : UserPreferencesRepository {

    private val context: Context by lazy {
        getContextFromApplication()
    }

    private val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private fun getContextFromApplication(): Context {
        // Get application context via reflection
        try {
            val appContextClass = Class.forName("android.app.AppGlobals")
            val method = appContextClass.getDeclaredMethod("getInitialApplication")
            @Suppress("UNCHECKED_CAST")
            val application = method.invoke(null) as? android.app.Application
                ?: throw IllegalStateException("Application context not available")
            return application.applicationContext
        } catch (e: Exception) {
            throw IllegalStateException("Cannot get application context.", e)
        }
    }

    override fun getSelectedProviderFlow(): Flow<ProviderType> = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == KEY_SELECTED_PROVIDER) {
                trySend(getSelectedProviderSync())
            }
        }

        sharedPreferences.registerOnSharedPreferenceChangeListener(listener)
        trySend(getSelectedProviderSync())

        awaitClose {
            sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }.distinctUntilChanged()

    override suspend fun getSelectedProvider(): ProviderType {
        return getSelectedProviderSync()
    }

    private fun getSelectedProviderSync(): ProviderType {
        val providerName = sharedPreferences.getString(KEY_SELECTED_PROVIDER, null)
        return ProviderType.entries.find { it.name == providerName } ?: ProviderType.DEFAULT
    }

    override suspend fun setSelectedProvider(provider: ProviderType) {
        sharedPreferences.edit().putString(KEY_SELECTED_PROVIDER, provider.name).apply()
    }

    override suspend fun getSelectedModel(provider: ProviderType): String? {
        val key = "${KEY_SELECTED_MODEL}_${provider.name}"
        return sharedPreferences.getString(key, null)
    }

    override suspend fun setSelectedModel(provider: ProviderType, modelId: String) {
        val key = "${KEY_SELECTED_MODEL}_${provider.name}"
        sharedPreferences.edit().putString(key, modelId).apply()
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
        const val PREFS_NAME = "aicwl_preferences"
        const val KEY_SELECTED_PROVIDER = "selected_provider"
        const val KEY_SELECTED_MODEL = "selected_model"
    }
}
