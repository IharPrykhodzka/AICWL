package ru.assistant.aicwl.chat.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Workspaces
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import ru.assistant.aicwl.chat.config.ModelConfig
import ru.assistant.aicwl.chat.config.TemperatureProfile
import ru.assistant.aicwl.chat.provider.model.UnifiedAIModel
import ru.assistant.aicwl.chat.provider.model.AIModelConfig
import ru.assistant.aicwl.chat.data.EnhancedChatMessage
import ru.assistant.aicwl.chat.utils.getClipboardManager
import ru.assistant.aicwl.chat.data.MessageRole
import ru.assistant.aicwl.chat.data.MessageType
import ru.assistant.aicwl.chat.data.UiChatMessage
import ru.assistant.aicwl.chat.ui.components.StructuredResponseCard
import ru.assistant.aicwl.chat.prompt.ui.PromptSettingsViewModel
import ru.assistant.aicwl.chat.prompt.ui.PromptSettingsViewModelFactory
import ru.assistant.aicwl.chat.prompt.ui.components.PromptSettingsScreen
import ru.assistant.aicwl.chat.prompt.SystemPromptConfig
import kotlinx.coroutines.launch

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

    // Navigation state
    var showSettingsScreen by remember { mutableStateOf(false) }

    // Load custom prompt on composition
    LaunchedEffect(Unit) {
        try {
            SystemPromptConfig.loadCustomPrompt(
                PromptSettingsViewModelFactory.getRepository()
            )
        } catch (e: Exception) {
            // Handle initialization error silently
        }
    }

    // Show settings screen or chat screen
    if (showSettingsScreen) {
        // Settings screen with its own ViewModel
        val coroutineScope = rememberCoroutineScope()
        val settingsViewModel: PromptSettingsViewModel = remember {
            PromptSettingsViewModelFactory.create(
                coroutineScope = coroutineScope
            )
        }
        val settingsUiState by settingsViewModel.uiState.collectAsStateWithLifecycle()
        val editedPrompt by settingsViewModel.editedPrompt.collectAsStateWithLifecycle()
        val newRuleText by settingsViewModel.newRuleText.collectAsStateWithLifecycle()

        PromptSettingsScreen(
            uiState = settingsUiState,
            editedPrompt = editedPrompt,
            newRuleText = newRuleText,
            onPromptChanged = { settingsViewModel.onPromptTextChanged(it) },
            onNewRuleTextChanged = { settingsViewModel.onNewRuleTextChanged(it) },
            onSave = {
                settingsViewModel.savePrompt()
                // Update SystemPromptConfig with new custom prompt
                SystemPromptConfig.setCustomPrompt(editedPrompt)
            },
            onReset = {
                settingsViewModel.resetToDefault()
                SystemPromptConfig.setCustomPrompt("")
            },
            onAddRule = { settingsViewModel.addRule() },
            onRemoveRule = { settingsViewModel.removeRule(it) },
            onClearRules = { settingsViewModel.clearAllRules() },
            onToggleChatHistory = { enabled -> settingsViewModel.toggleChatHistory(enabled) },
            onClearChatHistory = { settingsViewModel.clearChatHistory() },
            onBack = { showSettingsScreen = false },
            onClearError = { settingsViewModel.clearError() }
        )
    } else {
        // Chat screen
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
                    selectedProfile = uiState.selectedProfile,
                    onProfileSelected = { viewModel.selectProfile(it) },
                    onClearChat = { viewModel.clearChat() },
                    onSettingsClick = { showSettingsScreen = true }
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
                    isLoading = uiState.isLoading,
                    isBusinessAnalystMode = uiState.isBusinessAnalystMode,
                    onBusinessAnalystModeToggle = { viewModel.toggleBusinessAnalystMode() }
                )
            }
        }
    }
}

/**
 * Верхняя панель с выбором модели, профиля температуры и кнопкой очистки.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatTopBar(
    selectedModel: UnifiedAIModel,
    onModelSelected: (UnifiedAIModel) -> Unit,
    selectedProfile: TemperatureProfile,
    onProfileSelected: (TemperatureProfile) -> Unit,
    onClearChat: () -> Unit,
    onSettingsClick: () -> Unit = {}
) {
    var modelExpanded by remember { mutableStateOf(false) }
    var profileExpanded by remember { mutableStateOf(false) }

    // Determine if temperature control should be disabled (for Qwen models)
    val isTemperatureDisabled = !selectedModel.supportsThinking

    TopAppBar(
        title = {
            Column {
                Text(
                    text = "AI Chat Agent",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "${selectedModel.displayName} • ${selectedProfile.displayName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        actions = {
            // Селектор профиля температуры
            Box(modifier = Modifier.padding(end = 4.dp)) {
                IconButton(
                    onClick = { if (!isTemperatureDisabled) profileExpanded = true },
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = if (isTemperatureDisabled)
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f)
                        else
                            MaterialTheme.colorScheme.primary
                    ),
                    enabled = !isTemperatureDisabled
                ) {
                    Icon(
                        imageVector = Icons.Default.Thermostat,
                        contentDescription = if (isTemperatureDisabled)
                            "Temperature not available for this model"
                        else
                            "Select temperature profile",
                        modifier = Modifier.size(20.dp)
                    )
                }

                DropdownMenu(
                    expanded = profileExpanded,
                    onDismissRequest = { profileExpanded = false }
                ) {
                    TemperatureProfile.entries.forEach { profile ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(
                                        text = profile.displayName,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Text(
                                        text = profile.description,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (profile == selectedProfile) {
                                        Text(
                                            text = "Selected",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            },
                            onClick = {
                                onProfileSelected(profile)
                                profileExpanded = false
                            }
                        )
                    }
                }
            }

            // Селектор модели
            Box(modifier = Modifier.padding(end = 4.dp)) {
                TextButton(
                    onClick = { modelExpanded = true },
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
                    expanded = modelExpanded,
                    onDismissRequest = { modelExpanded = false }
                ) {
                    AIModelConfig.getAllModels().forEach { model ->
                        DropdownMenuItem(
                            text = {
                                Column {
                                    Text(
                                        text = model.displayName,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Text(
                                        text = model.description,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    if (model.uniqueId == selectedModel.uniqueId) {
                                        Text(
                                            text = "Selected",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            },
                            onClick = {
                                onModelSelected(model)
                                modelExpanded = false
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

            // Кнопка настроек промпта
            IconButton(onClick = onSettingsClick) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Prompt settings"
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
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.Top
    ) {
        // Кнопка копирования
        CopyButton(
            text_to_copy = content,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.width(4.dp))

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
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Top
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

        Spacer(modifier = Modifier.width(4.dp))

        // Кнопка копирования
        CopyButton(
            text_to_copy = content,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
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
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Top
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

        Spacer(modifier = Modifier.width(4.dp))

        // Кнопка копирования
        CopyButton(
            text_to_copy = content,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
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
    isLoading: Boolean,
    isBusinessAnalystMode: Boolean = false,
    onBusinessAnalystModeToggle: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Переключатель режима бизнес-аналитика
        BusinessAnalystToggleButton(
            is_enabled = isBusinessAnalystMode,
            on_toggle = onBusinessAnalystModeToggle
        )

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
            placeholder = {
                Text(
                    if (isBusinessAnalystMode) "Опишите вашу идею для ТЗ..."
                    else "Type your message..."
                )
            },
            enabled = !isLoading,
            maxLines = 4,
            shape = RoundedCornerShape(24.dp),
            colors = if (isBusinessAnalystMode) {
                OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.tertiary,
                    focusedLabelColor = MaterialTheme.colorScheme.tertiary
                )
            } else {
                OutlinedTextFieldDefaults.colors()
            }
        )

        FloatingActionButton(
            onClick = { if (inputText.isNotBlank() && !isLoading) onSend() },
            modifier = Modifier.size(48.dp),
            containerColor = if (isBusinessAnalystMode)
                MaterialTheme.colorScheme.tertiary
            else
                MaterialTheme.colorScheme.primary
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

/**
 * Переключатель режима бизнес-аналитика.
 * Иконка с индикацией активного режима.
 */
@Composable
private fun BusinessAnalystToggleButton(
    is_enabled: Boolean,
    on_toggle: () -> Unit
) {
    val container_color = if (is_enabled)
        MaterialTheme.colorScheme.tertiaryContainer
    else
        MaterialTheme.colorScheme.surfaceVariant

    val content_color = if (is_enabled)
        MaterialTheme.colorScheme.onTertiaryContainer
    else
        MaterialTheme.colorScheme.onSurfaceVariant

    IconButton(
        onClick = on_toggle,
        modifier = Modifier.size(48.dp)
    ) {
        Box(
            modifier = Modifier.size(40.dp),
            contentAlignment = Alignment.Center
        ) {
            // Фон с индикатором
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(container_color)
                    .then(
                        if (is_enabled) {
                            Modifier
                        } else {
                            Modifier
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Workspaces,
                    contentDescription = if (is_enabled) "Выключить режим бизнес-аналитика"
                    else "Включить режим бизнес-аналитика",
                    tint = content_color,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Индикатор активного режима (точка)
            if (is_enabled) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(12.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.tertiary)
                        .padding(1.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.White)
                    )
                }
            }
        }
    }
}

/**
 * Кнопка копирования текста в буфер обмена.
 */
@Composable
private fun CopyButton(
    text_to_copy: String,
    tint: Color = MaterialTheme.colorScheme.onSurfaceVariant
) {
    var show_copied_feedback by remember { mutableStateOf(false) }

    IconButton(
        onClick = {
            getClipboardManager().setText(text_to_copy)
            show_copied_feedback = true
        },
        modifier = Modifier.size(32.dp)
    ) {
        Icon(
            imageVector = if (show_copied_feedback) Icons.Default.Check else Icons.Default.ContentCopy,
            contentDescription = if (show_copied_feedback) "Скопировано" else "Копировать",
            tint = if (show_copied_feedback) MaterialTheme.colorScheme.primary else tint,
            modifier = Modifier.size(18.dp)
        )
    }

    // Сбросить состояние через 2 секунды
    LaunchedEffect(show_copied_feedback) {
        if (show_copied_feedback) {
            kotlinx.coroutines.delay(2000)
            show_copied_feedback = false
        }
    }
}
