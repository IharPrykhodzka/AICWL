package ru.assistant.aicwl.chat.agent

import ru.assistant.aicwl.chat.data.ChatMessage
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
                val content = response.choices?.firstOrNull()?.message?.content
                    ?: "No response from model"

                logger.i("Response received. Length: ${content.length}")
                logger.d("Response preview: ${content.take(150)}...")

                content
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
     *
     * @param message Сообщение пользователя
     * @param modelId Модель для использования
     * @param conversationHistory Предыдущие сообщения для контекста
     * @param customSystemPrompt Опциональный кастомный системный промт
     * @return Текст ответа AI
     */
    suspend fun chatWithHistory(
        message: String,
        modelId: String,
        conversationHistory: List<String>,
        customSystemPrompt: String? = null
    ): String {
        logger.i("Chat with history. Model: $modelId, History size: ${conversationHistory.size}")

        // Формируем список сообщений с историей
        val messages = buildList {
            // Системный промт
            add(
                ChatMessage(
                    role = "system",
                    content = customSystemPrompt ?: SystemPromptConfig.getSystemPrompt()
                )
            )

            // История разговора (попеременно user/assistant)
            conversationHistory.forEach { historyMessage ->
                // Для упрощения добавляем всё как пользовательские сообщения
                // В будущем можно улучшить с разделением ролей
                add(
                    ChatMessage(
                        role = "user",
                        content = historyMessage
                    )
                )
            }

            // Текущее сообщение
            add(
                ChatMessage(
                    role = "user",
                    content = message
                )
            )
        }

        logger.d("Total messages: ${messages.size}")

        val result = chatApiClient.sendChatRequest(modelId, messages)

        return result.fold(
            onSuccess = { response ->
                val content = response.choices?.firstOrNull()?.message?.content
                    ?: "No response from model"
                logger.i("Response received. Length: ${content.length}")
                content
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
