package ru.assistant.aicwl.chat.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ru.assistant.aicwl.chat.data.ResponseStatus
import ru.assistant.aicwl.chat.data.StructuredAiResponse

/**
 * Карточка со структурированным ответом от AI.
 * Выбирает дизайн в зависимости от режима:
 * - Business Analyst Mode: enhanced design с вопросами, прогрессом, reasoning
 * - Main Mode: минималистичный лаконичный дизайн
 */
@Composable
fun StructuredResponseCard(
    response: StructuredAiResponse,
    modifier: Modifier = Modifier,
    onSuggestionClick: (String) -> Unit = {}
) {
    // Определяем режим: бизнес-аналитик если есть вопросы или summary
    val isBusinessAnalystMode = response.questions.isNotEmpty() ||
            response.questionNumber != null ||
            response.summary.isNotBlank()

    if (isBusinessAnalystMode) {
        EnhancedStructuredResponseCard(
            response = response,
            modifier = modifier,
            onSuggestionClick = onSuggestionClick
        )
    } else {
        MinimalistResponseCard(
            response = response,
            modifier = modifier,
            onSuggestionClick = onSuggestionClick
        )
    }
}

/**
 * Минималистичная карточка ответа для mainPrompt.
 * Скромный дизайн с мягкими цветовыми акцентами.
 */
@Composable
private fun MinimalistResponseCard(
    response: StructuredAiResponse,
    modifier: Modifier = Modifier,
    onSuggestionClick: (String) -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Основной контент с мягким фоном
            if (response.content.isNotBlank()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = response.content,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }

            // Highlights - с цветными маркерами
            if (response.highlights.isNotEmpty()) {
                MinimalistHighlightsSection(highlights = response.highlights)
            }

            // Suggestions - с иконками и цветными фонами
            if (response.suggestions.isNotEmpty()) {
                MinimalistSuggestionsSection(
                    suggestions = response.suggestions,
                    onSuggestionClick = onSuggestionClick
                )
            }
        }
    }
}

/**
 * Улучшенная карточка ответа для businessAnalystPrompt.
 * Полнофункциональный дизайн с прогрессом, вопросами, reasoning.
 */
@Composable
private fun EnhancedStructuredResponseCard(
    response: StructuredAiResponse,
    modifier: Modifier = Modifier,
    onSuggestionClick: (String) -> Unit = {}
) {
    var isContentExpanded by rememberSaveable { mutableStateOf(true) }
    var isHighlightsExpanded by rememberSaveable { mutableStateOf(true) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = when (response.computedStatus) {
                ResponseStatus.SUCCESS -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
                ResponseStatus.ERROR -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
                ResponseStatus.NEEDS_CLARIFICATION -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.1f)
            }
        ),
        border = BorderStroke(
            width = 1.dp,
            color = when (response.computedStatus) {
                ResponseStatus.SUCCESS -> MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                ResponseStatus.ERROR -> MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
                ResponseStatus.NEEDS_CLARIFICATION -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f)
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Улучшенный заголовок со статусом и категорией
            EnhancedStatusHeader(
                status = response.computedStatus,
                category = response.safeMeta.category,
                confidence = response.safeMeta.confidence,
                questionNumber = response.questionNumber,
                totalQuestions = response.totalQuestions,
                summary = response.safeSummary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Questions (уточняющие вопросы от бизнес-аналитика) - приоритетный блок
            if (response.questions.isNotEmpty()) {
                EnhancedQuestionsSection(
                    questions = response.questions,
                    questionNumber = response.questionNumber,
                    totalQuestions = response.totalQuestions,
                    onQuestionClick = onSuggestionClick
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Reasoning (логика) - компактная секция если есть
            if (response.reasoning.isNotBlank()) {
                CompactReasoningSection(
                    reasoning = response.reasoning
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Action Items (список дел) - улучшенная версия
            if (response.actionItems.isNotEmpty()) {
                EnhancedActionItemsSection(items = response.actionItems)
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Content (основной ответ) - expandable секция
            if (response.content.isNotBlank()) {
                ExpandableContentSection(
                    content = response.content,
                    isExpanded = isContentExpanded,
                    onToggle = { isContentExpanded = it },
                    previewLength = 200
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Highlights (ключевые мысли) - expandable секция
            if (response.highlights.isNotEmpty()) {
                ExpandableHighlightsSection(
                    highlights = response.highlights,
                    isExpanded = isHighlightsExpanded,
                    onToggle = { isHighlightsExpanded = it }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }

            // Suggestions (предложения) - улучшенная версия
            if (response.suggestions.isNotEmpty()) {
                EnhancedSuggestionsSection(
                    suggestions = response.suggestions,
                    onSuggestionClick = onSuggestionClick
                )
            }
        }
    }
}

/**
 * Улучшенный заголовок со статусом, категорией и summary.
 */
@Composable
private fun EnhancedStatusHeader(
    status: ResponseStatus,
    category: String,
    confidence: Double,
    questionNumber: Int? = null,
    totalQuestions: Int? = null,
    summary: String = ""
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Summary - основной ответ вверху
        if (summary.isNotBlank()) {
            Text(
                text = summary,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Статус с прогрессом вопросов если есть
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                StatusIcon(status = status)
                Text(
                    text = getStatusText(status),
                    style = MaterialTheme.typography.labelMedium,
                    color = getStatusColor(status),
                    fontWeight = FontWeight.Medium
                )
                // Индикатор прогресса для режима интервью
                if (questionNumber != null && totalQuestions != null) {
                    Text(
                        text = " ($questionNumber/$totalQuestions)",
                        style = MaterialTheme.typography.labelSmall,
                        color = getStatusColor(status).copy(alpha = 0.7f)
                    )
                }
            }

            // Категория и уверенность
            if (category.isNotBlank()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = category,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    ConfidenceBadge(confidence = confidence)
                }
            }
        }

        // Прогресс-бар для режима интервью
        if (questionNumber != null && totalQuestions != null) {
            val progress = questionNumber.toFloat() / totalQuestions.toFloat()
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = getStatusColor(status),
                trackColor = getStatusColor(status).copy(alpha = 0.2f)
            )
        }
    }
}

/**
 * Иконка статуса.
 */
@Composable
private fun StatusIcon(status: ResponseStatus) {
    val (icon, tint) = when (status) {
        ResponseStatus.SUCCESS -> Icons.Default.CheckCircle to MaterialTheme.colorScheme.primary
        ResponseStatus.ERROR -> Icons.Default.Error to MaterialTheme.colorScheme.error
        ResponseStatus.NEEDS_CLARIFICATION -> Icons.Default.Help to MaterialTheme.colorScheme.tertiary
    }

    Icon(
        imageVector = icon,
        contentDescription = null,
        tint = tint,
        modifier = Modifier.size(18.dp)
    )
}

/**
 * Текст статуса.
 */
private fun getStatusText(status: ResponseStatus): String {
    return when (status) {
        ResponseStatus.SUCCESS -> "Успешно"
        ResponseStatus.ERROR -> "Ошибка"
        ResponseStatus.NEEDS_CLARIFICATION -> "Уточнение"
    }
}

/**
 * Цвет статуса.
 */
@Composable
private fun getStatusColor(status: ResponseStatus): Color {
    return when (status) {
        ResponseStatus.SUCCESS -> MaterialTheme.colorScheme.primary
        ResponseStatus.ERROR -> MaterialTheme.colorScheme.error
        ResponseStatus.NEEDS_CLARIFICATION -> MaterialTheme.colorScheme.tertiary
    }
}

/**
 * Бейдж уверенности AI.
 */
@Composable
private fun ConfidenceBadge(confidence: Double) {
    val percentage = (confidence * 100).toInt()
    val color = when {
        confidence >= 0.8 -> Color(0xFF4CAF50)
        confidence >= 0.5 -> Color(0xFFFFC107)
        else -> Color(0xFFF44336)
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = "$percentage%",
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Секция с заголовком и контентом.
 */
@Composable
private fun SectionCard(
    icon: ImageVector? = null,
    title: String? = null,
    content: String,
    color: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            if (title != null) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    color = color,
                    fontWeight = FontWeight.Medium
                )
            }
            Text(
                text = content,
                style = if (title != null)
                    MaterialTheme.typography.bodyMedium
                else
                    MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * Секция с action items (список дел).
 */
@Composable
private fun ActionItemsSection(items: List<String>) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Что сделать:",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Medium
        )

        items.forEachIndexed { index, item ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Checkbox визуал
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(4.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${index + 1}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Text(
                    text = item,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * Секция с ключевыми мыслями (highlights).
 */
@Composable
private fun HighlightsSection(highlights: List<String>) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "Главное:",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.tertiary,
            fontWeight = FontWeight.Medium
        )

        highlights.forEach { highlight ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(MaterialTheme.colorScheme.tertiary)
                    )
                Text(
                    text = highlight,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Секция с предложениями (suggestions).
 */
@Composable
private fun SuggestionsSection(
    suggestions: List<String>,
    onSuggestionClick: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "Далее можно спросить:",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.secondary,
            fontWeight = FontWeight.Medium
        )

        suggestions.forEach { suggestion ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSuggestionClick(suggestion) },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(MaterialTheme.colorScheme.secondary)
                    )
                    Text(
                        text = suggestion,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }
    }
}

/**
 * Секция с уточняющими вопросами от бизнес-аналитика.
 */
@Composable
private fun QuestionsSection(
    questions: List<String>,
    questionNumber: Int? = null,
    totalQuestions: Int? = null,
    onQuestionClick: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Заголовок с прогрессом
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Уточняющие вопросы:",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.tertiary,
                fontWeight = FontWeight.Medium
            )

            // Индикатор прогресса
            if (questionNumber != null && totalQuestions != null) {
                Text(
                    text = "$questionNumber из $totalQuestions",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Normal
                )
            }
        }

        // Прогресс-бар
        if (questionNumber != null && totalQuestions != null) {
            val progress = questionNumber.toFloat() / totalQuestions.toFloat()
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = MaterialTheme.colorScheme.tertiary,
            )
            Spacer(modifier = Modifier.height(4.dp))
        }

        questions.forEachIndexed { index, question ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onQuestionClick(question) },
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
                ),
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    // Номер вопроса (смещенный на основе questionNumber)
                    val displayNumber = if (questionNumber != null) {
                        questionNumber + index
                    } else {
                        index + 1
                    }

                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.tertiary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$displayNumber",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onTertiary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Текст вопроса
                    Text(
                        text = question,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )

                    // Иконка - можно ответить
                    Icon(
                        imageVector = Icons.Default.Help,
                        contentDescription = "Ответить",
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

/**
 * Улучшенная секция с уточняющими вопросами от бизнес-аналитика.
 * Более акцентированный дизайн для приоритетного блока.
 */
@Composable
private fun EnhancedQuestionsSection(
    questions: List<String>,
    questionNumber: Int? = null,
    totalQuestions: Int? = null,
    onQuestionClick: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Заголовок секции
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Help,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = "Уточняющий вопрос",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.tertiary,
                    fontWeight = FontWeight.Bold
                )
            }

            // Индикатор прогресса
            if (questionNumber != null && totalQuestions != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                ) {
                    Text(
                        text = "$questionNumber из $totalQuestions",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onTertiaryContainer,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Вопросы - более акцентированные карточки
        questions.forEach { question ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onQuestionClick(question) },
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f)
                ),
                border = BorderStroke(
                    width = 1.5.dp,
                    color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.5f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    // Иконка вопроса
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.tertiary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Help,
                            contentDescription = "Вопрос",
                            tint = MaterialTheme.colorScheme.onTertiary,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    // Текст вопроса
                    Text(
                        text = question,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.weight(1f)
                    )

                    // Индикатор кликабельности
                    Icon(
                        imageVector = Icons.Default.ArrowRight,
                        contentDescription = "Ответить",
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

/**
 * Компактная секция для отображения логики (reasoning).
 */
@Composable
private fun CompactReasoningSection(
    reasoning: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Top
        ) {
            Icon(
                imageVector = Icons.Default.Lightbulb,
                contentDescription = "Логика",
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(18.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Логика:",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.tertiary,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = reasoning,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Улучшенная секция с action items (список дел).
 */
@Composable
private fun EnhancedActionItemsSection(items: List<String>) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Заголовок
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.TaskAlt,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "Что нужно сделать:",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
        }

        // Action items
        items.forEachIndexed { index, item ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    // Чекбокс с номером
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "${index + 1}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Текст
                    Text(
                        text = item,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

/**
 * Expandable секция для длинного контента.
 */
@Composable
private fun ExpandableContentSection(
    content: String,
    isExpanded: Boolean,
    onToggle: (Boolean) -> Unit,
    previewLength: Int = 200
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Заголовок с кнопкой toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggle(!isExpanded) },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Подробный ответ",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
            }

            Icon(
                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (isExpanded) "Свернуть" else "Развернуть",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }

        // Контент (с анимацией)
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ) {
                Text(
                    text = content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(14.dp)
                )
            }
        }

        // Preview если свернуто
        if (!isExpanded) {
            Text(
                text = content.take(previewLength) + if (content.length > previewLength) "..." else "",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }
    }
}

/**
 * Expandable секция для highlights.
 */
@Composable
private fun ExpandableHighlightsSection(
    highlights: List<String>,
    isExpanded: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Заголовок с кнопкой toggle
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggle(!isExpanded) },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Lightbulb,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "Главное (${highlights.size})",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.tertiary,
                    fontWeight = FontWeight.Bold
                )
            }

            Icon(
                imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (isExpanded) "Свернуть" else "Развернуть",
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(20.dp)
            )
        }

        // Highlights (с анимацией)
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically() + fadeIn(),
            exit = shrinkVertically() + fadeOut()
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                highlights.forEach { highlight ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.tertiary)
                                .align(Alignment.CenterVertically)
                        )
                        Text(
                            text = highlight,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Улучшенная секция с предложениями (suggestions).
 */
@Composable
private fun EnhancedSuggestionsSection(
    suggestions: List<String>,
    onSuggestionClick: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Заголовок
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Продолжить диалог:",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.Bold
            )
        }

        // Suggestions в виде чипов
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            suggestions.forEach { suggestion ->
                SuggestionChip(
                    onClick = { onSuggestionClick(suggestion) },
                    label = {
                        Text(
                            text = suggestion,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                        labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

// ==================== MINIMALIST COMPONENTS ====================

/**
 * Минималистичная секция highlights для mainPrompt.
 * С цветными маркерами и мягким градиентным фоном.
 */
@Composable
private fun MinimalistHighlightsSection(
    highlights: List<String>
) {
    // Цвета для маркеров - градиент от tertiary к primary
    val markerColors = listOf(
        MaterialTheme.colorScheme.tertiary,
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Заголовок секции с иконкой
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(bottom = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Lightbulb,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.size(16.dp)
            )
            Text(
                text = "Главное:",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.tertiary,
                fontWeight = FontWeight.Medium
            )
        }

        // Highlights с цветными маркерами
        highlights.forEachIndexed { index, highlight ->
            val markerColor = markerColors[index % markerColors.size]

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Цветной маркер - скругленный прямоугольник
                Box(
                    modifier = Modifier
                        .size(6.dp, 6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(markerColor)
                        .align(Alignment.CenterVertically)
                )
                Text(
                    text = highlight,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * Минималистичная секция suggestions для mainPrompt.
 * С иконками и мягкими цветовыми фонами.
 */
@Composable
private fun MinimalistSuggestionsSection(
    suggestions: List<String>,
    onSuggestionClick: (String) -> Unit
) {
    // Цвета для suggestions - вариации на основе secondary
    val suggestionColors = listOf(
        MaterialTheme.colorScheme.secondaryContainer,
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
        MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f)
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Заголовок секции с иконкой
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(bottom = 4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier
                    .size(16.dp)
                    .rotate(-90f)
            )
            Text(
                text = "Продолжить:",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.secondary,
                fontWeight = FontWeight.Medium
            )
        }

        // Suggestions как интерактивные карточки
        Column(
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            suggestions.forEachIndexed { index, suggestion ->
                val backgroundColor = suggestionColors[index % suggestionColors.size]
                val borderColor = when (index % 3) {
                    0 -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.3f)
                    1 -> MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                    else -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f)
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSuggestionClick(suggestion) },
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = backgroundColor
                    ),
                    border = BorderStroke(
                        width = 1.dp,
                        color = borderColor
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Иконка в зависимости от индекса
                        val icon = when (index % 3) {
                            0 -> Icons.Default.ArrowRight
                            1 -> Icons.Default.Help
                            else -> Icons.Default.Lightbulb
                        }
                        val iconTint = when (index % 3) {
                            0 -> MaterialTheme.colorScheme.secondary
                            1 -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.tertiary
                        }

                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = iconTint,
                            modifier = Modifier.size(16.dp)
                        )

                        Text(
                            text = suggestion,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}
