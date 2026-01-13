package ru.assistant.aicwl.chat.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.assistant.aicwl.chat.agent.chatAgent
import ru.assistant.aicwl.chat.config.ModelConfig
import ru.assistant.aicwl.chat.data.EnhancedChatMessage
import ru.assistant.aicwl.chat.data.MessageRole
import ru.assistant.aicwl.chat.data.UiChatMessage
import ru.assistant.aicwl.chat.utils.currentTimeMillis
import ru.assistant.aicwl.chat.utils.createLogger

/**
 * ViewModel для управления состоянием чата и взаимодействиями.
 * Поддерживает структурированные ответы от AI.
 */
class ChatViewModel : ViewModel() {
    private val logger = createLogger("ChatViewModel")

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    /**
     * Обновляет выбранную AI-модель.
     */
    fun selectModel(modelId: String) {
        logger.i("Model selected: $modelId (display: ${ModelConfig.getDisplayName(modelId)})")
        _uiState.value = _uiState.value.copy(selectedModel = modelId)
    }

    /**
     * Обновляет текст ввода пользователя.
     */
    fun updateInputText(text: String) {
        _uiState.value = _uiState.value.copy(inputText = text)
    }

    /**
     * Отправляет сообщение пользователя и получает ответ AI.
     * Автоматически определяет тип ответа (структурированный или обычный).
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
        val userMessage = EnhancedChatMessage(
            id = generateId(),
            role = MessageRole.USER,
            originalContent = currentInput,
            timestamp = now,
            messageType = ru.assistant.aicwl.chat.data.MessageType.PLAIN_TEXT
        )

        // Добавляем сообщение пользователя
        _uiState.value = _uiState.value.copy(
            enhancedMessages = _uiState.value.enhancedMessages + userMessage,
            inputText = "",
            isLoading = true
        )

        logger.d("User message added. Total messages: ${_uiState.value.enhancedMessages.size + 1}")

        // Получаем ответ AI
        viewModelScope.launch {
            try {
                logger.d("Starting AI request...")
                val response = chatAgent.chat(
                    message = currentInput,
                    modelId = _uiState.value.selectedModel
                )

                logger.i("AI response received. Length: ${response.length}")
                logger.d("Response preview: ${response.take(200)}...")

                // Создаём enhanced сообщение с автоматическим определением типа
                val assistantMessage = EnhancedChatMessage.fromAiResponse(
                    id = generateId(),
                    content = response,
                    timestamp = currentTimeMillis()
                )

                logger.d("Message type: ${assistantMessage.messageType}")
                logger.d("Is structured: ${assistantMessage.structuredData != null}")

                _uiState.value = _uiState.value.copy(
                    enhancedMessages = _uiState.value.enhancedMessages + assistantMessage,
                    isLoading = false
                )

                logger.d("Assistant message added. Total messages: ${_uiState.value.enhancedMessages.size}")
            } catch (e: Exception) {
                logger.e("Failed to get AI response", e)

                val errorMessage = EnhancedChatMessage(
                    id = generateId(),
                    role = MessageRole.ASSISTANT,
                    originalContent = "Error: ${e.message}",
                    timestamp = currentTimeMillis(),
                    messageType = ru.assistant.aicwl.chat.data.MessageType.ERROR
                )

                _uiState.value = _uiState.value.copy(
                    enhancedMessages = _uiState.value.enhancedMessages + errorMessage,
                    isLoading = false
                )
            }
        }
    }

    /**
     * Отправляет сообщение из предложений (suggestion).
     */
    fun sendSuggestion(suggestion: String) {
        updateInputText(suggestion)
        sendMessage()
    }

    /**
     * Очищает все сообщения из чата.
     */
    fun clearChat() {
        logger.i("Clearing chat. Previous messages count: ${_uiState.value.enhancedMessages.size}")
        _uiState.value = _uiState.value.copy(enhancedMessages = emptyList())
    }

    private fun generateId(): String = "${currentTimeMillis()}-${(0..999).random()}"

    /**
     * UI-состояние для экрана чата.
     */
    data class ChatUiState(
        // Список расширенных сообщений с поддержкой структурированных ответов
        val enhancedMessages: List<EnhancedChatMessage> = emptyList(),
        val inputText: String = "",
        val selectedModel: String = ModelConfig.DEFAULT_MODEL,
        val isLoading: Boolean = false,
        val isModelSelectorExpanded: Boolean = false
    ) {
        // Обратная совместимость - простой список для старого UI
        val messages: List<UiChatMessage>
            get() = enhancedMessages.map { it.toUiChatMessage() }
    }
}
