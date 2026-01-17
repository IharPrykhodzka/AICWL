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
import ru.assistant.aicwl.chat.prompt.SystemPromptConfig

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Инициализируем контекст для буфера обмена
        initAppContext(applicationContext)

        // Инициализируем preferences для настроек промпта
        val preferences = PromptPreferences()
        preferences.initialize(applicationContext)

        // Инициализируем фабрику настроек промпта
        PromptSettingsViewModelFactory.initialize(
            defaultPrompt = SystemPromptConfig.mainPrompt,
            preferences = preferences
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