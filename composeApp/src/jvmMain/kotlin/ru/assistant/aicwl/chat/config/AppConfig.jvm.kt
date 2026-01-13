package ru.assistant.aicwl.chat.config

actual object AppConfig {
    actual val zApiKey: String by lazy {
        loadConfigProperty("llm.z.api.key") ?: error(
            "API Key not found! Please set llm.z.api.key in config.properties. " +
            "Create config.properties in the project root with your API key."
        )
    }

    actual val zApiEndpoint: String by lazy {
        loadConfigProperty("llm.z.api.endpoint") ?: "https://api.z.ai/api/coding/paas/v4/chat/completions"
    }

    private fun loadConfigProperty(key: String): String? {
        return try {
            val props = java.util.Properties()

            // Пытаемся найти config.properties, выполняя поиск вверх от текущей директории
            val configFile = findConfigFile() ?: return null

            configFile.inputStream().use { props.load(it) }
            props.getProperty(key)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Ищет config.properties в текущей директории и родительских директориях.
     * Это необходимо, поскольку Desktop-приложение запускается из директории build/.
     */
    private fun findConfigFile(): java.io.File? {
        // Пробуем несколько начальных точек
        val searchPaths = listOf(
            java.io.File(".").absoluteFile,  // Текущая директория
            java.io.File("").absoluteFile,   // Рабочая директория
            java.io.File(System.getProperty("user.dir")),  // Явный user.dir
            java.io.File(System.getProperty("user.home"))  // Домашняя директория пользователя
        )

        for (startDir in searchPaths) {
            var currentDir = startDir
            println("[AppConfig] Поиск от: ${currentDir.absolutePath}")

            // Поднимаемся до 8 уровней вверх от этой начальной точки
            repeat(8) {
                val configFile = java.io.File(currentDir, "config.properties")
                if (configFile.exists()) {
                    println("[AppConfig] Файл конфигурации найден: ${configFile.absolutePath}")
                    return configFile
                }
                val parent = currentDir.parentFile
                if (parent == null) return@repeat
                currentDir = parent
            }
        }

        // Также пробуем найти корень проекта по общим маркерам
        var checkDir = java.io.File(".").absoluteFile
        repeat(10) {
            if (java.io.File(checkDir, "build.gradle.kts").exists() ||
                java.io.File(checkDir, "settings.gradle.kts").exists()) {
                val configFile = java.io.File(checkDir, "config.properties")
                if (configFile.exists()) {
                    println("[AppConfig] Файл конфигурации найден через gradle-маркер: ${configFile.absolutePath}")
                    return configFile
                }
            }
            val parent = checkDir.parentFile
            if (parent == null) return@repeat
            checkDir = parent
        }

        return null
    }
}
