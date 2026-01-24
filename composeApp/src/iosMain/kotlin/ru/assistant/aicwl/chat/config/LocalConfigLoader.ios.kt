package ru.assistant.aicwl.chat.config

import platform.Foundation.*
import kotlinx.cinterop.ExperimentalForeignApi

/**
 * Загружает API-ключи из локального файла в бандле проекта.
 * Файл должен быть добавлен в Xcode проект, но НЕ должен быть закоммичен в git.
 *
 * Формат local_config.txt:
 * ZAI_API_KEY=your_zai_key_here
 */
object LocalConfigLoader {
    @OptIn(ExperimentalForeignApi::class)
    private fun loadConfigFile(): Map<String, String>? {
        return try {
            val path = NSBundle.mainBundle.pathForResource("local_config", "txt")
            if (path != null) {
                val content = NSString.stringWithContentsOfFile(path, NSUTF8StringEncoding, null)
                content?.toString()?.parseConfigFile()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    fun loadApiKeyFromFile(): String? {
        return loadConfigFile()?.get("ZAI_API_KEY")
    }
}

/**
 * Парсит содержимое конфигурационного файла в формате KEY=VALUE
 */
private fun String.parseConfigFile(): Map<String, String> {
    return lines()
        .map { it.trim() }
        .filter { it.isNotEmpty() && !it.startsWith("#") }
        .mapNotNull { line ->
            val separatorIndex = line.indexOf('=')
            if (separatorIndex > 0) {
                val key = line.substring(0, separatorIndex).trim()
                val value = line.substring(separatorIndex + 1).trim()
                if (value.isNotEmpty() && !value.startsWith("your-")) {
                    key to value
                } else {
                    null
                }
            } else {
                null
            }
        }
        .toMap()
}
