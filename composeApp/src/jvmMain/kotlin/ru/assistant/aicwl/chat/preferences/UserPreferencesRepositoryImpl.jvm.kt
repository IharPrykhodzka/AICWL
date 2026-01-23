package ru.assistant.aicwl.chat.preferences

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import ru.assistant.aicwl.chat.provider.ProviderType
import ru.assistant.aicwl.chat.provider.model.AIModelConfig
import ru.assistant.aicwl.chat.provider.model.UnifiedAIModel
import java.io.File
import java.util.Properties

/**
 * JVM implementation of UserPreferencesRepository using file-based storage.
 * Preferences are stored in a .properties file in the user's home directory.
 */
actual class UserPreferencesRepositoryImpl actual constructor() : UserPreferencesRepository {

    private val preferencesFile: File by lazy {
        val userHome = System.getProperty("user.home")
        File(userHome, ".aicwl/preferences.properties").apply {
            parentFile?.mkdirs()
        }
    }

    private val properties: Properties by lazy {
        loadProperties()
    }

    private val _selectedProviderFlow = MutableStateFlow(loadSelectedProvider())
    private var propertiesModified = false

    override fun getSelectedProviderFlow(): Flow<ProviderType> = _selectedProviderFlow

    override suspend fun getSelectedProvider(): ProviderType {
        return loadSelectedProvider()
    }

    private fun loadSelectedProvider(): ProviderType {
        val providerName = properties.getProperty(KEY_SELECTED_PROVIDER)
        return ProviderType.entries.find { it.name == providerName } ?: ProviderType.DEFAULT
    }

    override suspend fun setSelectedProvider(provider: ProviderType) {
        properties.setProperty(KEY_SELECTED_PROVIDER, provider.name)
        propertiesModified = true
        _selectedProviderFlow.value = provider
    }

    override suspend fun getSelectedModel(provider: ProviderType): String? {
        val key = "${KEY_SELECTED_MODEL}_${provider.name}"
        return properties.getProperty(key)
    }

    override suspend fun setSelectedModel(provider: ProviderType, modelId: String) {
        val key = "${KEY_SELECTED_MODEL}_${provider.name}"
        properties.setProperty(key, modelId)
        propertiesModified = true
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

    private fun loadProperties(): Properties {
        val props = Properties()
        if (preferencesFile.exists()) {
            try {
                preferencesFile.inputStream().use { props.load(it) }
            } catch (e: Exception) {
                System.err.println("[UserPreferencesRepository] Failed to load properties: ${e.message}")
            }
        }
        return props
    }

    /**
     * Save properties to file. Call this when the application shuts down.
     */
    fun saveProperties() {
        if (propertiesModified) {
            try {
                preferencesFile.outputStream().use { properties.store(it, "AICWL User Preferences") }
                propertiesModified = false
            } catch (e: Exception) {
                System.err.println("[UserPreferencesRepository] Failed to save properties: ${e.message}")
            }
        }
    }

    private companion object {
        const val KEY_SELECTED_PROVIDER = "selected_provider"
        const val KEY_SELECTED_MODEL = "selected_model"
    }
}
