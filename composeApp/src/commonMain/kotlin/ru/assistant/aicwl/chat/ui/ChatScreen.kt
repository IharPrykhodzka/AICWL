package ru.assistant.aicwl.chat.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.assistant.aicwl.chat.config.ModelConfig
import ru.assistant.aicwl.chat.data.EnhancedChatMessage
import ru.assistant.aicwl.chat.data.MessageRole
import ru.assistant.aicwl.chat.data.MessageType
import ru.assistant.aicwl.chat.data.UiChatMessage
import ru.assistant.aicwl.chat.ui.components.StructuredResponseCard

/**
 * Главный экран чата с выбором модели и списком сообщений.
 * Поддерживает структурированные ответы от AI.
 */
@Composable
fun ChatScreen(
    viewModel: ChatViewModel = chatViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    // Автопрокрутка вниз при поступлении новых сообщений
    LaunchedEffect(uiState.enhancedMessages.size) {
        if (uiState.enhancedMessages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.enhancedMessages.size - 1)
        }
    }

    Scaffold(
        topBar = {
            ChatTopBar(
                selectedModel = uiState.selectedModel,
                onModelSelected = { viewModel.selectModel(it) },
                onClearChat = { viewModel.clearChat() }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Список сообщений
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = uiState.enhancedMessages,
                    key = { it.id }
                ) { message ->
                    EnhancedChatMessageItem(
                        message = message,
                        onSuggestionClick = { suggestion -> viewModel.sendSuggestion(suggestion) }
                    )
                }

                if (uiState.isLoading) {
                    item {
                        LoadingIndicator()
                    }
                }
            }

            // Поле ввода
            ChatInputField(
                inputText = uiState.inputText,
                onInputChanged = { viewModel.updateInputText(it) },
                onSend = { viewModel.sendMessage() },
                isLoading = uiState.isLoading
            )
        }
    }
}

/**
 * Верхняя панель с выбором модели и кнопкой очистки.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatTopBar(
    selectedModel: String,
    onModelSelected: (String) -> Unit,
    onClearChat: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    TopAppBar(
        title = {
            Column {
                Text(
                    text = "AI Chat Agent",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = ModelConfig.getDisplayName(selectedModel),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        actions = {
            // Селектор модели
            Box(modifier = Modifier.padding(end = 8.dp)) {
                TextButton(
                    onClick = { expanded = true },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text("Model")
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Select model",
                        modifier = Modifier.size(16.dp)
                    )
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    ModelConfig.ALL_MODELS.forEach { modelId ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(
                                        text = ModelConfig.getDisplayName(modelId),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    if (modelId == selectedModel) {
                                        Text(
                                            text = "Selected",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            },
                            onClick = {
                                onModelSelected(modelId)
                                expanded = false
                            }
                        )
                    }
                }
            }

            // Кнопка очистки чата
            IconButton(onClick = onClearChat) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "Clear chat"
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}

/**
 * Элемент отдельного сообщения чата (устаревший, для обратной совместимости).
 */
@Composable
fun ChatMessageItem(message: UiChatMessage) {
    EnhancedChatMessageItem(
        message = EnhancedChatMessage(
            id = message.id,
            role = message.role,
            originalContent = message.content,
            timestamp = message.timestamp,
            messageType = MessageType.PLAIN_TEXT
        ),
        onSuggestionClick = {}
    )
}

/**
 * Элемент расширенного сообщения чата с поддержкой структурированных ответов.
 * Автоматически определяет, как отображать сообщение: как структурированную карточку
 * или как обычный текст.
 */
@Composable
fun EnhancedChatMessageItem(
    message: EnhancedChatMessage,
    onSuggestionClick: (String) -> Unit
) {
    val isUser = message.role == MessageRole.USER

    // Для пользователя - обычное сообщение
    if (isUser) {
        UserMessageBubble(
            content = message.originalContent,
            timestamp = message.timestamp
        )
        return
    }

    // Для AI - проверяем тип сообщения
    when (message.messageType) {
        MessageType.STRUCTURED -> {
            // Структурированный ответ - отображаем карточку
            message.structuredData?.let { structured ->
                StructuredResponseCard(
                    response = structured,
                    onSuggestionClick = onSuggestionClick
                )
            } ?: run {
                // Если structuredData == null, fallback на обычный текст
                AssistantMessageBubble(
                    content = message.originalContent,
                    timestamp = message.timestamp
                )
            }
        }
        MessageType.ERROR -> {
            // Сообщение об ошибке
            ErrorMessageBubble(
                content = message.originalContent,
                timestamp = message.timestamp
            )
        }
        else -> {
            // Обычный текстовый ответ
            AssistantMessageBubble(
                content = message.originalContent,
                timestamp = message.timestamp
            )
        }
    }
}

/**
 * Пузырь сообщения пользователя.
 */
@Composable
private fun UserMessageBubble(
    content: String,
    timestamp: Long
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 12.dp,
                        topEnd = 4.dp,
                        bottomStart = 12.dp,
                        bottomEnd = 12.dp
                    )
                )
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(12.dp)
        ) {
            Column {
                Text(
                    text = content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = formatTimestamp(timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

/**
 * Пузырь сообщения ассистента (обычный текст).
 */
@Composable
private fun AssistantMessageBubble(
    content: String,
    timestamp: Long
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 320.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 4.dp,
                        topEnd = 12.dp,
                        bottomStart = 12.dp,
                        bottomEnd = 12.dp
                    )
                )
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .padding(12.dp)
        ) {
            Column {
                Text(
                    text = content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = formatTimestamp(timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

/**
 * Пузырь сообщения об ошибке.
 */
@Composable
private fun ErrorMessageBubble(
    content: String,
    timestamp: Long
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 320.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 4.dp,
                        topEnd = 12.dp,
                        bottomStart = 12.dp,
                        bottomEnd = 12.dp
                    )
                )
                .background(
                    MaterialTheme.colorScheme.errorContainer
                )
                .padding(12.dp)
        ) {
            Column {
                Text(
                    text = content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    text = formatTimestamp(timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

/**
 * Поле ввода с кнопкой отправки.
 * Enter для отправки, Shift+Enter для новой строки.
 */
@Composable
fun ChatInputField(
    inputText: String,
    onInputChanged: (String) -> Unit,
    onSend: () -> Unit,
    isLoading: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = inputText,
            onValueChange = onInputChanged,
            modifier = Modifier
                .weight(1f)
                .onPreviewKeyEvent { keyEvent ->
                    // Проверяем, нажата ли клавиша Enter
                    val isEnter = isEnterKeyPressed(keyEvent)

                    if (isEnter) {
                        // Shift + Enter = новая строка (поведение по умолчанию, возвращаем false для разрешения)
                        if (keyEvent.isShiftPressed) {
                            false  // Разрешаем поведение по умолчанию (новая строка)
                        } else {
                            // Enter = отправка сообщения
                            if (inputText.isNotBlank() && !isLoading) {
                                onSend()
                            }
                            true  // Поглощаем событие (предотвращаем новую строку)
                        }
                    } else {
                        false  // Разрешаем другие клавиши
                    }
                },
            placeholder = { Text("Type your message...") },
            enabled = !isLoading,
            maxLines = 4,
            shape = RoundedCornerShape(24.dp)
        )

        FloatingActionButton(
            onClick = { if (inputText.isNotBlank() && !isLoading) onSend() },
            modifier = Modifier.size(48.dp),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(
                imageVector = Icons.Default.Send,
                contentDescription = "Send",
                tint = if (inputText.isNotBlank() && !isLoading)
                    MaterialTheme.colorScheme.onPrimary
                else
                    MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f)
            )
        }
    }
}

/**
 * Индикатор загрузки для ответа AI.
 */
@Composable
fun LoadingIndicator() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .padding(12.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Text(
                    text = "Thinking...",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
    }
}

/**
 * Форматирует метку времени для отображения.
 */
private fun formatTimestamp(timestamp: Long): String {
    // Простое форматирование метки времени - кроссплатформенное
    val minutes = (timestamp / 60_000) % 60
    val hours = (timestamp / 3_600_000) % 24
    return "${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}"
}
