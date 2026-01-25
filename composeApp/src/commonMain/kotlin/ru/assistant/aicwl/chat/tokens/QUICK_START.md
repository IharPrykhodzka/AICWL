# Token Tracking Quick Start

## Initialization (Required!)

### Android (MainActivity.kt)

```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // Initialize token tracking
    val tokenStorage = ru.assistant.aicwl.chat.tokens.TokenStorage(this)
    ru.assistant.aicwl.chat.tokens.initializeTokenTracker(tokenStorage)

    // Initialize ViewModel factory with token tracker
    val repository = ChatHistoryRepositoryImpl(...)
    val tokenTracker = ru.assistant.aicwl.chat.tokens.getTokenTracker(tokenStorage)

    initializeChatViewModelFactory(
        chatHistoryRepository = repository,
        tokenTracker = tokenTracker
    )
}
```

### iOS (main.kt)

```kotlin
fun main() {
    // Initialize token tracking
    val tokenStorage = ru.assistant.aicwl.chat.tokens.TokenStorage()
    ru.assistant.aicwl.chat.tokens.initializeTokenTracker(tokenStorage)

    // Initialize ViewModel
    val repository = ChatHistoryRepositoryImpl(...)
    val tokenTracker = ru.assistant.aicwl.chat.tokens.getTokenTracker(tokenStorage)

    initializeChatViewModel(
        repository = repository,
        tracker = tokenTracker
    )

    // ... rest of app setup
}
```

### Desktop (main.kt)

```kotlin
fun main() {
    // Initialize token tracking
    val tokenStorage = ru.assistant.aicwl.chat.tokens.TokenStorage()
    ru.assistant.aicwl.chat.tokens.initializeTokenTracker(tokenStorage)

    // Initialize ViewModel
    val repository = ChatHistoryRepositoryImpl(...)
    val tokenTracker = ru.assistant.aicwl.chat.tokens.getTokenTracker(tokenStorage)

    initializeChatViewModel(
        repository = repository,
        tracker = tokenTracker
    )

    // ... rest of app setup
}
```

## Basic Usage

### Estimate Tokens Before Sending

```kotlin
import ru.assistant.aicwl.chat.tokens.TokenCounter

val text = "Your message here"
val model = selectedModel

// Get estimate
val estimate = TokenCounter.estimateTextTokens(text, model)

// Use in UI
println("~${estimate.estimatedTokens} tokens")
println("~$${TokenCounter.formatCost(estimate.estimatedCost)}")
```

### Access Statistics

```kotlin
import ru.assistant.aicwl.chat.tokens.getTokenTracker

val tracker = getTokenTracker()
val stats = tracker.getCurrentStatistics()

// Overall statistics
println("Total requests: ${stats.totalRequests}")
println("Total tokens: ${stats.totalTokens}")
println("Total cost: $${stats.totalCost}")

// Per-model statistics
stats.modelBreakdown.values.forEach { modelStats ->
    println("${modelStats.modelId}: ${modelStats.totalTokens} tokens")
}
```

### Reset Statistics

```kotlin
viewModel.resetTokenStatistics()
```

## UI Components

### Show Token Stats Panel

```kotlin
var showStats by remember { mutableStateOf(false) }

// Button to open
IconButton(onClick = { showStats = true }) {
    Icon(Icons.Default.Workspaces, "Token stats")
}

// Dialog
if (showStats) {
    TokenStatsPanel(
        statistics = uiState.tokenStatistics,
        onDismiss = { showStats = false },
        onReset = {
            viewModel.resetTokenStatistics()
            showStats = false
        }
    )
}
```

### Show Token Estimate in Input

```kotlin
// In your input field
if (currentInputEstimate != null) {
    TokenEstimateIndicator(estimate = currentInputEstimate)
}
```

## Common Patterns

### Check if Tracking is Active

```kotlin
val tracker = getTokenTracker()
if (tracker.hasStatistics()) {
    // Show statistics
} else {
    // Show "no data" message
}
```

### Format for Display

```kotlin
import ru.assistant.aicwl.chat.tokens.TokenCounter

// Format token count
val formatted = TokenCounter.formatTokenCount(1500) // "1.5K"

// Format cost
val cost = TokenCounter.formatCost(0.0123) // "0.01"
```

### Get Statistics for Specific Model

```kotlin
val stats = tracker.getCurrentStatistics()
val modelStats = stats.getStatsForModel("glm-4.7")

if (modelStats != null) {
    println("Requests: ${modelStats.requestCount}")
    println("Tokens: ${modelStats.totalTokens}")
    println("Cost: $${modelStats.cost}")
}
```

## Key Classes

### TokenCounter
Utility for estimating tokens and formatting.

```kotlin
TokenCounter.estimateTokens(text: String): Int
TokenCounter.estimateCost(promptText: String, completionTokens: Int, model: UnifiedAIModel): Double
TokenCounter.formatTokenCount(tokens: Int): String
TokenCounter.formatCost(cost: Double): String
```

### TokenTracker
Manages statistics and persistence.

```kotlin
val statistics: StateFlow<TokenStatistics>
suspend fun recordUsage(usage: TokenUsage)
suspend fun resetStatistics()
fun getCurrentStatistics(): TokenStatistics
fun hasStatistics(): Boolean
```

### TokenStatistics
Data class holding all statistics.

```kotlin
val totalRequests: Int
val totalPromptTokens: Int
val totalCompletionTokens: Int
val totalTokens: Int
val totalCost: Double
val modelBreakdown: Map<String, ModelTokenStats>
```

## Troubleshooting

### Statistics Not Showing

1. **Check initialization**: Ensure `initializeTokenTracker()` is called before `initializeChatViewModel()`
2. **Check storage**: Verify platform-specific storage is working
3. **Check permissions**: Ensure app has storage permissions (Android)

### Token Estimates Seem Wrong

1. **They're estimates**: The `~` prefix indicates approximation
2. **Different languages**: Estimates vary by language (English vs Chinese)
3. **Code vs text**: Code has different token ratios
4. **Use actual data**: After sending, use actual usage from API response

### Cost Shows $0.00

1. **Check model config**: Ensure `inputCostPerMillion` and `outputCostPerMillion` are set
2. **Check API response**: Verify usage data is being returned
3. **Check pricing**: Some models may not have public pricing

## Next Steps

- Read full documentation: [`README.md`](README.md)
- Check implementation: [`TokenTracker.kt`](TokenTracker.kt)
- See UI components: [`TokenStatsPanel.kt`](../ui/components/TokenStatsPanel.kt)
