package ru.assistant.aicwl.chat.prompt.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import ru.assistant.aicwl.chat.prompt.model.PromptRuleData
import ru.assistant.aicwl.chat.prompt.ui.PromptSettingsUiState

/**
 * Prompt Settings Screen.
 * Presentation layer - UI component following Material 3 guidelines.
 * State is hoisted to parent component.
 *
 * @param uiState Current UI state from ViewModel
 * @param editedPrompt Currently edited prompt text
 * @param newRuleText New rule text being entered
 * @param onPromptChanged Callback when prompt text changes
 * @param onNewRuleTextChanged Callback when new rule text changes
 * @param onSave Callback to save prompt
 * @param onReset Callback to reset to default
 * @param onAddRule Callback to add new rule
 * @param onRemoveRule Callback to remove a rule
 * @param onClearRules Callback to clear all rules
 * @param onToggleChatHistory Callback to toggle chat history persistence
 * @param onClearChatHistory Callback to clear chat history
 * @param onBack Callback to navigate back
 * @param onClearError Callback to dismiss error
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PromptSettingsScreen(
    uiState: PromptSettingsUiState,
    editedPrompt: String,
    newRuleText: String,
    onPromptChanged: (String) -> Unit,
    onNewRuleTextChanged: (String) -> Unit,
    onSave: () -> Unit,
    onReset: () -> Unit,
    onAddRule: () -> Unit,
    onRemoveRule: (String) -> Unit,
    onClearRules: () -> Unit,
    onToggleChatHistory: (Boolean) -> Unit,
    onClearChatHistory: () -> Unit,
    onBack: () -> Unit,
    onClearError: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Настройки промпта") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        modifier = modifier
    ) { paddingValues ->
        when (uiState) {
            is PromptSettingsUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is PromptSettingsUiState.Success -> {
                PromptSettingsContent(
                    customPrompt = uiState.customPrompt,
                    additionalRules = uiState.additionalRules,
                    isUsingCustomPrompt = uiState.isUsingCustomPrompt,
                    saveChatHistory = uiState.saveChatHistory,
                    editedPrompt = editedPrompt,
                    newRuleText = newRuleText,
                    onPromptChanged = onPromptChanged,
                    onNewRuleTextChanged = onNewRuleTextChanged,
                    onSave = onSave,
                    onReset = onReset,
                    onAddRule = onAddRule,
                    onRemoveRule = onRemoveRule,
                    onClearRules = onClearRules,
                    onToggleChatHistory = onToggleChatHistory,
                    onClearChatHistory = onClearChatHistory,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }

            is PromptSettingsUiState.Error -> {
                ErrorContent(
                    message = uiState.message,
                    onDismiss = onClearError,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }
        }
    }
}

@Composable
private fun PromptSettingsContent(
    customPrompt: String?,
    additionalRules: List<PromptRuleData>,
    isUsingCustomPrompt: Boolean,
    saveChatHistory: Boolean,
    editedPrompt: String,
    newRuleText: String,
    onPromptChanged: (String) -> Unit,
    onNewRuleTextChanged: (String) -> Unit,
    onSave: () -> Unit,
    onReset: () -> Unit,
    onAddRule: () -> Unit,
    onRemoveRule: (String) -> Unit,
    onClearRules: () -> Unit,
    onToggleChatHistory: (Boolean) -> Unit,
    onClearChatHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Main Prompt Section
        item {
            PromptEditorSection(
                editedPrompt = editedPrompt,
                onPromptChanged = onPromptChanged,
                onSave = onSave,
                onReset = onReset,
                isUsingCustomPrompt = isUsingCustomPrompt
            )
        }

        // 2. Additional Rules Section
        item {
            RulesSection(
                rules = additionalRules,
                newRuleText = newRuleText,
                onNewRuleTextChanged = onNewRuleTextChanged,
                onAddRule = onAddRule,
                onRemoveRule = onRemoveRule,
                onClearRules = if (additionalRules.isNotEmpty()) onClearRules else null
            )
        }

        // 3. Chat History Section
        item {
            ChatHistorySection(
                saveChatHistory = saveChatHistory,
                onToggleChatHistory = onToggleChatHistory,
                onClearChatHistory = onClearChatHistory
            )
        }
    }
}

@Composable
private fun ChatHistorySection(
    saveChatHistory: Boolean,
    onToggleChatHistory: (Boolean) -> Unit,
    onClearChatHistory: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "История чата",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Сохранять историю",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (saveChatHistory) {
                            "История сообщений сохраняется между сессиями"
                        } else {
                            "История не сохраняется"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }

                Switch(
                    checked = saveChatHistory,
                    onCheckedChange = onToggleChatHistory
                )
            }

            Divider(
                modifier = Modifier.padding(vertical = 4.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = onClearChatHistory,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Очистить историю чата")
                }
            }
        }
    }
}

@Composable
private fun PromptEditorSection(
    editedPrompt: String,
    onPromptChanged: (String) -> Unit,
    onSave: () -> Unit,
    onReset: () -> Unit,
    isUsingCustomPrompt: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Основной промпт",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (isUsingCustomPrompt) {
                SuggestionChip(
                    onClick = { },
                    label = { Text("Используется кастомный промпт") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            OutlinedTextField(
                value = editedPrompt,
                onValueChange = onPromptChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                label = { Text("Текст системного промпта") },
                placeholder = { Text("Введите базовый промпт для AI...") },
                textStyle = MaterialTheme.typography.bodyMedium,
                singleLine = false
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
            ) {
                TextButton(onClick = onReset) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Сбросить")
                }

                Button(onClick = onSave) {
                    Icon(Icons.Default.Save, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Сохранить")
                }
            }
        }
    }
}

@Composable
private fun RulesSection(
    rules: List<PromptRuleData>,
    newRuleText: String,
    onNewRuleTextChanged: (String) -> Unit,
    onAddRule: () -> Unit,
    onRemoveRule: (String) -> Unit,
    onClearRules: (() -> Unit)?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Дополнительные правила",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )

                if (onClearRules != null) {
                    TextButton(onClick = onClearRules) {
                        Text("Очистить все")
                    }
                }
            }

            // Add new rule input
            OutlinedTextField(
                value = newRuleText,
                onValueChange = onNewRuleTextChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Новое правило") },
                placeholder = { Text("Например: Отвечай только на русском") },
                trailingIcon = {
                    IconButton(onClick = onAddRule, enabled = newRuleText.isNotBlank()) {
                        Icon(Icons.Default.Add, contentDescription = "Добавить правило")
                    }
                },
                singleLine = true
            )

            // Rules list
            if (rules.isEmpty()) {
                Text(
                    text = "Нет дополнительных правил",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.6f),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                rules.forEach { rule ->
                    RuleItem(
                        rule = rule,
                        onRemove = { onRemoveRule(rule.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun RuleItem(
    rule: PromptRuleData,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = rule.text,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )

            IconButton(onClick = onRemove) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Удалить правило",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Ошибка",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.error
        )

        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium
        )

        Button(onClick = onDismiss) {
            Text("Попробовать снова")
        }
    }
}
