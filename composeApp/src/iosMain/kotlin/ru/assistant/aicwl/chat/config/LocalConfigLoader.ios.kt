package ru.assistant.aicwl.chat.config

import platform.Foundation.*
import kotlinx.cinterop.ExperimentalForeignApi

/**
 * Load API key from local file in the project bundle.
 * The file should be added to Xcode project but NOT committed to git.
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
