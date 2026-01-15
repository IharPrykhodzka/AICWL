package ru.assistant.aicwl.chat.utils

/**
 * Кроссплатформенные утилиты для работы со временем.
 */
expect fun currentTimeMillis(): Long

/**
 * Кроссплатформенные утилиты для работы с буфером обмена.
 */
expect fun getClipboardManager(): ClipboardManager

/**
 * Интерфейс менеджера буфера обмена.
 * Реализация зависит от платформы.
 */
interface ClipboardManager {
    /**
     * Копирует текст в буфер обмена.
     */
    fun setText(text: String)

    /**
     * Получает текст из буфера обмена.
     */
    fun getText(): String?

    /**
     * Проверяет, есть ли текст в буфере обмена.
     */
    fun hasText(): Boolean
}
