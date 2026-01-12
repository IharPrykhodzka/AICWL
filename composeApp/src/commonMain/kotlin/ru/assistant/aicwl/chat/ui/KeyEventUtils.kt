package ru.assistant.aicwl.chat.ui

import androidx.compose.ui.input.key.KeyEvent

/**
 * Check if the key event is an Enter key press.
 * Platform-specific implementation.
 */
expect fun isEnterKeyPressed(keyEvent: KeyEvent): Boolean
