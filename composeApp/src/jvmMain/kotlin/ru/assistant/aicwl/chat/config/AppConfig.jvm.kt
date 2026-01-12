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

            // Try to find config.properties by searching upward from current directory
            val configFile = findConfigFile() ?: return null

            configFile.inputStream().use { props.load(it) }
            props.getProperty(key)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Search for config.properties in current directory and parent directories.
     * This is needed because Desktop app runs from build/ directory.
     */
    private fun findConfigFile(): java.io.File? {
        // Try multiple starting points
        val searchPaths = listOf(
            java.io.File(".").absoluteFile,  // Current directory
            java.io.File("").absoluteFile,   // Working directory
            java.io.File(System.getProperty("user.dir")),  // Explicit user.dir
            java.io.File(System.getProperty("user.home"))  // User home directory
        )

        for (startDir in searchPaths) {
            var currentDir = startDir
            println("[AppConfig] Searching from: ${currentDir.absolutePath}")

            // Search up to 8 levels up from this starting point
            repeat(8) {
                val configFile = java.io.File(currentDir, "config.properties")
                if (configFile.exists()) {
                    println("[AppConfig] Found config file at: ${configFile.absolutePath}")
                    return configFile
                }
                val parent = currentDir.parentFile
                if (parent == null) return@repeat
                currentDir = parent
            }
        }

        // Also try checking if project root can be found via common markers
        var checkDir = java.io.File(".").absoluteFile
        repeat(10) {
            if (java.io.File(checkDir, "build.gradle.kts").exists() ||
                java.io.File(checkDir, "settings.gradle.kts").exists()) {
                val configFile = java.io.File(checkDir, "config.properties")
                if (configFile.exists()) {
                    println("[AppConfig] Found config file via gradle marker at: ${configFile.absolutePath}")
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
