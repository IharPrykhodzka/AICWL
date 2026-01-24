package ru.assistant.aicwl.chat.config

import ru.assistant.aicwl.BuildConfig

actual object AppConfig {
    // Z.ai Configuration
    actual val zApiKey: String = BuildConfig.LLM_API_KEY
    actual val zApiEndpoint: String = "https://api.z.ai/api/coding/paas/v4/chat/completions"

    // Qwen Configuration
    actual val qwenApiKey: String = ""
    actual val qwenApiEndpoint: String = "https://router.huggingface.co/v1/chat/completions"

    // Oreal Configuration
    actual val orealApiKey: String = ""
    actual val orealApiEndpoint: String = "https://router.huggingface.co/v1/chat/completions"
}
