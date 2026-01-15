package ru.assistant.aicwl.chat.agent

import ru.assistant.aicwl.chat.data.ChatMessage
import ru.assistant.aicwl.chat.data.InterviewHistoryEntry
import ru.assistant.aicwl.chat.network.chatApiClient
import ru.assistant.aicwl.chat.prompt.SystemPromptConfig
import ru.assistant.aicwl.chat.utils.createLogger

/**
 * Агент чата, взаимодействующий с Z.AI API.
 * Поддерживает динамическое переключение моделей на основе выбора пользователя.
 * Автоматически добавляет системный промт ко всем запросам.
 */
class ChatAgent {
    private val logger = createLogger("ChatAgent")

    /**
     * Отправляет сообщение AI, используя указанную модель.
     * Системный промт добавляется автоматически.
     *
     * @param message Сообщение пользователя
     * @param modelId Модель для генерации ответа
     * @param customSystemPrompt Опциональный кастомный системный промт (заменяет стандартный)
     * @return Текст ответа AI или сообщение об ошибке
     */
    suspend fun chat(
        message: String,
        modelId: String,
        customSystemPrompt: String? = null
    ): String {
        logger.i("Chat requested. Model: $modelId")
        logger.d("User message length: ${message.length}")

        // Формируем список сообщений с системным промтом
        val messages = buildList {
            // Добавляем системный промт
            add(
                ChatMessage(
                    role = "system",
//                    content = customSystemPrompt ?: SystemPromptConfig.getSystemPrompt()
                    content = SystemPromptConfig.getSystemPrompt()
                )
            )
            // Добавляем сообщение пользователя
            add(
                ChatMessage(
                    role = "user",
                    content = message
                )
            )
        }

        val result = chatApiClient.sendChatRequest(modelId, messages)

        return result.fold(
            onSuccess = { response ->
                val message = response.choices?.firstOrNull()?.message
                val content = message?.content
                val reasoningContent = message?.reasoning_content

                // Используем reasoning_content если content пустой
                val actualContent = when {
                    !content.isNullOrBlank() -> content
                    !reasoningContent.isNullOrBlank() -> reasoningContent
                    else -> "No response from model"
                }

                logger.i("Response received. Length: ${actualContent.length}")
                logger.d("Response preview: ${actualContent.take(150)}...")

                actualContent
            },
            onFailure = { exception ->
                val errorMsg = "Error: ${exception.message}"
                logger.e(errorMsg, exception)
                logger.e("Exception type: ${exception::class.simpleName}")

                // Предоставляем понятные пользователю сообщения об ошибках
                when {
                    exception.message?.contains("timeout", ignoreCase = true) == true ->
                        "Request timeout. The AI model is taking too long. Try the 'Fastest' model."
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
        )
    }

    /**
     * Отправляет сообщение с историей разговора для контекста.
     * Системный промт добавляется автоматически.
     * История передается с правильным чередованием ролей user/assistant.
     *
     * @param message Сообщение пользователя
     * @param modelId Модель для использования
     * @param conversationHistory Предыдущие сообщения с ролями для контекста
     * @param currentQuestionNumber Номер текущего вопроса (для режима интервью)
     * @param fixedTotalQuestions Зафиксированное общее количество вопросов (защита от изменений)
     * @param customSystemPrompt Опциональный кастомный системный промт
     * @return Текст ответа AI
     */
    suspend fun chatWithHistory(
        message: String,
        modelId: String,
        conversationHistory: List<InterviewHistoryEntry> = emptyList(),
        currentQuestionNumber: Int? = null,
        fixedTotalQuestions: Int? = null,
        customSystemPrompt: String? = null
    ): String {
        logger.i("Chat with history. Model: $modelId, History size: ${conversationHistory.size}, Current question: $currentQuestionNumber, Fixed totalQuestions: $fixedTotalQuestions")

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
            // Системный промт
            add(
                ChatMessage(
                    role = "system",
                    content = systemPrompt
                )
            )

            // История разговора с правильными ролями
            conversationHistory.forEach { entry ->
                add(entry.toChatMessage())
            }

            // Текущее сообщение пользователя
            add(
                ChatMessage(
                    role = "user",
                    content = message
                )
            )
        }

        logger.d("Total messages: ${messages.size}")
        logger.d("Messages breakdown: system=1, history=${conversationHistory.size}, user=1")

        val result = chatApiClient.sendChatRequest(modelId, messages)

        return result.fold(
            onSuccess = { response ->
                val message = response.choices?.firstOrNull()?.message
                val content = message?.content
                val reasoningContent = message?.reasoning_content

                // Используем reasoning_content если content пустой
                val actualContent = when {
                    !content.isNullOrBlank() -> content
                    !reasoningContent.isNullOrBlank() -> reasoningContent
                    else -> "No response from model"
                }

                logger.i("Response received. Length: ${actualContent.length}")
                actualContent
            },
            onFailure = { exception ->
                logger.e("Chat with history failed", exception)
                "Error: ${exception.message ?: "Unknown error"}"
            }
        )
    }
}

/**
 * Одиночный экземпляр (singleton) агента чата.
 */
val chatAgent = ChatAgent()
