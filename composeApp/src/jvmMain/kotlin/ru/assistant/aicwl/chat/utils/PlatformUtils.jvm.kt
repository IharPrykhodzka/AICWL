package ru.assistant.aicwl.chat.utils

import java.awt.Toolkit
import java.awt.datatransfer.StringSelection

actual fun currentTimeMillis(): Long = System.currentTimeMillis()

/**
 * Создает менеджер буфера обмена для JVM.
 */
actual fun getClipboardManager(): ClipboardManager {
    return JvmClipboardManagerImpl()
}

/**
 * JVM реализация буфера обмена.
 */
private class JvmClipboardManagerImpl : ClipboardManager {
    private val clipboard = Toolkit.getDefaultToolkit().systemClipboard

    override fun setText(text: String) {
        val selection = StringSelection(text)
        clipboard.setContents(selection, selection)
    }

    override fun getText(): String? {
        val contents = clipboard.getContents(null)
        return if (contents?.isDataFlavorSupported(java.awt.datatransfer.DataFlavor.stringFlavor) == true) {
            contents.getTransferData(java.awt.datatransfer.DataFlavor.stringFlavor)?.toString()
        } else {
            null
        }
    }

    override fun hasText(): Boolean {
        val contents = clipboard.getContents(null)
        return contents?.isDataFlavorSupported(java.awt.datatransfer.DataFlavor.stringFlavor) == true
    }
}
