package ru.assistant.aicwl.chat.prompt.ui

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.assistant.aicwl.chat.prompt.domain.*
import ru.assistant.aicwl.chat.prompt.model.PromptRuleData
import ru.assistant.aicwl.chat.prompt.model.PromptSettings

/**
 * ViewModel for prompt settings screen.
 * Presentation layer - manages UI state and handles user interactions.
 * Follows MVVM pattern with unidirectional data flow.
 */
class PromptSettingsViewModel(
    private val loadSettingsUseCase: LoadPromptSettingsUseCase,
    private val updateCustomPromptUseCase: UpdateCustomPromptUseCase,
    private val addRuleUseCase: AddPromptRuleUseCase,
    private val removeRuleUseCase: RemovePromptRuleUseCase,
    private val resetPromptUseCase: ResetPromptUseCase,
    private val clearRulesUseCase: ClearPromptRulesUseCase,
    private val toggleChatHistoryUseCase: ToggleChatHistoryUseCase,
    private val clearChatHistoryUseCase: ClearChatHistoryUseCase,
    private val defaultMainPrompt: String,
    coroutineScope: CoroutineScope? = null
) {
    private val viewModelScope = coroutineScope ?: CoroutineScope(Dispatchers.Main)

    // UI State
    private val _uiState = MutableStateFlow<PromptSettingsUiState>(PromptSettingsUiState.Loading)
    val uiState: StateFlow<PromptSettingsUiState> = _uiState.asStateFlow()

    // Editable state for form inputs
    private val _editedPrompt = MutableStateFlow("")
    val editedPrompt: StateFlow<String> = _editedPrompt.asStateFlow()

    private val _newRuleText = MutableStateFlow("")
    val newRuleText: StateFlow<String> = _newRuleText.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            _uiState.value = PromptSettingsUiState.Loading
            try {
                val settings = loadSettingsUseCase()
                _editedPrompt.value = settings.getEffectivePrompt(defaultMainPrompt)
                _uiState.value = PromptSettingsUiState.Success(
                    customPrompt = settings.customMainPrompt,
                    additionalRules = settings.additionalRules,
                    isUsingCustomPrompt = settings.hasCustomPrompt(),
                    effectivePrompt = settings.getEffectivePrompt(defaultMainPrompt),
                    saveChatHistory = settings.saveChatHistory
                )
            } catch (e: Exception) {
                _uiState.value = PromptSettingsUiState.Error(e.message ?: "Failed to load settings")
            }
        }
    }

    fun onPromptTextChanged(text: String) {
        _editedPrompt.value = text
    }

    fun onNewRuleTextChanged(text: String) {
        _newRuleText.value = text
    }

    fun savePrompt() {
        viewModelScope.launch {
            try {
                updateCustomPromptUseCase(_editedPrompt.value)
                loadSettings()
            } catch (e: Exception) {
                _uiState.value = PromptSettingsUiState.Error(e.message ?: "Failed to save prompt")
            }
        }
    }

    fun resetToDefault() {
        viewModelScope.launch {
            try {
                resetPromptUseCase()
                _editedPrompt.value = defaultMainPrompt
                loadSettings()
            } catch (e: Exception) {
                _uiState.value = PromptSettingsUiState.Error(e.message ?: "Failed to reset prompt")
            }
        }
    }

    fun addRule() {
        val ruleText = _newRuleText.value.trim()
        if (ruleText.isBlank()) return

        viewModelScope.launch {
            try {
                addRuleUseCase(ruleText)
                _newRuleText.value = ""
                loadSettings()
            } catch (e: Exception) {
                _uiState.value = PromptSettingsUiState.Error(e.message ?: "Failed to add rule")
            }
        }
    }

    fun removeRule(ruleId: String) {
        viewModelScope.launch {
            try {
                removeRuleUseCase(ruleId)
                loadSettings()
            } catch (e: Exception) {
                _uiState.value = PromptSettingsUiState.Error(e.message ?: "Failed to remove rule")
            }
        }
    }

    fun clearAllRules() {
        viewModelScope.launch {
            try {
                clearRulesUseCase()
                loadSettings()
            } catch (e: Exception) {
                _uiState.value = PromptSettingsUiState.Error(e.message ?: "Failed to clear rules")
            }
        }
    }

    fun clearError() {
        val currentState = _uiState.value
        if (currentState is PromptSettingsUiState.Error) {
            _uiState.value = PromptSettingsUiState.Loading
            loadSettings()
        }
    }

    fun toggleChatHistory(enabled: Boolean) {
        viewModelScope.launch {
            try {
                toggleChatHistoryUseCase(enabled)
                loadSettings()
            } catch (e: Exception) {
                _uiState.value = PromptSettingsUiState.Error(e.message ?: "Failed to update chat history setting")
            }
        }
    }

    fun clearChatHistory() {
        viewModelScope.launch {
            try {
                clearChatHistoryUseCase()
                // Show success or refresh state as needed
            } catch (e: Exception) {
                _uiState.value = PromptSettingsUiState.Error(e.message ?: "Failed to clear chat history")
            }
        }
    }
}

/**
 * UI State for prompt settings screen.
 * Sealed class ensures type safety and exhaustive handling.
 */
sealed class PromptSettingsUiState {
    object Loading : PromptSettingsUiState()

    data class Success(
        val customPrompt: String?,
        val additionalRules: List<PromptRuleData>,
        val isUsingCustomPrompt: Boolean,
        val effectivePrompt: String,
        val saveChatHistory: Boolean = true
    ) : PromptSettingsUiState()

    data class Error(val message: String) : PromptSettingsUiState()
}
