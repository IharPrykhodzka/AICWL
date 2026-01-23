package ru.assistant.aicwl.chat.config

import platform.Foundation.NSBundle
import platform.Foundation.NSUserDefaults

actual object AppConfig {
    // ============== Z.AI CONFIGURATION ==============
    actual val zApiKey: String by lazy {
        LocalConfigLoader.loadApiKeyFromFile()
            ?: NSUserDefaults.standardUserDefaults.stringForKey("LLM_Z_API_KEY")
            ?: NSBundle.mainBundle.objectForInfoDictionaryKey("LLM_Z_API_KEY") as? String
            ?: NSBundle.mainBundle.objectForInfoDictionaryKey("ZAIApiKey") as? String
            ?: ""
    }

    actual val zApiEndpoint: String by lazy {
        NSBundle.mainBundle.objectForInfoDictionaryKey("LLM_Z_API_ENDPOINT") as? String
            ?: "https://api.z.ai/api/coding/paas/v4/chat/completions"
    }

    // ============== OPENAI CONFIGURATION ==============
    actual val openaiApiKey: String by lazy {
        LocalConfigLoader.loadOpenAiApiKey()
            ?: NSUserDefaults.standardUserDefaults.stringForKey("OPENAI_API_KEY")
            ?: NSBundle.mainBundle.objectForInfoDictionaryKey("OPENAI_API_KEY") as? String
            ?: ""
    }

    actual val openaiApiEndpoint: String by lazy {
        NSBundle.mainBundle.objectForInfoDictionaryKey("OPENAI_API_ENDPOINT") as? String
            ?: "https://api.openai.com/v1/chat/completions"
    }

    // ============== ANTHROPIC CONFIGURATION ==============
    actual val anthropicApiKey: String by lazy {
        LocalConfigLoader.loadAnthropicApiKey()
            ?: NSUserDefaults.standardUserDefaults.stringForKey("ANTHROPIC_API_KEY")
            ?: NSBundle.mainBundle.objectForInfoDictionaryKey("ANTHROPIC_API_KEY") as? String
            ?: ""
    }

    actual val anthropicApiEndpoint: String by lazy {
        NSBundle.mainBundle.objectForInfoDictionaryKey("ANTHROPIC_API_ENDPOINT") as? String
            ?: "https://api.anthropic.com/v1/messages"
    }
}
