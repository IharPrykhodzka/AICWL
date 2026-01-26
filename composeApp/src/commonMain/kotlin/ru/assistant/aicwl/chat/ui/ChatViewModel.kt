package ru.assistant.aicwl.chat.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.assistant.aicwl.chat.agent.chatAgent
import ru.assistant.aicwl.chat.config.ModelConfig
import ru.assistant.aicwl.chat.config.TemperatureProfile
import ru.assistant.aicwl.chat.prompt.PromptRules
import ru.assistant.aicwl.chat.prompt.SystemPromptConfig
import ru.assistant.aicwl.chat.data.EnhancedChatMessage
import ru.assistant.aicwl.chat.data.InterviewHistoryEntry
import ru.assistant.aicwl.chat.data.MessageRole
import ru.assistant.aicwl.chat.data.UiChatMessage
import ru.assistant.aicwl.chat.provider.ProviderType
import ru.assistant.aicwl.chat.provider.model.AIModelConfig
import ru.assistant.aicwl.chat.provider.model.UnifiedAIModel
import ru.assistant.aicwl.chat.utils.currentTimeMillis
import ru.assistant.aicwl.chat.utils.createLogger

/**
 * ViewModel для управления состоянием чата и взаимодействиями.
 * Поддерживает структурированные ответы от AI.
 */
class ChatViewModel(
    private val chatHistoryRepository: ru.assistant.aicwl.chat.data.ChatHistoryRepository,
    private val tokenTracker: ru.assistant.aicwl.chat.tokens.TokenTracker? = null
) : ViewModel() {
    private val logger = createLogger("ChatViewModel")

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        loadChatHistory()
        loadTokenStatistics()
    }

    /**
     * Обновляет выбранную AI-модель.
     */
    fun selectModel(model: UnifiedAIModel) {
        logger.i("Model selected: ${model.modelId} (${model.displayName}) - Provider: ${model.providerType}")

        // Обновляем модель и пересчитываем оценку токенов
        _uiState.value = _uiState.value.copy(
            selectedModel = model,
            selectedProvider = model.providerType
        )

        // Пересчитываем оценку токенов для текущего ввода
        if (_uiState.value.inputText.isNotBlank()) {
            updateInputText(_uiState.value.inputText)
        }
    }

    /**
     * Обновляет выбранную AI-модель по ID (для обратной совместимости).
     */
    fun selectModel(modelId: String) {
        // Пытаемся найти модель в списке всех моделей
        val model = AIModelConfig.getAllModels().find { it.modelId == modelId }
        if (model != null) {
            selectModel(model)
        } else {
            logger.w("Model not found in AIModelConfig: $modelId, using fallback")
            // Fallback - создаем временную модель на основе ModelConfig
            _uiState.value = _uiState.value.copy(selectedModel = AIModelConfig.defaultModel)
        }
    }

    /**
     * Обновляет выбранный провайдер.
     */
    fun selectProvider(provider: ProviderType) {
        logger.i("Provider selected: $provider")
        val defaultModel = AIModelConfig.getDefaultModelForProvider(provider)
        _uiState.value = _uiState.value.copy(
            selectedProvider = provider,
            selectedModel = defaultModel
        )

        // Пересчитываем оценку токенов для текущего ввода
        if (_uiState.value.inputText.isNotBlank()) {
            updateInputText(_uiState.value.inputText)
        }
    }

    /**
     * Обновляет выбранный профиль температуры.
     */
    fun selectProfile(profile: TemperatureProfile) {
        logger.i("Profile selected: ${profile.displayName} (${profile.description})")
        _uiState.value = _uiState.value.copy(selectedProfile = profile)
    }

    /**
     * Обновляет текст ввода пользователя.
     */
    fun updateInputText(text: String) {
        // Вычисляем оценку токенов для ввода и полного запроса
        val (inputEstimate, requestEstimate) = tokenTracker?.let { tracker ->
            val model = _uiState.value.selectedModel

            // Оценка токенов только для ввода (для обратной совместимости)
            val inputEstimate = ru.assistant.aicwl.chat.tokens.TokenCounter.estimateTextTokens(text, model)

            // Детальная оценка токенов для полного запроса
            val requestEstimate = calculateRequestTokenEstimate(text, model)

            Pair(inputEstimate, requestEstimate)
        } ?: Pair(null, null)

        // Обновляем состояние одним вызовом copy
        _uiState.value = _uiState.value.copy(
            inputText = text,
            currentInputEstimate = inputEstimate,
            requestTokenEstimate = requestEstimate
        )
    }

    /**
     * Вычисляет детальную оценку токенов для полного запроса.
     * Учитывает системный промпт, историю сообщений и текущий ввод.
     */
    private fun calculateRequestTokenEstimate(
        inputText: String,
        model: ru.assistant.aicwl.chat.provider.model.UnifiedAIModel
    ): ru.assistant.aicwl.chat.tokens.RequestTokenEstimate? {
        val isBusinessMode = _uiState.value.isBusinessAnalystMode

        return if (isBusinessMode) {
            // В режиме бизнес-аналитика используем историю интервью
            ru.assistant.aicwl.chat.tokens.RequestTokenEstimate.forRequestWithHistory(
                inputText = inputText,
                conversationHistory = _uiState.value.businessAnalystHistory,
                model = model,
                currentQuestionNumber = _uiState.value.businessAnalystHistory
                    .count { it.role == MessageRole.ASSISTANT } + 1,
                fixedTotalQuestions = _uiState.value.fixedTotalQuestions
            )
        } else {
            // В обычном режиме используем историю enhancedMessages
            val conversationHistory = _uiState.value.enhancedMessages
                .map { msg ->
                    InterviewHistoryEntry(
                        role = msg.role,
                        content = msg.originalContent
                    )
                }

            ru.assistant.aicwl.chat.tokens.RequestTokenEstimate.forRequestWithHistory(
                inputText = inputText,
                conversationHistory = conversationHistory,
                model = model
            )
        }
    }

    /**
     * Загружает статистику токенов.
     */
    private fun loadTokenStatistics() {
        viewModelScope.launch {
            try {
                tokenTracker?.let { tracker ->
                    val stats = tracker.getCurrentStatistics()
                    _uiState.value = _uiState.value.copy(tokenStatistics = stats)
                    logger.i("Loaded token statistics: ${stats.totalRequests} requests, ${stats.totalTokens} tokens")
                }
            } catch (e: Exception) {
                logger.e("Failed to load token statistics", e)
            }
        }
    }

    /**
     * Сбрасывает статистику токенов.
     */
    fun resetTokenStatistics() {
        viewModelScope.launch {
            try {
                tokenTracker?.resetStatistics()
                // Обновляем UI после сброса
                tokenTracker?.let { tracker ->
                    val stats = tracker.getCurrentStatistics()
                    _uiState.value = _uiState.value.copy(tokenStatistics = stats)
                }
                logger.i("Token statistics reset")
            } catch (e: Exception) {
                logger.e("Failed to reset token statistics", e)
            }
        }
    }

    /**
     * Обновляет статистику токенов в UI.
     */
    private fun refreshTokenStatistics() {
        viewModelScope.launch {
            try {
                tokenTracker?.let { tracker ->
                    val stats = tracker.getCurrentStatistics()
                    _uiState.value = _uiState.value.copy(tokenStatistics = stats)
                    logger.d("Token statistics refreshed: ${stats.totalTokens} tokens")
                }
            } catch (e: Exception) {
                logger.e("Failed to refresh token statistics", e)
            }
        }
    }

    /**
     * Отправляет сообщение пользователя и получает ответ AI.
     * Автоматически определяет тип ответа (структурированный или обычный).
     * В режиме бизнес-аналитика использует историю диалога для контекста.
     */
    fun sendMessage() {
        val currentInput = _uiState.value.inputText.trim()
        if (currentInput.isEmpty()) {
            logger.w("Attempted to send empty message")
            return
        }

        val isBusinessMode = _uiState.value.isBusinessAnalystMode
        logger.i("Sending message. Model: ${_uiState.value.selectedModel}, Business mode: $isBusinessMode")
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

        // Логируем текущий системный промт для отладки
        val currentPrompt = ru.assistant.aicwl.chat.prompt.SystemPromptConfig.getSystemPrompt()
        logger.d("Current system prompt length: ${currentPrompt.length}")
        logger.d("System prompt preview: ${currentPrompt.take(300)}...")

        // Получаем ответ AI
        viewModelScope.launch {
            try {
                logger.d("Starting AI request...")

                val response = if (isBusinessMode) {
                    // В режиме бизнес-аналитика используем историю
                    // Определяем текущий номер вопроса на основе истории
                    val currentQuestionNumber = _uiState.value.businessAnalystHistory
                        .count { it.role == MessageRole.ASSISTANT } + 1

                    chatAgent.chatWithHistory(
                        message = currentInput,
                        modelId = _uiState.value.selectedModel.modelId,
                        conversationHistory = _uiState.value.businessAnalystHistory,
                        parameters = _uiState.value.selectedProfile.parameters,
                        currentQuestionNumber = currentQuestionNumber,
                        fixedTotalQuestions = _uiState.value.fixedTotalQuestions
                    )
                } else {
                    // Обычный режим - с историей сообщений для контекста
                    // Исключаем последнее сообщение (текущий ввод пользователя) так как оно будет добавлено в chatWithHistory
                    val conversationHistory = _uiState.value.enhancedMessages
                        .dropLast(1)  // Убираем текущее сообщение пользователя из истории
                        .map { msg ->
                            ru.assistant.aicwl.chat.data.InterviewHistoryEntry(
                                role = msg.role,
                                content = msg.originalContent
                            )
                        }

                    logger.d("Sending chat with history. Previous messages: ${conversationHistory.size}, Current: ${currentInput.take(50)}...")

                    chatAgent.chatWithHistory(
                        message = currentInput,
                        modelId = _uiState.value.selectedModel.modelId,
                        conversationHistory = conversationHistory,
                        parameters = _uiState.value.selectedProfile.parameters
                    )
                }

                logger.i("AI response received. Length: ${response.length}")
                logger.d("Response preview: ${response.take(200)}...")

                // Убираем пустые строки в начале и конце ответа
                val trimmedResponse = response.trim()

                // Получаем информацию о токенах для этого сообщения
                val tokenInfo = tokenTracker?.getLastMessageTokenInfo()

                // Создаём enhanced сообщение с автоматическим определением типа
                val assistantMessage = EnhancedChatMessage.fromAiResponse(
                    id = generateId(),
                    content = trimmedResponse,
                    timestamp = currentTimeMillis(),
                    tokenInfo = tokenInfo
                )

                logger.d("Message type: ${assistantMessage.messageType}")
                logger.d("Is structured: ${assistantMessage.structuredData != null}")
                logger.d("Token info: ${tokenInfo?.let { "${it.totalTokens} tokens, $${it.cost}" } ?: "N/A"}")

                // Обрабатываем историю для режима бизнес-аналитика
                val (newHistory, newFixedTotalQuestions) = if (isBusinessMode) {
                    val structured = assistantMessage.structuredData
                    when (structured?.computedStatus) {
                        ru.assistant.aicwl.chat.data.ResponseStatus.SUCCESS -> {
                            // Финальный ответ - очищаем историю и fixedTotalQuestions
                            logger.i("Business analyst mode: Final response received, clearing history")
                            Pair(emptyList<InterviewHistoryEntry>(), null)
                        }
                        ru.assistant.aicwl.chat.data.ResponseStatus.NEEDS_CLARIFICATION -> {
                            // Уточняющие вопросы - сохраняем в историю с правильными ролями
                            // Зафиксируем totalQuestions из первого ответа если ещё не зафиксирован
                            val fixedTotalQuestions = _uiState.value.fixedTotalQuestions ?: structured.totalQuestions

                            // Используем зафиксированное значение вместо того что пришло от AI
                            val actualTotalQuestions = fixedTotalQuestions ?: structured.totalQuestions

                            val userEntry = InterviewHistoryEntry(
                                role = MessageRole.USER,
                                content = currentInput
                            )

                            // Формируем контент ответа ассистента с вопросами
                            val assistantContent = buildString {
                                append(structured.safeSummary)
                                if (structured.reasoning.isNotBlank()) {
                                    append("\n\nЛогика: ${structured.reasoning}")
                                }
                                if (structured.questions.isNotEmpty()) {
                                    append("\n\nЗаданные вопросы:")
                                    structured.questions.forEachIndexed { index, question ->
                                        append("\n${index + 1}. $question")
                                    }
                                }
                            }

                            val assistantEntry = InterviewHistoryEntry(
                                role = MessageRole.ASSISTANT,
                                content = assistantContent,
                                questionNumber = structured.questionNumber,
                                totalQuestions = actualTotalQuestions  // Используем зафиксированное значение
                            )

                            logger.d("Business analyst mode: Adding entries to history")
                            logger.d("User: ${currentInput.take(100)}...")
                            logger.d("Assistant: ${assistantContent.take(100)}...")
                            logger.d("Fixed totalQuestions: $fixedTotalQuestions, AI sent: ${structured.totalQuestions}, Using: $actualTotalQuestions")

                            Pair(_uiState.value.businessAnalystHistory + userEntry + assistantEntry, fixedTotalQuestions)
                        }
                        else -> Pair(_uiState.value.businessAnalystHistory, _uiState.value.fixedTotalQuestions)
                    }
                } else {
                    Pair(_uiState.value.businessAnalystHistory, _uiState.value.fixedTotalQuestions)
                }

                _uiState.value = _uiState.value.copy(
                    enhancedMessages = _uiState.value.enhancedMessages + assistantMessage,
                    isLoading = false,
                    businessAnalystHistory = newHistory,
                    fixedTotalQuestions = newFixedTotalQuestions
                )

                logger.d("Assistant message added. Total messages: ${_uiState.value.enhancedMessages.size}")
                logger.d("Business analyst history size: ${newHistory.size}")

                // Auto-save chat history after successful message
                saveChatHistory()

                // Refresh token statistics after successful message
                refreshTokenStatistics()
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
        _uiState.value = _uiState.value.copy(
            enhancedMessages = emptyList(),
            businessAnalystHistory = emptyList(),
            fixedTotalQuestions = null
        )
    }

    /**
     * Загружает историю чата при инициализации.
     * Вызывается автоматически в init блоке если история включена.
     */
    private fun loadChatHistory() {
        viewModelScope.launch {
            try {
                val isEnabled = chatHistoryRepository.isChatHistoryEnabled()
                logger.i("Chat history enabled: $isEnabled")

                if (isEnabled) {
                    val history = chatHistoryRepository.getChatHistory()
                    if (history != null) {
                        logger.i("Loaded chat history with ${history.messages.size} messages")
                        _uiState.value = _uiState.value.copy(
                            enhancedMessages = history.messages.map { it.toDomain() },
                            businessAnalystHistory = history.businessAnalystHistory.map { it.toDomain() },
                            fixedTotalQuestions = history.fixedTotalQuestions
                        )
                    } else {
                        logger.i("No saved chat history found")
                    }
                }
            } catch (e: Exception) {
                logger.e("Failed to load chat history", e)
            }
        }
    }

    /**
     * Сохраняет текущую историю чата.
     * Вызывается автоматически после каждого нового сообщения если история включена.
     */
    private suspend fun saveChatHistory() {
        try {
            val isEnabled = chatHistoryRepository.isChatHistoryEnabled()
            if (!isEnabled) {
                logger.d("Chat history is disabled, skipping save")
                return
            }

            val history = ru.assistant.aicwl.chat.data.ChatHistoryData(
                messages = _uiState.value.enhancedMessages.map {
                    ru.assistant.aicwl.chat.data.SerializableChatMessage.fromDomain(it)
                },
                businessAnalystHistory = _uiState.value.businessAnalystHistory.map {
                    ru.assistant.aicwl.chat.data.SerializableInterviewEntry.fromDomain(it)
                },
                fixedTotalQuestions = _uiState.value.fixedTotalQuestions
            )

            chatHistoryRepository.saveChatHistory(history)
            logger.d("Chat history saved with ${history.messages.size} messages")
        } catch (e: Exception) {
            logger.e("Failed to save chat history", e)
        }
    }

    /**
     * Переключает режим бизнес-аналитика.
     * В этом режиме AI задает уточняющие вопросы для сбора требований.
     */
    fun toggleBusinessAnalystMode() {
        setBusinessAnalystMode(!_uiState.value.isBusinessAnalystMode)
    }

    /**
     * Устанавливает режим бизнес-аналитика.
     * @param enabled true для включения режима
     */
    fun setBusinessAnalystMode(enabled: Boolean) {
        logger.i("Business analyst mode: ${if (enabled) "ENABLED" else "DISABLED"}")

        if (enabled) {
            SystemPromptConfig.enableBusinessAnalystMode()
            logger.i("Business analyst mode enabled - using specialized prompt")
        } else {
            SystemPromptConfig.disableBusinessAnalystMode()
            logger.i("Business analyst mode disabled - using standard prompt")
        }

        // Логируем текущий промт для отладки
        logger.d("Current prompt length: ${SystemPromptConfig.getSystemPrompt().length}")
        logger.d("Current prompt preview: ${SystemPromptConfig.getSystemPrompt().take(300)}...")

        _uiState.value = _uiState.value.copy(
            isBusinessAnalystMode = enabled,
            businessAnalystHistory = emptyList(),  // Очищаем историю при переключении
            fixedTotalQuestions = null  // Очищаем зафиксированное количество вопросов
        )
    }

    private fun generateId(): String = "${currentTimeMillis()}-${(0..999).random()}"

    /**
     * UI-состояние для экрана чата.
     */
    data class ChatUiState(
        // Список расширенных сообщений с поддержкой структурированных ответов
        val enhancedMessages: List<EnhancedChatMessage> = emptyList(),
        val inputText: String = "",
        val selectedModel: UnifiedAIModel = AIModelConfig.defaultModel,
        val selectedProvider: ProviderType = ProviderType.DEFAULT,
        val selectedProfile: TemperatureProfile = TemperatureProfile.DEFAULT,
        val isLoading: Boolean = false,
        val isModelSelectorExpanded: Boolean = false,
        // Режим бизнес-аналитика - AI задает вопросы для сбора требований
        val isBusinessAnalystMode: Boolean = false,
        // История диалога для режима бизнес-аналитика с правильными ролями
        val businessAnalystHistory: List<InterviewHistoryEntry> = emptyList(),
        // Зафиксированное количество вопросов (из первого ответа AI)
        // Защищает от изменения totalQuestions во время интервью
        val fixedTotalQuestions: Int? = null,
        // Статистика использования токенов
        val tokenStatistics: ru.assistant.aicwl.chat.tokens.TokenStatistics = ru.assistant.aicwl.chat.tokens.TokenStatistics(),
        // Оценка токенов для текущего ввода
        val currentInputEstimate: ru.assistant.aicwl.chat.tokens.TokenEstimate? = null,
        // Детальная оценка токенов для полного запроса (system + history + input)
        val requestTokenEstimate: ru.assistant.aicwl.chat.tokens.RequestTokenEstimate? = null
    ) {
        // Обратная совместимость - простой список для старого UI
        val messages: List<UiChatMessage>
            get() = enhancedMessages.map { it.toUiChatMessage() }

        // Обратная совместимость - строковый ID модели
        val modelId: String
            get() = selectedModel.modelId
    }
}
