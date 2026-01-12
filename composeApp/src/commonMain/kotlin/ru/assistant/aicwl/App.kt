package ru.assistant.aicwl

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import org.jetbrains.compose.ui.tooling.preview.Preview
import ru.assistant.aicwl.chat.ui.ChatScreen

/**
 * Main application entry point.
 * Displays the ChatScreen with AI agent integration.
 */
@Composable
@Preview
fun App() {
    MaterialTheme {
        ChatScreen()
    }
}
