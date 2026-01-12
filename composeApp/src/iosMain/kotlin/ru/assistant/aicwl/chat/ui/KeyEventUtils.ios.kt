package ru.assistant.aicwl.chat.ui

import androidx.compose.ui.input.key.KeyEvent

actual fun isEnterKeyPressed(keyEvent: KeyEvent): Boolean {
    // On iOS, keyboard input is handled differently.
    // iOS typically uses a dedicated "Send" button on the keyboard,
    // not Enter key shortcuts like Desktop/Android.
    // This implementation checks the key label as a fallback.
    val keyLabel = keyEvent.toString()
    return keyLabel.contains("Enter", ignoreCase = true) ||
           keyLabel.contains("Return", ignoreCase = true)
}
