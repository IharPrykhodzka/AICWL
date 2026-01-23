package ru.assistant.aicwl.chat.config

import ru.assistant.aicwl.BuildConfig

actual object AppConfig {
    // Z.ai Configuration
    actual val zApiKey: String = BuildConfig.LLM_API_KEY
    actual val zApiEndpoint: String = "https://api.z.ai/api/coding/paas/v4/chat/completions"

    // OpenAI Configuration
    actual val openaiApiKey: String = BuildConfig.OPENAI_API_KEY
    actual val openaiApiEndpoint: String = "https://api.openai.com/v1/chat/completions"

    // Anthropic Configuration
    actual val anthropicApiKey: String = BuildConfig.ANTHROPIC_API_KEY
    actual val anthropicApiEndpoint: String = "https://api.anthropic.com/v1/messages"
}
