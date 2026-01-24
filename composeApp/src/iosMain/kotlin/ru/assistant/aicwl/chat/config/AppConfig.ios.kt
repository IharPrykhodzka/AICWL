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

    // ============== QWEN CONFIGURATION ==============
    actual val qwenApiKey: String by lazy {
        NSUserDefaults.standardUserDefaults.stringForKey("LLM_QWEN_API_KEY")
            ?: NSBundle.mainBundle.objectForInfoDictionaryKey("LLM_QWEN_API_KEY") as? String
            ?: NSBundle.mainBundle.objectForInfoDictionaryKey("QWENApiKey") as? String
            ?: ""
    }

    actual val qwenApiEndpoint: String by lazy {
        NSBundle.mainBundle.objectForInfoDictionaryKey("LLM_QWEN_API_ENDPOINT") as? String
            ?: "https://router.huggingface.co/v1/chat/completions"
    }

    // ============== OREAL CONFIGURATION ==============
    actual val orealApiKey: String by lazy {
        NSUserDefaults.standardUserDefaults.stringForKey("LLM_OREAL_API_KEY")
            ?: NSBundle.mainBundle.objectForInfoDictionaryKey("LLM_OREAL_API_KEY") as? String
            ?: NSBundle.mainBundle.objectForInfoDictionaryKey("OREALApiKey") as? String
            ?: ""
    }

    actual val orealApiEndpoint: String by lazy {
        NSBundle.mainBundle.objectForInfoDictionaryKey("LLM_OREAL_API_ENDPOINT") as? String
            ?: "https://router.huggingface.co/v1/chat/completions"
    }
}