package ru.assistant.aicwl.chat.ui

import androidx.compose.ui.input.key.KeyEvent
import java.awt.event.KeyEvent as AwtKeyEvent

actual fun isEnterKeyPressed(keyEvent: KeyEvent): Boolean {
    // On JVM/Desktop, use nativeKeyEvent to check for Enter key
    val nativeEvent = keyEvent.nativeKeyEvent
    if (nativeEvent is AwtKeyEvent) {
        return nativeEvent.keyCode == AwtKeyEvent.VK_ENTER
    }
    // Fallback: check if the key description contains "Enter"
    return keyEvent.toString().contains("Enter", ignoreCase = true)
}
