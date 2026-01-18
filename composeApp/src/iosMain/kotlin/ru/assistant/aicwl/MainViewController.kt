package ru.assistant.aicwl

import androidx.compose.ui.window.ComposeUIViewController
import ru.assistant.aicwl.chat.config.ApiKeyHelper
import ru.assistant.aicwl.chat.prompt.ui.PromptSettingsViewModelFactory
import ru.assistant.aicwl.chat.prompt.data.PromptPreferences
import ru.assistant.aicwl.chat.data.ChatHistoryPreferences
import ru.assistant.aicwl.chat.ui.initializeChatViewModel
import ru.assistant.aicwl.chat.prompt.SystemPromptConfig

fun MainViewController() = ComposeUIViewController {
    // Initialize preferences for iOS
    val preferences = PromptPreferences()
    val chatHistoryPrefs = ChatHistoryPreferences()

    // Initialize PromptSettingsFactory for iOS
    PromptSettingsViewModelFactory.initialize(
        defaultPrompt = SystemPromptConfig.mainPrompt,
        preferences = preferences,
        chatHistoryPrefs = chatHistoryPrefs
    )

    // Initialize ChatViewModel
    initializeChatViewModel(
        PromptSettingsViewModelFactory.getChatHistoryRepository()
    )

    // Установите ваш API-ключ здесь для тестирования или используйте Info.plist
    // ApiKeyHelper.setApiKey("your-actual-api-key-here")
    App()
}