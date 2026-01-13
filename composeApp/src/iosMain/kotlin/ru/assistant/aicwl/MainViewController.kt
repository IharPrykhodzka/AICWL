package ru.assistant.aicwl

import androidx.compose.ui.window.ComposeUIViewController
import ru.assistant.aicwl.chat.config.ApiKeyHelper

fun MainViewController() = ComposeUIViewController {
    // Установите ваш API-ключ здесь для тестирования или используйте Info.plist
    // ApiKeyHelper.setApiKey("your-actual-api-key-here")
    App()
}