package ru.assistant.aicwl.chat.config

import ru.assistant.aicwl.BuildConfig

actual object AppConfig {
    actual val zApiKey: String = BuildConfig.LLM_API_KEY

    actual val zApiEndpoint: String = "https://api.z.ai/api/coding/paas/v4/chat/completions"
}
