package ru.assistant.aicwl.chat.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.assistant.aicwl.chat.data.ResponseStatus
import ru.assistant.aicwl.chat.data.StructuredAiResponse

/**
 * Карточка со структурированным ответом от AI.
 */
@Composable
fun StructuredResponseCard(
    response: StructuredAiResponse,
    modifier: Modifier = Modifier,
    onSuggestionClick: (String) -> Unit = {}
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Статус и категория
            StatusHeader(
                status = response.computedStatus,
                category = response.safeMeta.category,
                confidence = response.safeMeta.confidence,
                questionNumber = response.questionNumber,
                totalQuestions = response.totalQuestions
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Summary (краткий ответ)
            SectionCard(
                icon = Icons.Default.TaskAlt,
                title = "Суть",
                content = response.safeSummary,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Reasoning (логика)
            if (response.reasoning.isNotBlank()) {
                SectionCard(
                    icon = Icons.Default.Lightbulb,
                    title = "Логика",
                    content = response.reasoning,
                    color = MaterialTheme.colorScheme.tertiary
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Questions (уточняющие вопросы от бизнес-аналитика)
            if (response.questions.isNotEmpty()) {
                QuestionsSection(
                    questions = response.questions,
                    questionNumber = response.questionNumber,
                    totalQuestions = response.totalQuestions,
                    onQuestionClick = onSuggestionClick
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Action Items (список дел)
            if (response.actionItems.isNotEmpty()) {
                ActionItemsSection(items = response.actionItems)
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Content (основной ответ)
            if (response.content.isNotBlank()) {
                SectionCard(
                    icon = null,
                    title = null,
                    content = response.content,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Highlights (ключевые мысли)
            if (response.highlights.isNotEmpty()) {
                HighlightsSection(highlights = response.highlights)
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Suggestions (предложения)
            if (response.suggestions.isNotEmpty()) {
                SuggestionsSection(
                    suggestions = response.suggestions,
                    onSuggestionClick = onSuggestionClick
                )
            }
        }
    }
}

/**
 * Заголовок со статусом и категорией.
 */
@Composable
private fun StatusHeader(
    status: ResponseStatus,
    category: String,
    confidence: Double,
    questionNumber: Int? = null,
    totalQuestions: Int? = null
) {
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
