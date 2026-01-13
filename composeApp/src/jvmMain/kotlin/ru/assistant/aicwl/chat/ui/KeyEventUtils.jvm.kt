package ru.assistant.aicwl.chat.ui

import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.isShiftPressed
import java.awt.event.KeyEvent as AwtKeyEvent

/**
 * Проверяет, является ли событие нажатием клавиши Enter на Desktop/JVM.
 * Поддерживает как основной Enter, так и Numpad Enter.
 */
actual fun isEnterKeyPressed(keyEvent: KeyEvent): Boolean {
    // Способ 1: Проверка через Key enum (Compose для Desktop)
    val key = keyEvent.key
    if (key == Key.Enter || key == Key.NumPadEnter) {
        return true
    }

    // Способ 2: Проверка через nativeKeyEvent (AWT)
    val nativeEvent = keyEvent.nativeKeyEvent
    if (nativeEvent is AwtKeyEvent) {
        return nativeEvent.keyCode == AwtKeyEvent.VK_ENTER ||
               nativeEvent.keyCode == 10  // Код клавиши Enter
    }

    return false
}
