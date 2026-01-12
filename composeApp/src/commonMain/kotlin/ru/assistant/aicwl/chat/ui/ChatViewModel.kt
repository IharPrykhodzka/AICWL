package ru.assistant.aicwl.chat.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.assistant.aicwl.chat.agent.chatAgent
import ru.assistant.aicwl.chat.config.ModelConfig
import ru.assistant.aicwl.chat.data.MessageRole
import ru.assistant.aicwl.chat.data.UiChatMessage
import ru.assistant.aicwl.chat.utils.currentTimeMillis
import ru.assistant.aicwl.chat.utils.createLogger

/**
 * ViewModel for managing chat state and interactions.
 */
class ChatViewModel : ViewModel() {
    private val logger = createLogger("ChatViewModel")

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    /**
     * Update the selected AI model.
     */
    fun selectModel(modelId: String) {
        logger.i("Model selected: $modelId (display: ${ModelConfig.getDisplayName(modelId)})")
        _uiState.value = _uiState.value.copy(selectedModel = modelId)
    }

    /**
     * Update the user's input text.
     */
    fun updateInputText(text: String) {
        _uiState.value = _uiState.value.copy(inputText = text)
    }

    /**
     * Send the user's message and get AI response.
     */
    fun sendMessage() {
        val currentInput = _uiState.value.inputText.trim()
        if (currentInput.isEmpty()) {
            logger.w("Attempted to send empty message")
            return
        }

        logger.i("Sending message. Model: ${_uiState.value.selectedModel}")
        logger.d("Message: ${currentInput.take(100)}...")

        val now = currentTimeMillis()
        val userMessage = UiChatMessage(
            id = generateId(),
            role = MessageRole.USER,
            content = currentInput,
            timestamp = now
        )

        // Add user message
        _uiState.value = _uiState.value.copy(
            messages = _uiState.value.messages + userMessage,
            inputText = "",
            isLoading = true
        )

        logger.d("User message added. Total messages: ${_uiState.value.messages.size + 1}")

        // Get AI response
        viewModelScope.launch {
            try {
                logger.d("Starting AI request...")
                val response = chatAgent.chat(
                    message = currentInput,
                    modelId = _uiState.value.selectedModel
                )

                logger.i("AI response received. Length: ${response.length}")

                val assistantMessage = UiChatMessage(
                    id = generateId(),
                    role = MessageRole.ASSISTANT,
                    content = response,
                    timestamp = currentTimeMillis()
                )

                _uiState.value = _uiState.value.copy(
                    messages = _uiState.value.messages + assistantMessage,
                    isLoading = false
                )

                logger.d("Assistant message added. Total messages: ${_uiState.value.messages.size}")
            } catch (e: Exception) {
                logger.e("Failed to get AI response", e)

                val errorMessage = UiChatMessage(
                    id = generateId(),
                    role = MessageRole.ASSISTANT,
                    content = "Error: ${e.message}",
                    timestamp = currentTimeMillis()
                )

                _uiState.value = _uiState.value.copy(
                    messages = _uiState.value.messages + errorMessage,
                    isLoading = false
                )
            }
        }
    }

    /**
     * Clear all messages from the chat.
     */
    fun clearChat() {
        logger.i("Clearing chat. Previous messages count: ${_uiState.value.messages.size}")
        _uiState.value = _uiState.value.copy(messages = emptyList())
    }

    private fun generateId(): String = "${currentTimeMillis()}-${(0..999).random()}"

    /**
     * UI State for the chat screen.
     */
    data class ChatUiState(
        val messages: List<UiChatMessage> = emptyList(),
        val inputText: String = "",
        val selectedModel: String = ModelConfig.DEFAULT_MODEL,
        val isLoading: Boolean = false,
        val isModelSelectorExpanded: Boolean = false
    )
}
