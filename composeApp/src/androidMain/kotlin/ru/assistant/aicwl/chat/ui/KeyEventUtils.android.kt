package ru.assistant.aicwl.chat.ui

import androidx.compose.ui.input.key.KeyEvent
import android.view.KeyEvent as AndroidKeyEvent

actual fun isEnterKeyPressed(keyEvent: KeyEvent): Boolean {
    // Use nativeKeyEvent to check for Enter key
    val nativeEvent = keyEvent.nativeKeyEvent
    if (nativeEvent is AndroidKeyEvent) {
        return nativeEvent.keyCode == AndroidKeyEvent.KEYCODE_ENTER ||
               nativeEvent.keyCode == AndroidKeyEvent.KEYCODE_NUMPAD_ENTER
    }
    return false
}
