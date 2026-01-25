package ru.assistant.aicwl.chat.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * Custom ViewModel factory for Android.
 * Provides ChatViewModel with ChatHistoryRepository and TokenTracker dependencies.
 */
class ChatViewModelFactory(
    private val chatHistoryRepository: ru.assistant.aicwl.chat.data.ChatHistoryRepository,
    private val tokenTracker: ru.assistant.aicwl.chat.tokens.TokenTracker? = null
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChatViewModel::class.java)) {
            return ChatViewModel(chatHistoryRepository, tokenTracker) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

private var factory: ChatViewModelFactory? = null

/**
 * Initialize the ChatViewModel factory with dependencies.
 * Must be called before using chatViewModel().
 */
fun initializeChatViewModelFactory(
    chatHistoryRepository: ru.assistant.aicwl.chat.data.ChatHistoryRepository,
    tokenTracker: ru.assistant.aicwl.chat.tokens.TokenTracker? = null
) {
    factory = ChatViewModelFactory(chatHistoryRepository, tokenTracker)
}

@Composable
actual fun chatViewModel(): ChatViewModel {
    return viewModel(
        factory = factory ?: throw IllegalStateException(
            "ChatViewModelFactory not initialized. Call initializeChatViewModelFactory() first."
        )
    )
}
