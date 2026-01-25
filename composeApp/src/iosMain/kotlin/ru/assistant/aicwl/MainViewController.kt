package ru.assistant.aicwl

import androidx.compose.ui.window.ComposeUIViewController
import ru.assistant.aicwl.chat.config.ApiKeyHelper
import ru.assistant.aicwl.chat.prompt.ui.PromptSettingsViewModelFactory
import ru.assistant.aicwl.chat.prompt.data.PromptPreferences
import ru.assistant.aicwl.chat.data.ChatHistoryPreferences
import ru.assistant.aicwl.chat.ui.initializeChatViewModel
import ru.assistant.aicwl.chat.prompt.SystemPromptConfig
import ru.assistant.aicwl.chat.tokens.TokenStorage
import ru.assistant.aicwl.chat.tokens.initializeTokenTracker
import ru.assistant.aicwl.chat.tokens.getTokenTracker
import ru.assistant.aicwl.chat.agent.initializeChatAgent

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

    // Initialize TokenStorage and TokenTracker
    val tokenStorage = TokenStorage()
    initializeTokenTracker(tokenStorage)
    val tokenTracker = getTokenTracker(tokenStorage)

    // Initialize ChatAgent with TokenTracker
    initializeChatAgent(tokenTracker)

    // Initialize ChatViewModel
    initializeChatViewModel(
        repository = PromptSettingsViewModelFactory.getChatHistoryRepository(),
        tracker = tokenTracker
    )

    // Установите ваш API-ключ здесь для тестирования или используйте Info.plist
    // ApiKeyHelper.setApiKey("your-actual-api-key-here")
    App()
}