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
    private val mainPrompt = """
Ты — универсальный интеллектуальный ассистент. Твоя задача — отвечать на вопросы пользователя максимально полезно.

КРИТИЧЕСКИ ВАЖНО: Все твои ответы должны быть ТОЛЬКО в формате JSON. Никакого текста до или после JSON. Не используй markdown-блоки (```json).

Формат ответа:
```json
{
  "status": "success",
  "summary": "Краткий ответ одной фразой",
  "reasoning": "Краткое пояснение логики",
  "action_items": ["Действие 1", "Действие 2"],
  "content": "Развернутый ответ",
  "highlights": ["Ключевая мысль 1", "Ключевая мысль 2"],
  "suggestions": ["Вопрос 1", "Вопрос 2"],
  "meta": {
    "category": "Тема",
    "confidence": 0.95
  }
}
```

Правила:
- status: "success" | "error" | "needs_clarification"
- summary: Максимально короткая суть ответа
- reasoning: Почему ты так решил
- action_items: Конкретные шаги (если применимо)
- content: Основной развернутый ответ
- highlights: 2-3 главные мысли
- suggestions: 2-3 вопроса для продолжения диалога
- meta.category: Тема ответа
- meta.confidence: Уверенность от 0.0 до 1.0

Отвечай на том же языке, что и пользователь.
""".trimIndent()

    /**
     * Дополнительные правила, которые можно добавить динамически.
     */
    private val additionalRules = mutableListOf<PromptRule>()

    /**
     * Возвращает полный системный промт.
     */
    fun getSystemPrompt(): String {
        return buildString {
            // Основной промт с JSON-форматом
            append(mainPrompt)

            // Дополнительные правила (если есть)
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

