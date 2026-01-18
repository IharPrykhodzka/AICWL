package ru.assistant.aicwl.chat.data

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

/**
 * Domain model for serializing chat history to persistent storage.
 * Contains all messages and metadata needed to restore chat state.
 *
 * This is a data transfer object (DTO) specifically for persistence,
 * following Clean Architecture principles.
 */
@Serializable
data class ChatHistoryData(
    val messages: List<SerializableChatMessage>,
    val businessAnalystHistory: List<SerializableInterviewEntry>,
    val fixedTotalQuestions: Int?,
    val timestamp: Long = currentTimeMillis()
) {
    companion object {
        private val json = Json {
            ignoreUnknownKeys = true
            isLenient = true
            encodeDefaults = true
        }

        /**
         * Serializes chat history to JSON string.
         */
        fun serialize(history: ChatHistoryData): String {
            return json.encodeToString(history)
        }

        /**
         * Deserializes chat history from JSON string.
         * Returns empty history if parsing fails.
         */
        fun deserialize(jsonString: String): ChatHistoryData? {
            return try {
                json.decodeFromString<ChatHistoryData>(jsonString)
            } catch (e: Exception) {
                null
            }
        }

        /**
         * Creates empty history.
         */
        fun empty() = ChatHistoryData(
            messages = emptyList(),
            businessAnalystHistory = emptyList(),
            fixedTotalQuestions = null
        )
    }
}

/**
 * Serializable version of EnhancedChatMessage for persistence.
 * Contains only essential data needed to restore messages.
 */
@Serializable
data class SerializableChatMessage(
    val id: String,
    val role: String,  // Serialized as string for MessageRole enum
    val originalContent: String,
    val timestamp: Long,
    val messageType: String,  // Serialized as string for MessageType enum
    val structuredData: String? = null  // Serialized JSON string of StructuredAiResponse
) {
    /**
     * Converts to EnhancedChatMessage domain model.
     */
    fun toDomain(): EnhancedChatMessage {
        return EnhancedChatMessage(
            id = id,
            role = MessageRole.valueOf(role),
            originalContent = originalContent,
            timestamp = timestamp,
            messageType = MessageType.valueOf(messageType),
            structuredData = structuredData?.let { StructuredAiResponse.tryParse(it) }
        )
    }

    companion object {
        /**
         * Creates from EnhancedChatMessage domain model.
         */
        fun fromDomain(message: EnhancedChatMessage): SerializableChatMessage {
            return SerializableChatMessage(
                id = message.id,
                role = message.role.name,
                originalContent = message.originalContent,
                timestamp = message.timestamp,
                messageType = message.messageType.name,
                structuredData = message.structuredData?.let {
                    Json {
                        ignoreUnknownKeys = true
                        isLenient = true
                        encodeDefaults = true
                    }.encodeToString(it)
                }
            )
        }
    }
}

/**
 * Serializable version of InterviewHistoryEntry for persistence.
 */
@Serializable
data class SerializableInterviewEntry(
    val role: String,
    val content: String,
    val questionNumber: Int? = null,
    val totalQuestions: Int? = null
) {
    /**
     * Converts to InterviewHistoryEntry domain model.
     */
    fun toDomain(): InterviewHistoryEntry {
        return InterviewHistoryEntry(
            role = MessageRole.valueOf(role),
            content = content,
            questionNumber = questionNumber,
            totalQuestions = totalQuestions
        )
    }

    companion object {
        /**
         * Creates from InterviewHistoryEntry domain model.
         */
        fun fromDomain(entry: InterviewHistoryEntry): SerializableInterviewEntry {
            return SerializableInterviewEntry(
                role = entry.role.name,
                content = entry.content,
                questionNumber = entry.questionNumber,
                totalQuestions = entry.totalQuestions
            )
        }
    }
}

/**
 * Expect function for getting current time in milliseconds.
 * Platform-specific implementations will be provided.
 */
expect fun currentTimeMillis(): Long
