package ru.assistant.aicwl.chat.config

import platform.Foundation.*
import kotlinx.cinterop.ExperimentalForeignApi

/**
 * Загружает API-ключ из локального файла в бандле проекта.
 * Файл должен быть добавлен в Xcode проект, но НЕ должен быть закоммичен в git.
 */
object LocalConfigLoader {
    @OptIn(ExperimentalForeignApi::class)
    fun loadApiKeyFromFile(): String? {
        return try {
            val path = NSBundle.mainBundle.pathForResource("local_config", "txt")
            if (path != null) {
                val content = NSString.stringWithContentsOfFile(path, NSUTF8StringEncoding, null)
                content?.toString()?.trim()?.takeIf { it.isNotEmpty() && !it.startsWith("your-") }
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}
