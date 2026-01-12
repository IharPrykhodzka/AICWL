package ru.assistant.aicwl.chat.agent

import ru.assistant.aicwl.chat.network.chatApiClient
import ru.assistant.aicwl.chat.utils.createLogger

/**
 * Simple Chat Agent that communicates with Z.AI API.
 * Supports dynamic model switching based on user selection.
 */
class ChatAgent {
    private val logger = createLogger("ChatAgent")

    /**
     * Send a message to the AI using the specified model.
     *
     * @param message User's message
     * @param modelId Model to use for generating response
     * @return AI's response text or error message
     */
    suspend fun chat(message: String, modelId: String): String {
        logger.i("Chat requested. Model: $modelId")
        logger.d("User message length: ${message.length}")

        val result = chatApiClient.sendUserMessage(modelId, message)

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

                // Provide user-friendly error messages
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
     * Send a message with conversation history for context.
     *
     * @param message User's message
     * @param modelId Model to use
     * @param conversationHistory Previous messages for context
     * @return AI's response text
     */
    suspend fun chatWithHistory(
        message: String,
        modelId: String,
        conversationHistory: List<String>
    ): String {
        logger.i("Chat with history. Model: $modelId, History size: ${conversationHistory.size}")

        val contextMessage = buildString {
            if (conversationHistory.isNotEmpty()) {
                append("Previous conversation:\n")
                conversationHistory.forEach { append("  - $it\n") }
                append("\nCurrent message: ")
            }
            append(message)
        }

        logger.d("Context message length: ${contextMessage.length}")

        return chat(contextMessage, modelId)
    }
}

/**
 * Singleton instance of the ChatAgent.
 */
val chatAgent = ChatAgent()
