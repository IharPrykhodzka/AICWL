package ru.assistant.aicwl.chat.network

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import ru.assistant.aicwl.chat.config.AppConfig
import ru.assistant.aicwl.chat.data.ChatCompletionRequest
import ru.assistant.aicwl.chat.data.ChatCompletionResponse
import ru.assistant.aicwl.chat.data.ChatMessage
import ru.assistant.aicwl.chat.data.ChatApiErrorResponse
import ru.assistant.aicwl.chat.utils.createLogger

/**
 * HTTP-клиент для взаимодействия с Z.AI Chat API.
 * Обрабатывает аутентификацию и JSON-сериализацию.
 */
class ChatApiClient {
    private val logger = createLogger("ChatApiClient")

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        prettyPrint = true
    }

    private val client = HttpClient {
        // Плагин HttpTimeout с увеличенными таймаутами для медленных AI-моделей
        install(HttpTimeout) {
            // Таймаут для установления соединения
            connectTimeoutMillis = 120_000  // 2 минуты

            // Общий таймаут для всего запроса (ожидание ответа)
            requestTimeoutMillis = 300_000   // 5 минут

            // Таймаут сокета - время между получаемыми пакетами данных
            socketTimeoutMillis = 300_000    // 5 минут
        }

        // Согласование содержимого для JSON
        install(ContentNegotiation) {
            json(json)
        }
    }

    /**
     * Отправляет запрос завершения чата с указанной моделью.
     *
     * @param modelId Идентификатор модели (например, "glm-4.7", "glm-4.6", "glm-4.5-air")
     * @param messages Список сообщений чата в разговоре
     * @return Result содержащий ChatCompletionResponse или ошибку
     */
    suspend fun sendChatRequest(
        modelId: String,
        messages: List<ChatMessage>
    ): Result<ChatCompletionResponse> {
        logger.i("Отправка запроса модели: $modelId, количество сообщений: ${messages.size}")
        logger.d("Endpoint: ${AppConfig.zApiEndpoint}")

        val request = ChatCompletionRequest(
            model = modelId,
            messages = messages
        )

        // Отладка: логируем JSON запроса
        val requestString = json.encodeToString(ChatCompletionRequest.serializer(), request)
        logger.d("Request JSON: $requestString")

        return try {
            val response: HttpResponse = client.post(AppConfig.zApiEndpoint) {
                // Основные заголовки для Z.AI API
                header("Content-Type", "application/json")
                header("Accept", "application/json")
                header("Accept-Language", "en-US,en")
                header("Authorization", AppConfig.zApiKey)  // БЕЗ префикса "Bearer "!
                header("User-Agent", "AICWL/1.0")

                logger.d("Request headers: Authorization=${maskApiKey(AppConfig.zApiKey)}")

                setBody(request)
            }

            // Получаем сырой ответ для логирования
            val rawBody: String = response.bodyAsText()
            logger.d("Raw response (${response.status}): ${rawBody.take(500)}...")

            // Проверяем HTTP-статус
            if (!response.status.isSuccess()) {
                logger.e("HTTP Error: ${response.status.value} - ${rawBody}")

                // Пытаемся распарсить ответ об ошибке
                try {
                    val errorResponse = json.decodeFromString(ChatApiErrorResponse.serializer(), rawBody)
                    val errorMsg = errorResponse.error?.message ?: "HTTP ${response.status.value}"
                    return Result.failure(Exception("API Error: ${errorResponse.error?.code ?: response.status.value} - $errorMsg"))
                } catch (e: Exception) {
                    return Result.failure(Exception("HTTP ${response.status.value}: ${rawBody.take(200)}"))
                }
            }

            // Парсим успешный ответ
            val parsedResponse = json.decodeFromString(ChatCompletionResponse.serializer(), rawBody)

            // Проверяем наличие choices
            if (parsedResponse.choices.isNullOrEmpty()) {
                logger.e("Response has no choices field. Raw: $rawBody")
                return Result.failure(Exception("Invalid API response: 'choices' field is missing or empty"))
            }

            logger.i("Response received. Choices count: ${parsedResponse.choices.size}")
            parsedResponse.choices.forEachIndexed { index, choice ->
                logger.d("Choice[$index]: role=${choice.message.role}, content length=${choice.message.content.length}")
            }

            Result.success(parsedResponse)
        } catch (e: Exception) {
            logger.e("Request failed for model: $modelId", e)
            logger.e("Exception type: ${e::class.simpleName}, Message: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Отправляет простое пользовательское сообщение и получает ответ.
     *
     * @param modelId Идентификатор модели
     * @param userMessage Текст сообщения пользователя
     * @return Result содержащий ChatCompletionResponse или ошибку
     */
    suspend fun sendUserMessage(
        modelId: String,
        userMessage: String
    ): Result<ChatCompletionResponse> {
        logger.i("Sending user message. Model: $modelId, Message length: ${userMessage.length}")
        logger.d("Message preview: ${userMessage.take(100)}...")

        val messages = listOf(
            ChatMessage(role = "user", content = userMessage)
        )
        return sendChatRequest(modelId, messages)
    }

    /**
     * Закрывает клиент по завершении работы.
     */
    fun close() {
        logger.i("Closing HTTP client")
        client.close()
    }

    /**
     * Маскирует API-ключ для логирования (показывает только первые 8 и последние 4 символа).
     */
    private fun maskApiKey(key: String): String {
        return if (key.length > 12) {
            "${key.take(8)}...${key.takeLast(4)}"
        } else {
            "***"
        }
    }
}

/**
 * Одиночный экземпляр (singleton) API-клиента.
 */
val chatApiClient = ChatApiClient()
