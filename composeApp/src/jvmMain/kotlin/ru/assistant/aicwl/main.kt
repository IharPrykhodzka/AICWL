package ru.assistant.aicwl

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import ru.assistant.aicwl.chat.prompt.ui.PromptSettingsViewModelFactory
import ru.assistant.aicwl.chat.prompt.data.PromptPreferences
import ru.assistant.aicwl.chat.data.ChatHistoryPreferences
import ru.assistant.aicwl.chat.ui.initializeChatViewModel
import ru.assistant.aicwl.chat.prompt.SystemPromptConfig
import ru.assistant.aicwl.chat.utils.LoggingConfig
import ru.assistant.aicwl.chat.tokens.TokenStorage
import ru.assistant.aicwl.chat.tokens.initializeTokenTracker
import ru.assistant.aicwl.chat.tokens.getTokenTracker
import ru.assistant.aicwl.chat.agent.initializeChatAgent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

fun main() = application {
    // Включаем debug логирование для Desktop
    LoggingConfig.enableDebug()
    // Initialize preferences
    val preferences = PromptPreferences()
    val chatHistoryPrefs = ChatHistoryPreferences()

    // Initialize TokenStorage and TokenTracker
    val tokenStorage = TokenStorage()
    initializeTokenTracker(tokenStorage)

    // Initialize ChatAgent with TokenTracker for token tracking
    val tokenTracker = getTokenTracker(tokenStorage)
    initializeChatAgent(tokenTracker)

    // Initialize PromptSettingsFactory for JVM/Desktop
    PromptSettingsViewModelFactory.initialize(
        defaultPrompt = SystemPromptConfig.mainPrompt,
        preferences = preferences,
        chatHistoryPrefs = chatHistoryPrefs
    )

    // Initialize ChatViewModel
    initializeChatViewModel(
        repository = PromptSettingsViewModelFactory.getChatHistoryRepository(),
        tracker = tokenTracker
    )

    Window(
        onCloseRequest = ::exitApplication,
        title = "AI Chat Agent"
    ) {
        App()
    }
}
