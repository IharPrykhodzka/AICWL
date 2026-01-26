package ru.assistant.aicwl.chat.agent

import ru.assistant.aicwl.chat.data.ChatRequestParameters
import ru.assistant.aicwl.chat.data.InterviewHistoryEntry
import ru.assistant.aicwl.chat.data.unified.UnifiedChatMessage
import ru.assistant.aicwl.chat.data.unified.UnifiedChatRequest
import ru.assistant.aicwl.chat.data.unified.MessageRole
import ru.assistant.aicwl.chat.provider.AIProviderFactory
import ru.assistant.aicwl.chat.provider.ProviderType
import ru.assistant.aicwl.chat.prompt.SystemPromptConfig
import ru.assistant.aicwl.chat.utils.createLogger
import ru.assistant.aicwl.chat.utils.PlatformTime

/**
 * Агент чата с поддержкой Z.ai AI-провайдера.
 *
 * Для обратной совместимости сохраняет старый API с modelId: String,
 * но рекомендуется использовать новые методы с ProviderType.
 */
class ChatAgent(
    private val tokenTracker: ru.assistant.aicwl.chat.tokens.TokenTracker? = null
) {
    private val logger = createLogger("ChatAgent")

    /**
     * Определяет провайдер по ID модели.
     * Для обратной совместимости со старым кодом.
     */
    private fun inferProviderFromModel(modelId: String): ProviderType {
        return when {
            modelId.startsWith("glm-") -> ProviderType.ZAI
            modelId.contains("Qwen", ignoreCase = true) || modelId.contains("featherless-ai") -> ProviderType.QWEN
            else -> ProviderType.DEFAULT
        }
    }

    /**
     * Отправляет сообщение AI, используя указанную модель.
     * Системный промт добавляется автоматически.
     *
     * @param message Сообщение пользователя
     * @param modelId Модель для генерации ответа
     * @param parameters Параметры генерации (temperature, maxTokens, и т.д.)
     * @param customSystemPrompt Опциональный кастомный системный промт
     * @return Текст ответа AI или сообщение об ошибке
     */
    suspend fun chat(
        message: String,
        modelId: String,
        parameters: ChatRequestParameters? = null,
        customSystemPrompt: String? = null
    ): String {
        val providerType = inferProviderFromModel(modelId)
        return chatWithProvider(message, providerType, modelId, parameters, customSystemPrompt)
    }

    /**
     * Отправляет сообщение используя указанный провайдер.
     * Рекомендуемый метод для новой архитектуры.
     *
     * @param message Сообщение пользователя
     * @param providerType Тип AI-провайдера
     * @param modelId ID модели
     * @param parameters Параметры генерации
     * @param customSystemPrompt Опциональный кастомный системный промт
     * @return Текст ответа AI или сообщение об ошибке
     */
    suspend fun chatWithProvider(
        message: String,
        providerType: ProviderType,
        modelId: String,
        parameters: ChatRequestParameters? = null,
        customSystemPrompt: String? = null
    ): String {
        logger.i("Chat requested. Provider: $providerType, Model: $modelId")
        logger.d("User message length: ${message.length}")

        // Формируем список сообщений с системным промтом
        val messages = buildList {
            add(
                UnifiedChatMessage(
                    role = MessageRole.SYSTEM,
                    content = customSystemPrompt ?: SystemPromptConfig.getSystemPrompt()
                )
            )
            add(
                UnifiedChatMessage(
                    role = MessageRole.USER,
                    content = message
                )
            )
        }

        return sendRequest(providerType, modelId, messages, parameters)
    }

    /**
     * Отправляет сообщение с историей разговора для контекста.
     * Системный промт добавляется автоматически.
     *
     * @param message Сообщение пользователя
     * @param modelId Модель для использования
     * @param conversationHistory Предыдущие сообщения с ролями для контекста
     * @param parameters Параметры генерации
     * @param currentQuestionNumber Номер текущего вопроса (для режима интервью)
     * @param fixedTotalQuestions Зафиксированное общее количество вопросов
     * @param customSystemPrompt Опциональный кастомный системный промт
     * @return Текст ответа AI
     */
    suspend fun chatWithHistory(
        message: String,
        modelId: String,
        conversationHistory: List<InterviewHistoryEntry> = emptyList(),
        parameters: ChatRequestParameters? = null,
        currentQuestionNumber: Int? = null,
        fixedTotalQuestions: Int? = null,
        customSystemPrompt: String? = null
    ): String {
        val providerType = inferProviderFromModel(modelId)
        return chatWithHistory(
            message, providerType, modelId, conversationHistory,
            parameters, currentQuestionNumber, fixedTotalQuestions, customSystemPrompt
        )
    }

    /**
     * Отправляет сообщение с историей, используя указанный провайдер.
     * Рекомендуемый метод для новой архитектуры.
     */
    suspend fun chatWithHistory(
        message: String,
        providerType: ProviderType,
        modelId: String,
        conversationHistory: List<InterviewHistoryEntry> = emptyList(),
        parameters: ChatRequestParameters? = null,
        currentQuestionNumber: Int? = null,
        fixedTotalQuestions: Int? = null,
        customSystemPrompt: String? = null
    ): String {
        logger.i("Chat with history. Provider: $providerType, Model: $modelId, History size: ${conversationHistory.size}")

        // Формируем системный промт с информацией о прогрессе интервью
        val systemPrompt = if (currentQuestionNumber != null && conversationHistory.isNotEmpty()) {
            buildString {
                append(customSystemPrompt ?: SystemPromptConfig.getSystemPrompt())
                appendLine()
                appendLine()
                appendLine("ТЕКУЩИЙ ПРОГРЕСС ИНТЕРВЬЮ:")
                appendLine("- Это вопрос №$currentQuestionNumber из интервью")
                if (fixedTotalQuestions != null) {
                    appendLine("- ЗАФИКСИРОВАННОЕ количество вопросов: $fixedTotalQuestions")
                    appendLine("- totalQuestions ДОЛЖЕН оставаться $fixedTotalQuestions - НЕ МЕНЯЙ ЕГО!")
                }
                appendLine("- Учти эту информацию при формировании ответа")
            }
        } else {
            customSystemPrompt ?: SystemPromptConfig.getSystemPrompt()
        }

        // Формируем список сообщений с историей
        val messages = buildList {
            add(
                UnifiedChatMessage(
                    role = MessageRole.SYSTEM,
                    content = systemPrompt
                )
            )

            // История разговора с правильными ролями
            conversationHistory.forEach { entry ->
                add(entry.toUnifiedChatMessage())
            }

            add(
                UnifiedChatMessage(
                    role = MessageRole.USER,
                    content = message
                )
            )
        }

        logger.d("Total messages: ${messages.size}")

        return sendRequest(providerType, modelId, messages, parameters)
    }

    /**
     * Отправляет запрос через AIProvider.
     */
    private suspend fun sendRequest(
        providerType: ProviderType,
        modelId: String,
        messages: List<UnifiedChatMessage>,
        parameters: ChatRequestParameters?
    ): String {
        return try {
            // Логируем параметры запроса для отладки
            logger.d("Request parameters: temperature=${parameters?.temperature}, " +
                     "doSample=${parameters?.doSample}, maxTokens=${parameters?.maxTokens}, " +
                     "topP=${parameters?.topP}, thinking=${parameters?.thinking?.type}")

            val provider = AIProviderFactory.createProvider(providerType)

            val request = UnifiedChatRequest(
                providerType = providerType,
                modelId = modelId,
                messages = messages,
                parameters = parameters,
                stream = false
            )

            val result = provider.sendChatRequest(request)

            result.fold(
                onSuccess = { response ->
                    val actualContent = when {
                        !response.content.isNullOrBlank() -> response.content
                        !response.thinkingContent.isNullOrBlank() -> response.thinkingContent
                        else -> "No response from model"
                    }

                    logger.i("Response received. Length: ${actualContent.length}")

                    // Записываем использование токенов (с fallback на оценку)
                    tokenTracker?.let { tracker ->
                        val model = ru.assistant.aicwl.chat.provider.model.AIModelConfig
                            .getAllModels()
                            .find { it.modelId == modelId }

                        if (model != null) {
                            // Проверяем есть ли реальные данные от API
                            val hasValidUsage = response.usage != null &&
                                    response.usage.promptTokens != null &&
                                    response.usage.completionTokens != null

                            if (hasValidUsage) {
                                // Используем данные от API
                                tracker.recordFromApi(
                                    apiUsage = ru.assistant.aicwl.chat.data.Usage(
                                        promptTokens = response.usage.promptTokens,
                                        completionTokens = response.usage.completionTokens,
                                        totalTokens = response.usage.totalTokens
                                    ),
                                    model = model,
                                    timestamp = PlatformTime.currentTimeMillis()
                                )
                                logger.d("Token usage recorded from API")
                            } else {
                                // Fallback: оцениваем токены на стороне клиента
                                val promptText = messages.joinToString("\n") { it.content }
                                val estimatedPromptTokens = ru.assistant.aicwl.chat.tokens.TokenCounter.estimateTokens(promptText)
                                val estimatedCompletionTokens = ru.assistant.aicwl.chat.tokens.TokenCounter.estimateTokens(actualContent)
                                val estimatedTotal = estimatedPromptTokens + estimatedCompletionTokens

                                // Создаём Usage объект с оценёнными значениями
                                val estimatedUsage = ru.assistant.aicwl.chat.data.Usage(
                                    promptTokens = estimatedPromptTokens,
                                    completionTokens = estimatedCompletionTokens,
                                    totalTokens = estimatedTotal
                                )

                                tracker.recordFromApi(
                                    apiUsage = estimatedUsage,
                                    model = model,
                                    timestamp = PlatformTime.currentTimeMillis()
                                )
                                logger.d("Token usage estimated on client side: prompt=$estimatedPromptTokens, completion=$estimatedCompletionTokens, total=$estimatedTotal")
                            }
                        }
                    }

                    actualContent
                },
                onFailure = { exception ->
                    val errorMsg = formatErrorMessage(exception)
                    logger.e(errorMsg, exception)
                    errorMsg
                }
            )
        } catch (e: Exception) {
            val errorMsg = formatErrorMessage(e)
            logger.e("Request failed for provider: $providerType, model: $modelId", e)
            errorMsg
        }
    }

    /**
     * Форматирует исключение в понятное пользователю сообщение.
     */
    private fun formatErrorMessage(exception: Throwable): String {
        return when {
            exception.message?.contains("timeout", ignoreCase = true) == true ->
                "Request timeout. The AI model is taking too long. Try a faster model."
            exception.message?.contains("401", ignoreCase = true) == true ||
            exception.message?.contains("Unauthorized", ignoreCase = true) == true ->
                "API Key error. Check your configuration."
            exception.message?.contains("429", ignoreCase = true) == true ->
                "Too many requests. Please wait a moment."
            exception.message?.contains("connection", ignoreCase = true) == true ->
                "Connection error. Check your internet connection."
            else -> "Error: ${exception.message ?: "Unknown error"}"
        }
    }
}

/**
 * Одиночный экземпляр (singleton) агента чата.
 * Использует ленивую инициализацию с поддержкой TokenTracker.
 * Note: Для KMP упрощенная реализация без синхронизации.
 */
private var chatAgentInstance: ChatAgent? = null

/**
 * Получает экземпляр ChatAgent с опциональным TokenTracker.
 * Если TokenTracker доступен, будет использоваться для отслеживания токенов.
 */
fun getChatAgent(tokenTracker: ru.assistant.aicwl.chat.tokens.TokenTracker? = null): ChatAgent {
    return chatAgentInstance ?: run {
        val instance = ChatAgent(tokenTracker = tokenTracker)
        chatAgentInstance = instance
        instance
    }
}

/**
 * Инициализирует ChatAgent с указанным TokenTracker.
 * Должна вызываться при запуске приложения для включения отслеживания токенов.
 */
fun initializeChatAgent(tokenTracker: ru.assistant.aicwl.chat.tokens.TokenTracker?) {
    if (chatAgentInstance == null && tokenTracker != null) {
        chatAgentInstance = ChatAgent(tokenTracker = tokenTracker)
    }
}

/**
 * Одиночный экземпляр агента чата (для обратной совместимости).
 * Рекомендуется использовать getChatAgent() с TokenTracker.
 */
@Deprecated("Use getChatAgent() with TokenTracker for token tracking", ReplaceWith("getChatAgent(tokenTracker)"))
val chatAgent: ChatAgent
    get() = getChatAgent()
