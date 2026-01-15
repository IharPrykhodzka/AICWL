package ru.assistant.aicwl.chat.utils

import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970
import platform.UIKit.UIPasteboard

actual fun currentTimeMillis(): Long = (NSDate().timeIntervalSince1970 * 1000).toLong()

/**
 * Создает менеджер буфера обмена для iOS.
 */
actual fun getClipboardManager(): ClipboardManager {
    return IosClipboardManagerImpl()
}

/**
 * iOS реализация буфера обмена.
 */
private class IosClipboardManagerImpl : ClipboardManager {
    private val pasteboard: UIPasteboard
        get() = UIPasteboard.generalPasteboard

    override fun setText(text: String) {
        pasteboard.string = text
    }

    override fun getText(): String? {
        return pasteboard.string
    }

    override fun hasText(): Boolean {
        return pasteboard.hasStrings
    }
}
