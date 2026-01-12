package ru.assistant.aicwl

import androidx.compose.ui.window.ComposeUIViewController
import ru.assistant.aicwl.chat.config.ApiKeyHelper

fun MainViewController() = ComposeUIViewController {
    // Set your API key here for testing, or use Info.plist
    // ApiKeyHelper.setApiKey("your-actual-api-key-here")
    App()
}