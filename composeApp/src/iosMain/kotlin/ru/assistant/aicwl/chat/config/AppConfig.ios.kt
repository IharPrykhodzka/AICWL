package ru.assistant.aicwl.chat.config

import platform.Foundation.NSBundle
import platform.Foundation.NSUserDefaults

actual object AppConfig {
    actual val zApiKey: String by lazy {
        // Пытаемся получить API-ключ из нескольких источников (в порядке приоритета):
        // 1. Локальный файл (local_config.txt в бандле) - самый безопасный для git
        // 2. UserDefaults (может быть установлен программно)
        // 3. Info.plist

        LocalConfigLoader.loadApiKeyFromFile()
            ?: NSUserDefaults.standardUserDefaults.stringForKey("LLM_Z_API_KEY")
            ?: NSBundle.mainBundle.objectForInfoDictionaryKey("LLM_Z_API_KEY") as? String
            ?: NSBundle.mainBundle.objectForInfoDictionaryKey("ZAIApiKey") as? String
            ?: error(
                "API Key not found!\n\n" +
                "Please set your API key in one of these ways:\n" +
                "1. Create iosApp/iosApp/local_config.txt with your key (add to Xcode, NOT to git)\n" +
                "2. Set programmatically: ApiKeyHelper.setApiKey(\"your_key\")\n" +
                "3. Add to Info.plist: <key>LLM_Z_API_KEY</key><string>your_key</string>\n"
            )
    }

    actual val zApiEndpoint: String by lazy {
        NSBundle.mainBundle.objectForInfoDictionaryKey("LLM_Z_API_ENDPOINT") as? String
            ?: "https://api.z.ai/api/coding/paas/v4/chat/completions"
    }
}
