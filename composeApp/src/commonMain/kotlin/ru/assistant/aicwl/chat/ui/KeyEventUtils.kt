package ru.assistant.aicwl.chat.ui

import androidx.compose.ui.input.key.KeyEvent

/**
 * Проверяет, является ли событие клавиши нажатием Enter.
 * Платформенно-зависимая реализация.
 */
expect fun isEnterKeyPressed(keyEvent: KeyEvent): Boolean
