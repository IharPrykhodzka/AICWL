package ru.assistant.aicwl.chat.ui

import androidx.compose.ui.input.key.KeyEvent

actual fun isEnterKeyPressed(keyEvent: KeyEvent): Boolean {
    // На iOS ввод с клавиатуры обрабатывается по-другому.
    // Обычно используется специальная кнопка "Send" на клавиатуре,
    // а не сочетания клавиш Enter как на Desktop/Android.
    // Эта реализация проверяет метку клавиши как запасной вариант.
    val keyLabel = keyEvent.toString()
    return keyLabel.contains("Enter", ignoreCase = true) ||
           keyLabel.contains("Return", ignoreCase = true)
}
