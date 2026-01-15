package ru.assistant.aicwl.chat.utils

import android.content.ClipData
import android.content.ClipboardManager as AndroidClipboardManager
import android.content.Context

actual fun currentTimeMillis(): Long = System.currentTimeMillis()

/**
 * Возвращает системный контекст приложения.
 * Требуется для доступа к ClipboardManager.
 */
private lateinit var appContext: Context

/**
 * Инициализирует контекст приложения. Должен быть вызван в Application.onCreate().
 */
fun initAppContext(context: Context) {
    appContext = context
}

/**
 * Создает менеджер буфера обмена для Android.
 */
actual fun getClipboardManager(): ClipboardManager {
    return AndroidClipboardManagerImpl()
}

/**
 * Android реализация буфера обмена.
 */
private class AndroidClipboardManagerImpl : ClipboardManager {
    private val systemClipboard: AndroidClipboardManager?
        get() = appContext.getSystemService(Context.CLIPBOARD_SERVICE) as? AndroidClipboardManager

    override fun setText(text: String) {
        val clip = ClipData.newPlainText("copied_text", text)
        systemClipboard?.setPrimaryClip(clip)
    }

    override fun getText(): String? {
        val clip = systemClipboard?.primaryClip
        if (clip != null && clip.itemCount > 0) {
            return clip.getItemAt(0).text?.toString()
        }
        return null
    }

    override fun hasText(): Boolean {
        return systemClipboard?.hasPrimaryClip() == true &&
                systemClipboard?.primaryClipDescription?.hasMimeType(
                    android.content.ClipDescription.MIMETYPE_TEXT_PLAIN
                ) == true
    }
}
