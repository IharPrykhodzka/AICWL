package ru.assistant.aicwl.chat.prompt

/**
 * Базовый интерфейс для правила промта.
 * Позволяет создавать различные типы правил для системного промта.
 */
interface PromptRule {
    /**
     * Возвращает текст правила для включения в системный промт.
     */
    fun getText(): String

    /**
     * Проверяет, активно ли правило.
     * Можно использовать для условного применения правил.
     */
    fun isEnabled(): Boolean = true
}

/**
 * Простое правило на основе статического текста.
 */
class SimplePromptRule(
    private val text: String,
    private val enabled: Boolean = true
) : PromptRule {
    override fun getText(): String = text
    override fun isEnabled(): Boolean = enabled
}

/**
 * Конфигурация системного промта.
 * Содержит набор правил, которые формируют поведение AI.
 */
object SystemPromptConfig {

    /**
     * Основной системный промт с JSON-форматом.
     * Это всегда первое и главное правило для AI.
     */
    val mainPrompt = """
Ты — универсальный интеллектуальный ассистент. Твоя задача — отвечать на вопросы пользователя максимально полезно.

КРИТИЧЕСКИ ВАЖНО - ФОРМАТ ОТВЕТА:
- ВСЕГДА отвечай ТОЛЬКО чистым JSON
- НЕ используй markdown-блоки (```json или ```)
- НЕ добавляй никакого текста до или после JSON
- Ответ должен начинаться сразу с { и заканчиваться }

Формат ответа (пример):
{
  "content": "Развернутый ответ",
  "highlights": ["Ключевая мысль 1", "Ключевая мысль 2"],
  "suggestions": ["Вопрос 1", "Вопрос 2"],
  "meta": {
    "category": "Тема",
    "confidence": 0.95
  }
}

Правила полей:
- content: Основной развернутый ответ
- highlights: 2-3 главные мысли
- suggestions: 2-3 вопроса для продолжения диалога от лица пользователя к ИИ агенту
- meta.category: Тема ответа
- meta.confidence: Уверенность от 0.0 до 1.0

Отвечай на том же языке, что и пользователь.
""".trimIndent()

    /**
     * Текущий кастомный промпт (загружается из preferences).
     * Если null, используется mainPrompt.
     */
    private var customPrompt: String? = null

    /**
     * Загружает кастомный промпт из preferences.
     * Должен вызываться при инициализации приложения.
     */
    suspend fun loadCustomPrompt(repository: ru.assistant.aicwl.chat.prompt.data.PromptSettingsRepository) {
        refreshFromRepository(repository)
    }

    /**
     * Обновляет состояние SystemPromptConfig из repository.
     * Синхронизирует customPrompt и additionalRules с сохранёнными данными.
     * Должен вызываться после любых изменений в настройках промта.
     *
     * @param repository Repository для загрузки актуальных настроек
     */
    suspend fun refreshFromRepository(repository: ru.assistant.aicwl.chat.prompt.data.PromptSettingsRepository) {
        val settings = repository.getSettings()
        customPrompt = settings.customMainPrompt

        // Загружаем и применяем сохранённые правила
        clearAdditionalRules()
        settings.additionalRules.forEach { ruleData ->
            addRule(SimplePromptRule(ruleData.text, ruleData.enabled))
        }
    }

    /**
     * Устанавливает кастомный промпт.
     */
    fun setCustomPrompt(text: String) {
        customPrompt = if (text.isNotBlank()) text else null
    }

    /**
     * Возвращает текущий основной промпт (кастомный или дефолтный).
     */
    private fun getCurrentMainPrompt(): String =
        if (customPrompt.isNullOrBlank()) mainPrompt else customPrompt!!

    /**
     * Специальный системный промт для режима бизнес-аналитика.
     * Полностью заменяет основной промпт при активации этого режима.
     */
    private val businessAnalystPrompt = """
Ты — опытный аналитик, который помогает пользователям уточнять их запросы. Твоя задача — провести интервью, чтобы лучше понять запрос и дать качественный ответ.

КРИТИЧЕСКИ ВАЖНО - ФОРМАТ ОТВЕТА:
- ВСЕГДА отвечай ТОЛЬКО чистым JSON
- НЕ используй markdown-блоки (```json или ```)
- НЕ добавляй никакого текста до или после JSON
- Ответ должен начинаться сразу с { и заканчиваться }

ОГРАНИЧЕНИЯ:
- Задавай ТОЛЬКО ОДИН вопрос за раз
- Вопрос должен быть кратким (до 60 символов)
- Общий ответ не должен превышать 350 символов

АДАПТИВНОЕ КОЛИЧЕСТВО ВОПРОСОВ:
- Определи тип запроса: ТЕХНИЧЕСКИЙ (приложение, сайт, система) или БЫТОВОЙ (фильм, рецепт, хобби)
- Для ТЕХНИЧЕСКИХ: задай 5 - 7 вопросов
- Для БЫТОВЫХ: задай РОВНО 3 - 5 вопроса
- Определи тип ТОЛЬКО ОДИН РАЗ в начале

КРИТИЧЕСКАЯ ЛОГИКА ВОПРОСОВ:
- questionNumber = номер ТЕКУЩЕГО вопроса который ты задаёшь (1, 2, 3...)
- totalQuestions = общее количество вопросов которое ты планируешь задать
- totalQuestions ОПРЕДЕЛЯЕТСЯ ТОЛЬКО ОДИН РАЗ в первом ответе
- КАТЕГОРИЧЕСКИ ЗАПРЕЩЕНО менять totalQuestions во время интервью
- Задавай вопросы по порядку от 1 до totalQuestions
- ПОСЛЕ получения ответа на последний вопрос → финальный ответ

Формат ответа (пример):
{
  "summary": "Краткий ответ одной фразой",
  "reasoning": "Краткое пояснение логики",
  "questionNumber": 1,
  "totalQuestions": 5,
  "questions": ["Ваш вопрос пользователю"],
  "content": "Развернутый ответ",
  "highlights": ["Ключевая мысль 1"],
  "suggestions": ["Предложение 1"],
  "meta": {
    "category": "Тема",
    "confidence": 0.95
  }
}

Правила полей:
- summary: Максимально короткая суть ответа
- reasoning: Почему ты так решил
- questionNumber: Номер текущего вопроса который ты задаёшь (1, 2, 3...)
- totalQuestions: Общее количество вопросов для составления полной картины
- questions: Массив с ОДНИМ вопросом для пользователя (текст вопроса)
- content: Развернутый ответ или пояснение
- highlights: 2-3 главные мысли
- suggestions: Варианты быстрых ответов на твой вопрос
- meta.category: Тема ответа
- meta.confidence: Уверенность от 0.0 до 1.0

Пример для бытового (3 вопроса):
Вопрос 1: {"questionNumber": 1, "totalQuestions": 3, "questions": ["Какой жанр предпочитаете?"], "suggestions": ["Комедия", "Драма"], ...}
Пользователь отвечает: "Комедия"
Вопрос 2: {"questionNumber": 2, "totalQuestions": 3, "questions": ["Есть любимые актёры?"], ...}
Пользователь отвечает
Вопрос 3: {"questionNumber": 3, "totalQuestions": 3, "questions": ["Какой период - старое или новое?"], ...}
Пользователь отвечает
ФИНАЛ: {"questionNumber": null, "questions": [], "content": "Вот моя рекомендация...", ...}

ВАЖНО:
- ВСЕГДА включай questionNumber и totalQuestions когда задаёшь вопросы
- totalQuestions НЕЛЬЗЯ менять после первого вопроса - это строго зафиксированное число
- Если totalQuestions был 3 в первом вопросе - он ДОЛЖЕН остаться 3 до конца
- В поле questions всегда клади ОДИН вопрос - тот который ты задаёшь
- В suggestions положи 2-4 варианта быстрых ответов
- Финальный ответ: questionNumber = null, questions = пустой массив
""".trimIndent()

    /**
     * Дополнительные правила, которые можно добавить динамически.
     */
    private val additionalRules = mutableListOf<PromptRule>()

    /**
     * Флаг режима бизнес-аналитика. Когда true, используется businessAnalystPrompt вместо mainPrompt.
     */
    private var isBusinessAnalystModeActive = false

    /**
     * Возвращает полный системный промт.
     * В режиме бизнес-аналитика возвращает специализированный промпт.
     */
    fun getSystemPrompt(): String {
        return if (isBusinessAnalystModeActive) {
            // В режиме бизнес-аналитика используем специальный промпт без основного
            businessAnalystPrompt
        } else {
            // Стандартный режим с основным промтом и дополнительными правилами
            buildString {
                append(getCurrentMainPrompt())

                val enabledAdditionalRules = additionalRules.filter { it.isEnabled() }
                if (enabledAdditionalRules.isNotEmpty()) {
                    appendLine()
                    appendLine()
                    appendLine("Дополнительные инструкции:")
                    enabledAdditionalRules.forEachIndexed { index, rule ->
                        appendLine("- ${rule.getText()}")
                    }
                }
            }
        }
    }

    /**
     * Включает режим бизнес-аналитика.
     * В этом режиме используется специализированный системный промпт.
     */
    fun enableBusinessAnalystMode() {
        isBusinessAnalystModeActive = true
        clearAdditionalRules()
    }

    /**
     * Выключает режим бизнес-аналитика.
     * Возвращает стандартный системный промпт.
     */
    fun disableBusinessAnalystMode() {
        isBusinessAnalystModeActive = false
        clearAdditionalRules()
    }

    /**
     * Проверяет, активен ли режим бизнес-аналитика.
     */
    fun isBusinessAnalystMode(): Boolean = isBusinessAnalystModeActive

    /**
     * Добавляет новое правило в конфигурацию.
     * Может использоваться для runtime-добавления правил.
     */
    fun addRule(rule: PromptRule) {
        additionalRules.add(rule)
    }

    /**
     * Удаляет все дополнительные правила.
     */
    fun clearAdditionalRules() {
        additionalRules.clear()
    }

    /**
     * Создаёт новый системный промт с переопределением базовых правил.
     * Полезно для создания специализированных конфигураций.
     *
     * @param customPrompt Кастомный промт
     * @param rules Дополнительные правила
     * @return Строка системного промта
     */
    fun createCustomPrompt(
        customPrompt: String,
        vararg rules: PromptRule
    ): String {
        return buildString {
            append(customPrompt)

            val enabledRules = rules.filter { it.isEnabled() }
            if (enabledRules.isNotEmpty()) {
                appendLine()
                appendLine()
                appendLine("Дополнительные инструкции:")
                enabledRules.forEach { rule ->
                    appendLine("- ${rule.getText()}")
                }
            }
        }
    }
}

/**
 * Предопределённые правила для распространённых сценариев.
 */
object PromptRules {
    /**
     * Правило для режима "только код" - без объяснений.
     */
    fun codeOnly() = SimplePromptRule(
        "Отвечай только кодом без дополнительных объяснений, если пользователь явно не попросил пояснений."
    )

    /**
     * Правило для подробного объяснения.
     */
    fun verbose() = SimplePromptRule(
        "Предоставляй подробные объяснения с примерами для каждого элемента кода."
    )

    /**
     * Правило для использования определённого языка программирования по умолчанию.
     */
    fun preferLanguage(language: String) = SimplePromptRule(
        "По умолчанию используй $language для примеров кода, если пользователь не указал другой язык."
    )

    /**
     * Правило для режима обучения.
     */
    fun educationalMode() = SimplePromptRule(
        "Объясняй концепции так, как будто учишь новичка. Используй аналогии и пошаговые разъяснения."
    )

    /**
     * Правило для режима обзора кода.
     */
    fun codeReviewMode() = SimplePromptRule(
        "При показе кода указывай потенциальные проблемы, улучшения и best practices."
    )

    /**
     * Правило для фокуса на безопасности.
     */
    fun securityFocus() = SimplePromptRule(
        "Уделяй особое внимание безопасности. Указывай потенциальные уязвимости и best practices для защиты."
    )
}

