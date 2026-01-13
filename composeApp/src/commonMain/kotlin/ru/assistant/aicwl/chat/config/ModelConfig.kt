package ru.assistant.aicwl.chat.config

/**
 * Константы конфигурации для доступных AI-моделей.
 * Каждая модель имеет разные возможности и производительность.
 */
object ModelConfig {
    /**
     * Самая мощная модель - самая медленная, но с ответами самого высокого качества.
     * Используйте для сложных рассуждений, генерации кода и детального анализа.
     */
    const val GLM_MODEL_SENIOR = "glm-4.7"

    /**
     * Сбалансированная модель - хорошее качество с разумной скоростью.
     * Рекомендуемая модель по умолчанию для большинства случаев.
     */
    const val GLM_MODEL_MIDDLE = "glm-4.6"

    /**
     * Самая быстрая модель - более низкое качество, но самые быстрые ответы.
     * Используйте для простых запросов, быстрых сводок и когда скорость важнее всего.
     */
    const val GLM_MODEL_JUNIOR = "glm-4.5-air"

    /**
     * Модель по умолчанию, если пользователь не сделал выбор.
     */
    const val DEFAULT_MODEL = GLM_MODEL_JUNIOR

    /**
     * Все доступные модели в порядке убывания возможностей.
     */
    val ALL_MODELS = listOf(
        GLM_MODEL_SENIOR,
        GLM_MODEL_MIDDLE,
        GLM_MODEL_JUNIOR
    )

    /**
     * Отображаемые имена моделей (для UI).
     */
    val MODEL_DISPLAY_NAMES = mapOf(
        GLM_MODEL_SENIOR to "Senior (GLM-4.7) - Most Powerful",
        GLM_MODEL_MIDDLE to "Middle (GLM-4.6) - Balanced",
        GLM_MODEL_JUNIOR to "Junior (GLM-4.5-Air) - Fastest"
    )

    /**
     * Возвращает отображаемое имя для идентификатора модели.
     */
    fun getDisplayName(modelId: String): String {
        return MODEL_DISPLAY_NAMES[modelId] ?: modelId
    }
}
