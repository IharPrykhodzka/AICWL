package ru.assistant.aicwl

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import org.jetbrains.compose.ui.tooling.preview.Preview
import ru.assistant.aicwl.chat.ui.ChatScreen

/**
 * Главная точка входа в приложение.
 * Отображает ChatScreen с интеграцией AI-агента.
 */
@Composable
@Preview
fun App() {
    MaterialTheme {
        ChatScreen()
    }
}
