package ru.assistant.aicwl.chat.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

private var chatHistoryRepository: ru.assistant.aicwl.chat.data.ChatHistoryRepository? = null

/**
 * Initialize the ChatViewModel with dependencies.
 * Must be called before using chatViewModel().
 */
fun initializeChatViewModel(repository: ru.assistant.aicwl.chat.data.ChatHistoryRepository) {
    chatHistoryRepository = repository
}

@Composable
actual fun chatViewModel(): ChatViewModel {
    val repository = chatHistoryRepository ?: throw IllegalStateException(
        "ChatHistoryRepository not initialized. Call initializeChatViewModel() first."
    )
    return remember { ChatViewModel(repository) }
}
