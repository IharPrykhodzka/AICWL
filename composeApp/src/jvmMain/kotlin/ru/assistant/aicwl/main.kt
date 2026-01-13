package ru.assistant.aicwl

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "AI Chat Agent"
    ) {
        App()
    }
}
