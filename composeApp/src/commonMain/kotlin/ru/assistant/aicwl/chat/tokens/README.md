# Token Tracking and Cost Calculation System

## Overview

Comprehensive token tracking and cost calculation system for the AICWL Kotlin Multiplatform chat application. This system provides real-time token counting, cost estimation, and persistent statistics tracking across all AI conversations.

## Features

### 1. Real-time Token Estimation
- **Pre-send estimation**: Shows estimated tokens/cost before sending a message
- **Supports multiple languages**: English, Russian, Chinese/Japanese (CJK), code
- **Context-aware**: Adjusts estimation based on text type

### 2. Comprehensive Statistics
- **Total tokens**: Overall usage across all conversations
- **Total for AI responses**: Cumulative completion tokens
- **Total for user messages**: Cumulative prompt tokens
- **Cost tracking**: Calculates cost based on model pricing
- **Per-model breakdown**: Statistics broken down by AI model

### 3. Persistent Storage
- **Platform-specific implementations**:
  - Android: SharedPreferences
  - iOS: UserDefaults
  - Desktop: Local file system
- **Automatic saving**: Statistics saved after each request

## Architecture

### Core Components

#### 1. Data Models ([`TokenUsage.kt`](TokenUsage.kt))

```kotlin
// Single usage record
data class TokenUsage(
    val modelId: String,
    val promptTokens: Int,
    val completionTokens: Int,
    val totalTokens: Int,
    val estimatedCost: Double,
    val timestamp: Long
)

// Aggregated statistics
data class TokenStatistics(
    val totalRequests: Int,
    val totalPromptTokens: Int,
    val totalCompletionTokens: Int,
    val totalTokens: Int,
    val totalCost: Double,
    val modelBreakdown: Map<String, ModelTokenStats>
)

// Pre-send estimation
data class TokenEstimate(
    val estimatedTokens: Int,
    val estimatedCost: Double,
    val characterCount: Int
)
```

#### 2. Token Counter ([`TokenCounter.kt`](TokenCounter.kt))

Utility for estimating token counts using empirical rules:

- **English**: ~4 characters per token
- **Russian**: ~3 characters per token
- **Chinese/Japanese**: ~2.5 characters per token
- **Code**: ~3-4 characters per token

```kotlin
// Estimate tokens for text
val tokens = TokenCounter.estimateTokens(text)

// Estimate cost
val cost = TokenCounter.estimateCost(promptText, estimatedCompletion, model)

// Format for display
val formatted = TokenCounter.formatTokenCount(1500) // "1.5K"
```

#### 3. Token Tracker ([`TokenTracker.kt`](TokenTracker.kt))

Manages token statistics with automatic persistence:

```kotlin
class TokenTracker(
    private val storage: TokenStorage
) {
    val statistics: StateFlow<TokenStatistics>

    suspend fun recordUsage(usage: TokenUsage)
    suspend fun resetStatistics()
    fun getCurrentStatistics(): TokenStatistics
}
```

#### 4. Storage Layer ([`TokenStorage.kt`](TokenStorage.kt))

Platform-agnostic interface with platform-specific implementations:

- **Android**: [`TokenStorage.android.kt`](../androidMain/kotlin/ru/assistant/aicwl/chat/tokens/TokenStorage.android.kt)
- **iOS**: [`TokenStorage.ios.kt`](../iosMain/kotlin/ru/assistant/aicwl/chat/tokens/TokenStorage.ios.kt)
- **Desktop**: [`TokenStorage.jvm.kt`](../jvmMain/kotlin/ru/assistant/aicwl/chat/tokens/TokenStorage.jvm.kt)

### UI Components

#### 1. Token Stats Panel ([`TokenStatsPanel.kt`](../ui/components/TokenStatsPanel.kt))

Full-screen dialog showing:
- Overall statistics (requests, tokens, cost)
- Per-model breakdown
- Reset functionality

```kotlin
TokenStatsPanel(
    statistics = tokenStatistics,
    onDismiss = { /* close */ },
    onReset = { viewModel.resetTokenStatistics() }
)
```

#### 2. Token Estimate Indicator

Real-time token counter in chat input field:

```kotlin
TokenEstimateIndicator(
    estimate = currentInputEstimate
)
```

Shows:
- Estimated token count
- Estimated cost (if pricing available)

#### 3. Chat Screen Integration

Updated [`ChatScreen.kt`](../ui/ChatScreen.kt) with:
- Token stats button in top bar
- Real-time token counter in input
- Total tokens display in title
- Full stats dialog

## Integration Guide

### 1. Initialize Token Tracker

In your app initialization (e.g., `MainActivity.kt` for Android):

```kotlin
// Create storage
val tokenStorage = TokenStorage(context)

// Initialize tracker
val tokenTracker = TokenTracker(tokenStorage)
initializeTokenTracker(tokenStorage)

// Pass to ViewModel factory
initializeChatViewModelFactory(
    chatHistoryRepository = repository,
    tokenTracker = tokenTracker
)
```

### 2. Update ChatAgent

The `ChatAgent` now accepts an optional `TokenTracker`:

```kotlin
val chatAgent = ChatAgent(
    tokenTracker = tokenTracker
)
```

Token usage is automatically recorded after each API response.

### 3. Use in ViewModel

```kotlin
class ChatViewModel(
    private val chatHistoryRepository: ChatHistoryRepository,
    private val tokenTracker: TokenTracker? = null
) : ViewModel() {
    // Token statistics automatically loaded
    // Token estimates calculated on text change

    fun resetTokenStatistics() {
        viewModelScope.launch {
            tokenTracker?.resetStatistics()
        }
    }
}
```

### 4. Access Statistics

```kotlin
// Get current statistics
val stats = tokenTracker.getCurrentStatistics()

// Get per-model statistics
val modelStats = stats.getStatsForModel("glm-4.7")

// Check if tracking is active
if (tokenTracker.hasStatistics()) {
    // Display statistics
}
```

## Token Estimation Algorithm

The system uses empirical rules for token estimation:

### Text Detection

1. **CJK Detection**: Checks for characters in range `0x4E00..0x9FFF`
   - Ratio: 0.4 tokens per character (~2.5 chars/token)

2. **Cyrillic Detection**: Checks for characters in range `0x0400..0x04FF`
   - Ratio: 0.35 tokens per character (~3 chars/token)

3. **Code Detection**: Checks for `{`, `}`, `;`, `(` characters
   - Ratio: 0.3 tokens per character (~3.3 chars/token)

4. **Default (Latin)**: No special characters detected
   - Ratio: 0.25 tokens per character (~4 chars/token)

### Cost Calculation

Cost is calculated based on model pricing:

```kotlin
val inputCost = (inputCostPerMillion * inputTokens) / 1_000_000
val outputCost = (outputCostPerMillion * outputTokens) / 1_000_000
val totalCost = inputCost + outputCost
```

## Display Formatting

### Token Counts

- `< 1,000`: Raw number (e.g., "742")
- `1,000 - 999,999`: K format (e.g., "1.5K")
- `≥ 1,000,000`: M format (e.g., "2.3M")

### Costs

- `< $0.01`: 4 decimal places (e.g., "$0.0023")
- `$0.01 - $0.99`: 2 decimal places (e.g., "$0.45")
- `≥ $1.00`: 2 decimal places (e.g., "$1.23")

## Data Persistence

### Storage Format

Statistics are stored as JSON:

```json
{
  "totalRequests": 42,
  "totalPromptTokens": 15000,
  "totalCompletionTokens": 8000,
  "totalTokens": 23000,
  "totalCost": 0.045,
  "modelBreakdown": {
    "glm-4.7": {
      "modelId": "glm-4.7",
      "requestCount": 30,
      "promptTokens": 12000,
      "completionTokens": 6000,
      "totalTokens": 18000,
      "cost": 0.035
    }
  },
  "lastUpdate": 1234567890
}
```

### Storage Locations

- **Android**: `SharedPreferences` - "token_statistics" -> "token_stats_data"
- **iOS**: `UserDefaults` - "token_stats_data"
- **Desktop**: `~/.aicwl/token_statistics.json`

## Best Practices

### 1. Always Initialize Early

Initialize token tracker during app startup to ensure statistics are loaded:

```kotlin
// In Application.onCreate() or similar
val tokenStorage = TokenStorage(context)
initializeTokenTracker(tokenStorage)
```

### 2. Handle Null Pricing

Some models don't have pricing information. Always check:

```kotlin
val cost = model.estimateCost(promptTokens, completionTokens)
if (cost != null) {
    // Display cost
} else {
    // Hide cost or show "N/A"
}
```

### 3. Reset Statistics Responsibly

Provide user confirmation before resetting:

```kotlin
if (userConfirms) {
    viewModel.resetTokenStatistics()
}
```

### 4. Estimate Conservatively

Token estimation is approximate. Always display with `~` prefix:

```
~125 tokens
~$0.0003
```

## Future Enhancements

### Potential Improvements

1. **Real Tokenizer Integration**
   - Integrate platform-specific tokenizers for accurate counting
   - Use `tiktoken` for OpenAI-compatible models
   - Use model-specific tokenizers for GLM, Qwen

2. **Budget Management**
   - Set daily/monthly cost limits
   - Warn when approaching limits
   - Block requests when limit exceeded

3. **Advanced Analytics**
   - Token usage over time charts
   - Cost per request trends
   - Model efficiency comparisons

4. **Export/Import**
   - Export statistics to CSV
   - Backup/restore functionality
   - Cross-device synchronization

## Troubleshooting

### Statistics Not Saving

**Problem**: Statistics reset on app restart

**Solution**: Ensure token tracker is initialized before ViewModel creation:

```kotlin
// Correct order
initializeTokenTracker(tokenStorage)
initializeChatViewModel(repository, tokenTracker)
```

### Incorrect Token Estimates

**Problem**: Estimates seem too high/low

**Solution**: The system uses empirical rules. For accurate counts:
- Use actual API response usage data when available
- Consider implementing a proper tokenizer
- The `~` prefix indicates estimation

### Cost Not Displaying

**Problem**: Cost shows as $0.00 or not at all

**Solution**: Check model configuration in [`AIModelConfig.kt`](../provider/model/AIModelConfig.kt):

```kotlin
UnifiedAIModel(
    // ...
    inputCostPerMillion = 0.50,  // Must be set
    outputCostPerMillion = 1.50  // Must be set
)
```

## Files Modified/Created

### New Files

1. `/composeApp/src/commonMain/kotlin/ru/assistant/aicwl/chat/tokens/TokenUsage.kt`
2. `/composeApp/src/commonMain/kotlin/ru/assistant/aicwl/chat/tokens/TokenCounter.kt`
3. `/composeApp/src/commonMain/kotlin/ru/assistant/aicwl/chat/tokens/TokenTracker.kt`
4. `/composeApp/src/commonMain/kotlin/ru/assistant/aicwl/chat/tokens/TokenStorage.kt`
5. `/composeApp/src/androidMain/kotlin/ru/assistant/aicwl/chat/tokens/TokenStorage.android.kt`
6. `/composeApp/src/iosMain/kotlin/ru/assistant/aicwl/chat/tokens/TokenStorage.ios.kt`
7. `/composeApp/src/jvmMain/kotlin/ru/assistant/aicwl/chat/tokens/TokenStorage.jvm.kt`
8. `/composeApp/src/commonMain/kotlin/ru/assistant/aicwl/chat/ui/components/TokenStatsPanel.kt`
9. `/composeApp/src/commonMain/kotlin/ru/assistant/aicwl/chat/tokens/README.md` (this file)

### Modified Files

1. `/composeApp/src/commonMain/kotlin/ru/assistant/aicwl/chat/agent/ChatAgent.kt`
2. `/composeApp/src/commonMain/kotlin/ru/assistant/aicwl/chat/ui/ChatViewModel.kt`
3. `/composeApp/src/commonMain/kotlin/ru/assistant/aicwl/chat/ui/ChatScreen.kt`
4. `/composeApp/src/androidMain/kotlin/ru/assistant/aicwl/chat/ui/ViewModelFactory.android.kt`
5. `/composeApp/src/iosMain/kotlin/ru/assistant/aicwl/chat/ui/ViewModelFactory.ios.kt`
6. `/composeApp/src/jvmMain/kotlin/ru/assistant/aicwl/chat/ui/ViewModelFactory.jvm.kt`

## License

This token tracking system is part of the AICWL project.
