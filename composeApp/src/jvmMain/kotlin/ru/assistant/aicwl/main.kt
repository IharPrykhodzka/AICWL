package ru.assistant.aicwl

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import ru.assistant.aicwl.chat.prompt.ui.PromptSettingsViewModelFactory
import ru.assistant.aicwl.chat.prompt.data.PromptPreferences
import ru.assistant.aicwl.chat.prompt.SystemPromptConfig

fun main() = application {
    // Initialize PromptSettingsFactory for JVM/Desktop
    PromptSettingsViewModelFactory.initialize(
        defaultPrompt = SystemPromptConfig.mainPrompt,
        preferences = PromptPreferences()
    )

    Window(
        onCloseRequest = ::exitApplication,
        title = "AI Chat Agent"
    ) {
        App()
    }
}
