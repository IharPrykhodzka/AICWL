package ru.assistant.aicwl

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import ru.assistant.aicwl.chat.utils.initAppContext
import ru.assistant.aicwl.chat.prompt.ui.PromptSettingsViewModelFactory
import ru.assistant.aicwl.chat.prompt.data.PromptPreferences
import ru.assistant.aicwl.chat.data.ChatHistoryPreferences
import ru.assistant.aicwl.chat.ui.initializeChatViewModelFactory
import ru.assistant.aicwl.chat.prompt.SystemPromptConfig
import ru.assistant.aicwl.chat.tokens.TokenStorage
import ru.assistant.aicwl.chat.tokens.initializeTokenTracker
import ru.assistant.aicwl.chat.tokens.getTokenTracker
import ru.assistant.aicwl.chat.agent.initializeChatAgent

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Инициализируем контекст для буфера обмена
        initAppContext(applicationContext)

        // Инициализируем preferences для настроек промпта
        val preferences = PromptPreferences()
        preferences.initialize(applicationContext)

        // Инициализируем preferences для истории чата
        val chatHistoryPrefs = ChatHistoryPreferences()
        chatHistoryPrefs.initialize(applicationContext)

        // Инициализируем фабрику настроек промпта
        PromptSettingsViewModelFactory.initialize(
            defaultPrompt = SystemPromptConfig.mainPrompt,
            preferences = preferences,
            chatHistoryPrefs = chatHistoryPrefs
        )

        // Initialize TokenStorage and TokenTracker
        val tokenStorage = TokenStorage()
        tokenStorage.initialize(applicationContext)
        initializeTokenTracker(tokenStorage)
        val tokenTracker = getTokenTracker(tokenStorage)

        // Initialize ChatAgent with TokenTracker
        initializeChatAgent(tokenTracker)

        // Инициализируем фабрику ChatViewModel
        initializeChatViewModelFactory(
            chatHistoryRepository = PromptSettingsViewModelFactory.getChatHistoryRepository(),
            tokenTracker = tokenTracker
        )

        setContent {
            App()
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}