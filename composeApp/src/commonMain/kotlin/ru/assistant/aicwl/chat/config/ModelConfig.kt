package ru.assistant.aicwl.chat.config

import ru.assistant.aicwl.chat.data.ChatRequestParameters

/**
 * Профили температуры для генерации ответов AI.
 *
 * Каждый профиль определяет баланс между креативностью и детерминизмом ответов.
 */
enum class TemperatureProfile(
    val displayName: String,
    val description: String,
    val parameters: ChatRequestParameters
) {
    /**
     * Экстремально фантазийный профиль - максимальная креативность.
     *
     * Экстремально высокая температура (1.9) с nucleus sampling (0.9).
     * Генерирует максимально неожиданные и нестандартные ответы.
     *
     * Применение:
     * - Генерация альтернативных сценариев и "безумных" идей
     * - Творческие эксперименты и фантастика
     * - Brainstorming с максимальным разнообразием
     */
    FANTASY_PLUS(
        displayName = "Fantasy++",
        description = "Экстремальная креативность (T=1.9)",
        parameters = ChatRequestParameters.fantasyPlus()
    ),

    /**
     * Фантазийный профиль - очень высокая креативность.
     *
     * Высокая температура (1.2), разнообразные и непредсказуемые ответы.
     *
     * Применение:
     * - Творческое письмо и генерация историй
     * - Brainstorming и поиск нестандартных решений
     * - Эксперименты с альтернативными идеями
     */
    FANTASY(
        displayName = "Fantasy",
        description = "Высокая креативность (T=1.2)",
        parameters = ChatRequestParameters.fantasy()
    ),

    /**
     * Творческий профиль - повышенная креативность.
     *
     * Высокая температура (0.8), подходит для генерации идей и вариантов решений.
     *
     * Применение:
     * - Генерация идей и концепций
     * - Поиск вариативных решений
     * - Креативные задачи с сохранением связности
     */
    CREATIVE(
        displayName = "Creative",
        description = "Творческий режим (T=0.8)",
        parameters = ChatRequestParameters.creative()
    ),

    /**
     * Сбалансированный профиль - баланс между креативностью и точностью.
     *
     * Средняя температура (0.5), подходит для большинства задач.
     *
     * Применение:
     * - Повседневные задачи
     * - Деловая переписка
     * - Общие вопросы и ответы
     */
    BALANCED(
        displayName = "Balanced",
        description = "Сбалансированный (T=0.5)",
        parameters = ChatRequestParameters.balanced()
    ),

    /**
     * Технический профиль - высокая точность и детерминизм.
     *
     * Низкая температура (0.2), подходит для кода и технической документации.
     *
     * Применение:
     * - Написание и отладка кода
     * - Техническая документация
     * - Фактическая информация и точные данные
     */
    TECHNICAL(
        displayName = "Technical",
        description = "Технический (T=0.2)",
        parameters = ChatRequestParameters.technical()
    ),

    /**
     * Быстрый профиль - минимальная температура для быстрых ответов.
     *
     * Температура (0.0), короткие и лаконичные ответы без режима мышления.
     *
     * Применение:
     * - Простые вопросы фактического характера
     * - Быстрые справки
     * - Операции, где скорость важнее качества
     */
    FAST(
        displayName = "Fast",
        description = "Быстрый, без thinking (T=0.0)",
        parameters = ChatRequestParameters.fast()
    );

    companion object {
        /**
         * Профиль по умолчанию.
         */
        val DEFAULT = BALANCED

        /**
         * Возвращает профиль по его названию.
         */
        fun fromDisplayName(name: String): TemperatureProfile {
            return entries.find { it.displayName == name } ?: DEFAULT
        }
    }
}

/**
 * Константы конфигурации для доступных AI-моделей.
 * Каждая модель имеет разные возможности и производительность.
 *
 * Также содержит рекомендуемые параметры для каждой модели.
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
    const val GLM_MODEL_MIDDLE = "glm-4.7-flash"

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
        GLM_MODEL_MIDDLE to "Middle (GLM-4.7-Flash) - Balanced",
        GLM_MODEL_JUNIOR to "Junior (GLM-4.5-Air) - Fastest"
    )

    /**
     * Рекомендуемые параметры генерации для каждой модели.
     *
     * Эти предустановки оптимизированы для достижения наилучшего баланса
     * между качеством, скоростью и разнообразием ответов для каждой модели.
     */
    val RECOMMENDED_PARAMETERS = mapOf(
        GLM_MODEL_SENIOR to ChatRequestParameters.technical().copy(
            maxTokens = 4096,
            temperature = 0.3f
        ),
        GLM_MODEL_MIDDLE to ChatRequestParameters.balanced().copy(
            maxTokens = 2048,
            temperature = 0.5f
        ),
        GLM_MODEL_JUNIOR to ChatRequestParameters.fast().copy(
            maxTokens = 1024,
            temperature = 0.2f
        )
    )

    /**
     * Возвращает отображаемое имя для идентификатора модели.
     */
    fun getDisplayName(modelId: String): String {
        return MODEL_DISPLAY_NAMES[modelId] ?: modelId
    }

    /**
     * Возвращает рекомендуемые параметры для указанной модели.
     *
     * Если для модели нет рекомендуемых параметров, возвращает сбалансированную конфигурацию.
     *
     * @param modelId Идентификатор модели
     * @return Рекомендуемые параметры генерации
     */
    fun getRecommendedParameters(modelId: String): ChatRequestParameters {
        return RECOMMENDED_PARAMETERS[modelId] ?: ChatRequestParameters.balanced()
    }

    /**
     * Проверяет, поддерживает ли модель режим мышления (Chain-of-Thought).
     *
     * @param modelId Идентификатор модели
     * @return true, если модель поддерживает режим мышления
     */
    fun supportsThinking(modelId: String): Boolean {
        return modelId in listOf(GLM_MODEL_SENIOR, GLM_MODEL_MIDDLE, GLM_MODEL_JUNIOR)
    }

    /**
     * Возвращает максимальное количество токенов для указанной модели.
     *
     * @param modelId Идентификатор модели
     * @return Максимальное количество токенов или null, если модель неизвестна
     */
    fun getMaxTokens(modelId: String): Int? {
        return when (modelId) {
            GLM_MODEL_SENIOR -> 131072
            GLM_MODEL_MIDDLE -> 131072
            GLM_MODEL_JUNIOR -> 98304
            else -> null
        }
    }
}
