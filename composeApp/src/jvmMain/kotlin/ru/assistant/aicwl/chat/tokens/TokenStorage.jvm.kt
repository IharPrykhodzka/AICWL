package ru.assistant.aicwl.chat.tokens

import java.io.File
import java.io.IOException

/**
 * JVM/Desktop реализация TokenStorage с использованием локального файла.
 */
actual class TokenStorage {
    private val statsFile: File by lazy {
        val userHome = System.getProperty("user.home")
        val appDir = File(userHome, ".aicwl")
        if (!appDir.exists()) {
            appDir.mkdirs()
        }
        File(appDir, "token_statistics.json")
    }

    actual suspend fun loadTokenStatistics(): String? {
        return try {
            if (statsFile.exists()) {
                statsFile.readText()
            } else {
                null
            }
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    actual suspend fun saveTokenStatistics(jsonData: String) {
        try {
            statsFile.writeText(jsonData)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    actual suspend fun clearTokenStatistics() {
        try {
            if (statsFile.exists()) {
                statsFile.delete()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}
