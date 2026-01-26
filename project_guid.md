# AICWL - Project Guide

## Table of Contents
1. [Project Overview](#project-overview)
2. [Architecture & Patterns](#architecture--patterns)
3. [Project Structure](#project-structure)
4. [Core Components Analysis](#core-components-analysis)
5. [Data Layer](#data-layer)
6. [Domain/Business Logic Layer](#domainbusiness-logic-layer)
7. [Presentation/UI Layer](#presentationui-layer)
8. [Key Relationships & Data Flow](#key-relationships--data-flow)
9. [AI/Chat Integration](#aichat-integration)
10. [AI Agent Guide](#ai-agent-guide)

---

## Project Overview

### Purpose and Functionality

**AICWL** (AI Chat with Kotlin) is a cross-platform AI chat assistant application built with Kotlin Multiplatform (KMP) and Compose Multiplatform. The application provides real-time conversations with multiple AI models through a unified interface.

### Key Features

- **Multi-Platform Support**: Android (API 27+), iOS (arm64/x64), Desktop (Windows, macOS, Linux)
- **Multiple AI Providers**:
  - Z.AI (GLM models: glm-4.7, glm-4.5v, glm-4.5-air, glm-4.7-flash)
  - Qwen (via HuggingFace)
  - Oreal (via HuggingFace)
- **Chat History Management**: Persistent storage with automatic save/load
- **Token Tracking**: Real-time token usage and cost estimation
- **Structured Responses**: AI can return structured JSON responses for better UX
- **Business Analyst Mode**: Interactive mode for requirements gathering
- **Custom System Prompts**: Configurable AI behavior through prompt settings
- **Temperature Profiles**: Creative, Technical, Balanced, Fast presets
- **Material Design 3**: Modern, responsive UI

### Technology Stack

| Technology | Version | Purpose |
|------------|---------|---------|
| Kotlin Multiplatform | 2.3.0 | Cross-platform code sharing |
| Compose Multiplatform | 1.9.3 | Declarative UI framework |
| Ktor Client | 3.0.3 | HTTP networking |
| Kotlinx Serialization | 1.8.0 | JSON serialization |
| Material Design 3 | - | UI design system |

---

## Architecture & Patterns

### Overall Architecture

The application follows **Clean Architecture** principles with clear separation of concerns:

```
┌─────────────────────────────────────────┐
│         Presentation Layer              │
│  (Compose UI + ViewModels)              │
└─────────────────────────────────────────┘
                    │
                    ▼
┌─────────────────────────────────────────┐
│           Domain Layer                  │
│  (ChatAgent + Business Logic)           │
└─────────────────────────────────────────┘
                    │
                    ▼
┌─────────────────────────────────────────┐
│            Data Layer                   │
│  (API Clients + Repositories)           │
└─────────────────────────────────────────┘
```

### Design Patterns

1. **MVVM (Model-View-ViewModel)**
   - View: Compose UI screens
   - ViewModel: State management with `StateFlow`
   - Model: Data classes and repositories

2. **Repository Pattern**
   - Abstract data access behind interfaces
   - Examples: `ChatHistoryRepository`, `PromptSettingsRepository`, `UserPreferencesRepository`

3. **Strategy Pattern**
   - `AIProvider` interface for multiple AI providers
   - Each provider implements the same interface
   - `AIProviderFactory` creates appropriate provider

4. **Factory Pattern**
   - `ViewModelFactory` for ViewModel creation
   - `AIProviderFactory` for provider instantiation

5. **Singleton Pattern**
   - `ChatAgent`, `ChatApiClient`, `TokenTracker` instances
   - Lazy initialization with thread-safe access

6. **Observer Pattern**
   - `StateFlow` for reactive state management
   - `collectAsStateWithLifecycle()` in Compose

### Dependency Injection

The project uses manual dependency injection via:
- Factory functions (`ViewModelFactory`, `AIProviderFactory`)
- Constructor injection in ViewModels and repositories
- Singleton instances for global services

### Expect/Actual Pattern

Platform-specific implementations use KMP's expect/actual:
- `AppConfig` (platform-specific configuration)
- `Logger` (platform-specific logging)
- `TokenStorage` (platform-specific persistence)
- `KeyEventUtils` (platform-specific keyboard handling)
- `PlatformUtils` (platform-specific utilities)

---

## Project Structure

```
AICWL/
├── composeApp/                              # Main shared module
│   ├── src/
│   │   ├── commonMain/                      # Shared code for all platforms
│   │   │   ├── kotlin/ru/assistant/aicwl/
│   │   │   │   ├── App.kt                   # Main app entry point
│   │   │   │   ├── chat/
│   │   │   │   │   ├── agent/              # Domain layer - Business logic
│   │   │   │   │   │   └── ChatAgent.kt     # Main chat orchestrator
│   │   │   │   │   ├── config/             # Configuration
│   │   │   │   │   │   ├── AppConfig.kt    # expect/actual config
│   │   │   │   │   │   ├── ModelConfig.kt  # Model configurations
│   │   │   │   │   ├── data/               # Data layer
│   │   │   │   │   │   ├── unified/        # Unified data models
│   │   │   │   │   │   ├── zai/            # Z.AI specific models
│   │   │   │   │   │   ├── qwen/           # Qwen specific models
│   │   │   │   │   │   ├── oreal/          # Oreal specific models
│   │   │   │   │   │   ├── ChatApiModels.kt
│   │   │   │   │   │   ├── ChatRequestParameters.kt
│   │   │   │   │   │   ├── StructuredResponse.kt
│   │   │   │   │   │   └── ChatHistoryRepository.kt
│   │   │   │   │   ├── network/            # Networking
│   │   │   │   │   │   └── ChatApiClient.kt # Ktor HTTP client
│   │   │   │   │   ├── preferences/        # User preferences
│   │   │   │   │   │   └── UserPreferencesRepository.kt
│   │   │   │   │   ├── provider/           # AI Provider abstraction
│   │   │   │   │   │   ├── AIProvider.kt   # Provider interface
│   │   │   │   │   │   ├── AIProviderFactory.kt
│   │   │   │   │   │   ├── ProviderType.kt # Provider enum
│   │   │   │   │   │   ├── model/          # Unified model config
│   │   │   │   │   │   │   └── AIModelConfig.kt
│   │   │   │   │   │   ├── zai/            # Z.AI implementation
│   │   │   │   │   │   ├── qwen/           # Qwen implementation
│   │   │   │   │   │   └── oreal/          # Oreal implementation
│   │   │   │   │   ├── prompt/             # System prompt management
│   │   │   │   │   │   ├── SystemPromptConfig.kt
│   │   │   │   │   │   ├── data/           # Prompt repositories
│   │   │   │   │   │   ├── domain/         # Prompt use cases
│   │   │   │   │   │   └── ui/             # Prompt settings UI
│   │   │   │   │   ├── tokens/             # Token tracking
│   │   │   │   │   │   ├── TokenTracker.kt
│   │   │   │   │   │   ├── TokenCounter.kt
│   │   │   │   │   │   ├── TokenUsage.kt
│   │   │   │   │   │   └── TokenStorage.kt # expect/actual
│   │   │   │   │   ├── ui/                 # Presentation layer
│   │   │   │   │   │   ├── ChatScreen.kt   # Main chat UI
│   │   │   │   │   │   ├── ChatViewModel.kt # State management
│   │   │   │   │   │   ├── ViewModelFactory.kt
│   │   │   │   │   │   └── components/     # Reusable UI components
│   │   │   │   │   └── utils/              # Utilities
│   │   │   │   │       ├── Logger.kt       # expect/actual
│   │   │   │   │       ├── PlatformUtils.kt # expect/actual
│   │   │   │   │       └── PlatformTime.kt # expect/actual
│   │   │   │   ├── Greeting.kt             # Demo greeting
│   │   │   │   └── Platform.kt             # Platform info expect
│   │   │   └── composeResources/           # Shared resources
│   │   ├── androidMain/                    # Android-specific code
│   │   │   ├── kotlin/ru/assistant/aicwl/
│   │   │   │   ├── MainActivity.kt         # Android entry point
│   │   │   │   └── chat/                   # Android implementations
│   │   │   │       ├── config/AppConfig.android.kt
│   │   │   │       ├── data/               # Android data implementations
│   │   │   │       ├── preferences/        # Android preferences
│   │   │   │       ├── tokens/             # Android token storage
│   │   │   │       ├── ui/                 # Android UI specifics
│   │   │   │       └── utils/              # Android utilities
│   │   │   └── res/                        # Android resources
│   │   ├── iosMain/                        # iOS-specific code
│   │   │   ├── kotlin/ru/assistant/aicwl/
│   │   │   │   ├── MainViewController.kt   # iOS entry point
│   │   │   │   └── chat/                   # iOS implementations
│   │   │   │       ├── config/             # iOS config loading
│   │   │   │       ├── data/               # iOS data implementations
│   │   │   │       ├── preferences/        # iOS preferences
│   │   │   │       ├── tokens/             # iOS token storage
│   │   │   │       └── utils/              # iOS utilities
│   │   ├── jvmMain/                        # Desktop-specific code
│   │   │   ├── kotlin/ru/assistant/aicwl/
│   │   │   │   ├── main.kt                 # Desktop entry point
│   │   │   │   └── chat/                   # Desktop implementations
│   │   │   │       ├── config/AppConfig.jvm.kt
│   │   │   │       └── utils/              # Desktop utilities
│   │   └── commonTest/                     # Shared tests
│   └── build.gradle.kts                    # ComposeApp build config
├── iosApp/                                 # iOS native wrapper
│   └── iosApp/                             # SwiftUI integration
├── gradle/                                 # Gradle configuration
│   └── libs.versions.toml                  # Version catalog
├── build.gradle.kts                        # Root build script
├── settings.gradle.kts                     # Project settings
├── config.properties                       # Desktop config (API keys)
└── local.properties                        # Local config (API keys)
```

---

## Core Components Analysis

### Application Entry Points

#### `/composeApp/src/commonMain/kotlin/ru/assistant/aicwl/App.kt`
- **Purpose**: Main Compose application entry point
- **Component**: `App()` composable
- **Function**: Renders `ChatScreen` with MaterialTheme wrapper

#### Platform-Specific Entry Points

**Android**: `/composeApp/src/androidMain/kotlin/ru/assistant/aicwl/MainActivity.kt`
- Extends `ComponentActivity`
- Sets Compose content with `App()`
- Handles Android lifecycle

**iOS**: `/composeApp/src/iosMain/kotlin/ru/assistant/aicwl/MainViewController.kt`
- Creates `UIKitViewController`
- Bridges Compose to iOS SwiftUI
- Handles iOS-specific configuration

**Desktop**: `/composeApp/src/jvmMain/kotlin/ru/assistant/aicwl/main.kt`
- `main()` function for desktop
- Window configuration and Compose setup

### Domain Layer - Chat Agent

#### `/composeApp/src/commonMain/kotlin/ru/assistant/aicwl/chat/agent/ChatAgent.kt`

**Class**: `ChatAgent`
- **Purpose**: Main business logic orchestrator for AI chat
- **Key Methods**:
  - `chat(message, modelId, parameters, customSystemPrompt)`: Single message without history
  - `chatWithHistory(message, modelId, conversationHistory, parameters, ...)`: Chat with context
  - `chatWithProvider(message, providerType, modelId, ...)`: Provider-specific chat
  - `sendRequest(...)`: Internal method to execute requests via AIProvider
  - `formatErrorMessage(exception)`: User-friendly error messages

**Singleton Functions**:
- `getChatAgent(tokenTracker)`: Get/create singleton instance
- `initializeChatAgent(tokenTracker)`: Initialize with token tracking
- `chatAgent`: Deprecated property (use `getChatAgent()`)

**Key Features**:
- Provider inference from model ID
- System prompt injection
- Token tracking integration
- Error handling and formatting

### Data Layer - API Client

#### `/composeApp/src/commonMain/kotlin/ru/assistant/aicwl/chat/network/ChatApiClient.kt`

**Class**: `ChatApiClient`
- **Purpose**: HTTP client for Z.AI Chat API
- **Key Methods**:
  - `sendChatRequest(modelId, messages, parameters)`: Main API call
  - `sendUserMessage(modelId, userMessage)`: Simplified single message
  - `close()`: Cleanup resources

**Configuration**:
- Ktor HttpClient with:
  - 2 min connection timeout
  - 5 min request timeout
  - 5 min socket timeout
  - JSON content negotiation
- Authorization header (no "Bearer" prefix for Z.AI)

**Singleton**: `chatApiClient` instance

### Data Layer - Unified Models

#### `/composeApp/src/commonMain/kotlin/ru/assistant/aicwl/chat/data/unified/`

**Purpose**: Provider-agnostic data models for multi-provider support

**Key Files**:
- `UnifiedChatRequest.kt`: Standardized request format
- `UnifiedChatResponse.kt`: Standardized response format
- `UnifiedChatMessage.kt`: Standardized message format

### Provider Layer

#### `/composeApp/src/commonMain/kotlin/ru/assistant/aicwl/chat/provider/AIProvider.kt`

**Interface**: `AIProvider`
- **Purpose**: Abstract interface for all AI providers
- **Key Methods**:
  - `getProviderType()`: Get provider enum
  - `getProviderName()`: Human-readable name
  - `getAvailableModels()`: List supported models
  - `getDefaultModel()`: Get default model
  - `isConfigured()`: Check API key
  - `sendChatRequest(request)`: Execute chat request
  - `isValidModel(modelId)`: Validate model ID
  - `getEndpointInfo()`: Debug info
  - `close()`: Cleanup

**Implementations**:
- `ZAIProvider`: Z.AI integration
- `QwenProvider`: Qwen/HuggingFace integration
- `OrealProvider`: Oreal/HuggingFace integration

#### `/composeApp/src/commonMain/kotlin/ru/assistant/aicwl/chat/provider/AIProviderFactory.kt`

**Object**: `AIProviderFactory`
- **Purpose**: Factory for creating provider instances
- **Method**: `createProvider(providerType)`: Returns appropriate provider

### Configuration Layer

#### `/composeApp/src/commonMain/kotlin/ru/assistant/aicwl/chat/config/AppConfig.kt`

**Expect Object**: `AppConfig`
- **Purpose**: Platform-specific configuration loading
- **Properties**:
  - `zApiKey`: Z.AI API key
  - `zApiEndpoint`: Z.AI endpoint URL
  - `qwenApiKey`: Qwen API key
  - `qwenApiEndpoint`: Qwen endpoint URL
  - `orealApiKey`: Oreal API key
  - `orealApiEndpoint`: Oreal endpoint URL

**Actual Implementations**:
- `AppConfig.android.kt`: Loads from `local.properties`
- `AppConfig.ios.kt`: Loads from local file or Info.plist
- `AppConfig.jvm.kt`: Loads from `config.properties`

#### `/composeApp/src/commonMain/kotlin/ru/assistant/aicwl/chat/config/ModelConfig.kt`

**Object**: `ModelConfig`
- **Purpose**: Model-specific configurations and constants
- **Contains**: Model IDs, endpoints, parameters

#### `/composeApp/src/commonMain/kotlin/ru/assistant/aicwl/chat/provider/model/AIModelConfig.kt`

**Object**: `AIModelConfig`
- **Purpose**: Central registry of all AI models
- **Methods**:
  - `getAllModels()`: List all models
  - `getModelsByProvider(provider)`: Filter by provider
  - `getDefaultModelForProvider(provider)`: Get default
  - `getModelByUniqueId(uniqueId)`: Find by ID
- **Properties**:
  - `defaultModel`: Default model instance

**Model Tiers**:
- `SENIOR`: Most powerful (glm-4.7)
- `MIDDLE`: Balanced (glm-4.5v, glm-4.7-flash)
- `JUNIOR`: Fastest (glm-4.5-air)

### Token Tracking Layer

#### `/composeApp/src/commonMain/kotlin/ru/assistant/aicwl/chat/tokens/TokenTracker.kt`

**Class**: `TokenTracker`
- **Purpose**: Track token usage and costs
- **Key Methods**:
  - `recordUsage(usage)`: Record token usage
  - `recordFromApi(apiUsage, model, timestamp)`: Record from API response
  - `resetStatistics()`: Clear all stats
  - `getCurrentStatistics()`: Get current stats
  - `getStatisticsForModel(modelId)`: Model-specific stats
  - `getLastMessageTokenInfo()`: Get last message info

**State**: `StateFlow<TokenStatistics>` for reactive updates

**Singleton**: `getTokenTracker(storage)`

#### `/composeApp/src/commonMain/kotlin/ru/assistant/aicwl/chat/tokens/TokenCounter.kt`

**Object**: `TokenCounter`
- **Purpose**: Estimate tokens and calculate costs
- **Key Methods**:
  - `estimateTokens(text)`: Estimate token count
  - `estimateTextTokens(text, model)`: Model-specific estimate
  - `calculateCost(promptTokens, completionTokens, model)`: Calculate cost
  - `formatTokenCount(count)`: Format for display
  - `formatCost(cost)`: Format cost string

### Data Models

#### `/composeApp/src/commonMain/kotlin/ru/assistant/aicwl/chat/data/ChatRequestParameters.kt`

**Data Class**: `ChatRequestParameters`
- **Purpose**: Parameters for AI generation control
- **Properties**:
  - `doSample`: Boolean (default: true)
  - `temperature`: Float? (0-2)
  - `topP`: Float? (0-1)
  - `maxTokens`: Int?
  - `stream`: Boolean (default: false)
  - `thinking`: ThinkingConfig
  - `n`: Int (default: 1)

**Presets** (companion object):
- `fantasyPlus()`: Extremely creative (temp=1.9)
- `fantasy()`: Very creative (temp=1.2)
- `creative()`: Creative (temp=0.8)
- `technical()`: Deterministic (temp=0.2)
- `balanced()`: Balanced (temp=0.5)
- `fast()`: Fast (temp=0.0)

#### `/composeApp/src/commonMain/kotlin/ru/assistant/aicwl/chat/data/StructuredResponse.kt`

**Data Class**: `StructuredAiResponse`
- **Purpose**: Structured JSON response from AI
- **Properties**:
  - `status`: ResponseStatus?
  - `summary`: String
  - `reasoning`: String
  - `actionItems`: List<String>
  - `content`: String
  - `highlights`: List<String>
  - `suggestions`: List<String>
  - `questions`: List<String>
  - `meta`: ResponseMeta?
  - `questionNumber`: Int?
  - `totalQuestions`: Int?

**Key Methods**:
- `tryParse(rawResponse)`: Parse JSON with error recovery
- `repairTruncatedJson(json)`: Fix truncated JSON
- `extractJson(response)`: Extract JSON from text

**Enums**:
- `ResponseStatus`: SUCCESS, ERROR, NEEDS_CLARIFICATION
- `MessageType`: PLAIN_TEXT, STRUCTURED, ERROR

### Repository Layer

#### `/composeApp/src/commonMain/kotlin/ru/assistant/aicwl/chat/data/ChatHistoryRepository.kt`

**Interface**: `ChatHistoryRepository`
- **Purpose**: Manage chat history persistence
- **Methods**:
  - `saveChatHistory(history)`: Save history
  - `getChatHistory()`: Load history
  - `clearChatHistory()`: Clear history
  - `isChatHistoryEnabled()`: Check if enabled

**Implementation**: `ChatHistoryRepositoryImpl`

#### `/composeApp/src/commonMain/kotlin/ru/assistant/aicwl/chat/preferences/UserPreferencesRepository.kt`

**Interface**: `UserPreferencesRepository`
- **Purpose**: User settings persistence
- **Methods**:
  - `saveChatHistoryEnabled(enabled)`: Save setting
  - `isChatHistoryEnabled()`: Check setting

---

## Presentation/UI Layer

### Chat Screen

#### `/composeApp/src/commonMain/kotlin/ru/assistant/aicwl/chat/ui/ChatScreen.kt`

**Composable**: `ChatScreen(viewModel)`
- **Purpose**: Main chat interface
- **Components**:
  - `ChatTopBar`: Model selector, profile selector, actions
  - `LazyColumn`: Message list
  - `ChatInputField`: Text input with send button
  - `TokenStatsPanel`: Token statistics dialog
  - `PromptSettingsScreen`: Settings screen

**Message Components**:
- `EnhancedChatMessageItem`: Renders enhanced messages
- `UserMessageBubble`: User message styling
- `AssistantMessageBubble`: AI message styling
- `ErrorMessageBubble`: Error message styling
- `StructuredResponseCard`: Structured JSON display
- `LoadingIndicator`: Loading animation

**Input Features**:
- Enter to send
- Shift+Enter for new line
- Token estimation display
- Business analyst mode toggle

### Chat ViewModel

#### `/composeApp/src/commonMain/kotlin/ru/assistant/aicwl/chat/ui/ChatViewModel.kt`

**Class**: `ChatViewModel`
- **Purpose**: Manage chat state and business logic
- **State**: `StateFlow<ChatUiState>`

**Key Methods**:
- `sendMessage()`: Send message to AI
- `sendSuggestion(suggestion)`: Send suggestion click
- `clearChat()`: Clear all messages
- `selectModel(model)`: Change AI model
- `selectProvider(provider)`: Change provider
- `selectProfile(profile)`: Change temperature profile
- `updateInputText(text)`: Update input field
- `toggleBusinessAnalystMode()`: Toggle business mode
- `resetTokenStatistics()`: Clear token stats

**State Properties** (`ChatUiState`):
- `enhancedMessages`: List of messages
- `inputText`: Current input
- `selectedModel`: Current model
- `selectedProvider`: Current provider
- `selectedProfile`: Temperature profile
- `isLoading`: Loading state
- `isBusinessAnalystMode`: Business mode flag
- `businessAnalystHistory`: Interview history
- `fixedTotalQuestions`: Fixed question count
- `tokenStatistics`: Token stats
- `currentInputEstimate`: Input token estimate

### ViewModel Factory

#### `/composeApp/src/commonMain/kotlin/ru/assistant/aicwl/chat/ui/ViewModelFactory.kt`

**Expect Function**: `chatViewModel()`
- **Purpose**: Create ChatViewModel instance
- **Actual Implementations**:
  - `ViewModelFactory.android.kt`: Android-specific
  - `ViewModelFactory.ios.kt`: iOS-specific
  - `ViewModelFactory.jvm.kt`: Desktop-specific

---

## Key Relationships & Data Flow

### Message Flow

```
User Input
    │
    ▼
ChatInputField (UI)
    │
    ▼
ChatViewModel.sendMessage()
    │
    ▼
ChatAgent.chatWithHistory()
    │
    ▼
AIProvider.sendChatRequest()
    │
    ▼
ChatApiClient (HTTP Request)
    │
    ▼
AI Provider API
    │
    ▼
API Response
    │
    ▼
Parse Response (JSON)
    │
    ▼
EnhancedChatMessage.fromAiResponse()
    │
    ▼
Update UI State
    │
    ▼
Display in ChatScreen
```

### State Management Flow

```
User Action
    │
    ▼
ViewModel Method
    │
    ▼
Update MutableStateFlow
    │
    ▼
StateFlow.emit()
    │
    ▼
collectAsStateWithLifecycle() (UI)
    │
    ▼
Recomposition
    │
    ▼
Updated UI
```

### Provider Selection Flow

```
User selects model
    │
    ▼
ChatViewModel.selectModel(model)
    │
    ▼
Update selectedModel in state
    │
    ▼
Next message uses selectedModel.modelId
    │
    ▼
ChatAgent infers provider from modelId
    │
    ▼
AIProviderFactory.createProvider(providerType)
    │
    ▼
Provider-specific request
```

### Configuration Loading Flow

```
App Start
    │
    ▼
AppConfig (expect)
    │
    ├─────────────┬─────────────┬─────────────┐
    ▼             ▼             ▼             ▼
Android        iOS           JVM          (Platform)
(local.props)  (File/Info)   (config.props)
    │             │             │
    └─────────────┴─────────────┴─────────────┘
                    │
                    ▼
API Keys Available
    │
                    ▼
ChatApiClient / Providers Ready
```

---

## AI/Chat Integration

### Supported AI Providers

#### 1. Z.AI (Primary Provider)

**Models**:
- `glm-4.7`: Senior tier, most powerful
- `glm-4.5v`: Middle tier, vision capable
- `glm-4.5-air`: Junior tier, fastest
- `glm-4.7-flash`: Free model (limited time)

**Endpoint**: `https://api.z.ai/api/coding/paas/v4/chat/completions`

**Pricing** (per 1M tokens):
- glm-4.7: $0.60 input, $2.20 output
- glm-4.5v: $0.60 input, $1.80 output
- glm-4.5-air: $0.20 input, $1.10 output
- glm-4.7-flash: FREE

#### 2. Qwen (HuggingFace)

**Models**:
- `Qwen2.5-0.5B-Instruct`: Junior tier

**Integration**: Via HuggingFace Inference API

#### 3. Oreal (HuggingFace)

**Models**:
- `OREAL-7B-SFT`: Middle tier

**Integration**: Via HuggingFace Inference API

### Chat Modes

#### Standard Chat Mode
- Single messages without persistent context
- System prompt injected automatically
- Token tracking enabled

#### Conversation History Mode
- Maintains context across messages
- History saved to persistent storage
- Automatic history management

#### Business Analyst Mode
- Interactive requirements gathering
- Structured responses with questions
- Progress tracking (question X of Y)
- Clarification requests
- Final summary generation

### System Prompt Management

**Location**: `/composeApp/src/commonMain/kotlin/ru/assistant/aicwl/chat/prompt/SystemPromptConfig.kt`

**Features**:
- Default system prompt for general assistance
- Business analyst mode specialized prompt
- Custom prompt override via settings
- Dynamic prompt injection

**Modes**:
- Standard: General AI assistant
- Business Analyst: Requirements gathering specialist

### Token Management

**Token Tracking**:
- Real-time token counting
- Cost estimation per model
- Persistent statistics storage
- Per-model breakdown
- Request history tracking

**Token Storage** (Platform-specific):
- Android: SharedPreferences
- iOS: UserDefaults
- Desktop: Local file

---

## AI Agent Guide

### Quick Reference for AI Agents

This section provides essential information for AI agents working on this project.

### File Locations by Feature

#### Chat Core
- **Chat Agent**: `/composeApp/src/commonMain/kotlin/ru/assistant/aicwl/chat/agent/ChatAgent.kt`
- **Chat Screen**: `/composeApp/src/commonMain/kotlin/ru/assistant/aicwl/chat/ui/ChatScreen.kt`
- **Chat ViewModel**: `/composeApp/src/commonMain/kotlin/ru/assistant/aicwl/chat/ui/ChatViewModel.kt`

#### AI Providers
- **Provider Interface**: `/composeApp/src/commonMain/kotlin/ru/assistant/aicwl/chat/provider/AIProvider.kt`
- **Provider Factory**: `/composeApp/src/commonMain/kotlin/ru/assistant/aicwl/chat/provider/AIProviderFactory.kt`
- **Provider Type**: `/composeApp/src/commonMain/kotlin/ru/assistant/aicwl/chat/provider/ProviderType.kt`
- **Model Config**: `/composeApp/src/commonMain/kotlin/ru/assistant/aicwl/chat/provider/model/AIModelConfig.kt`

#### Data Layer
- **API Client**: `/composeApp/src/commonMain/kotlin/ru/assistant/aicwl/chat/network/ChatApiClient.kt`
- **Request Parameters**: `/composeApp/src/commonMain/kotlin/ru/assistant/aicwl/chat/data/ChatRequestParameters.kt`
- **Structured Response**: `/composeApp/src/commonMain/kotlin/ru/assistant/aicwl/chat/data/StructuredResponse.kt`
- **Chat History**: `/composeApp/src/commonMain/kotlin/ru/assistant/aicwl/chat/data/ChatHistoryRepository.kt`

#### Configuration
- **App Config**: `/composeApp/src/commonMain/kotlin/ru/assistant/aicwl/chat/config/AppConfig.kt`
- **Model Config**: `/composeApp/src/commonMain/kotlin/ru/assistant/aicwl/chat/config/ModelConfig.kt`
- **System Prompt**: `/composeApp/src/commonMain/kotlin/ru/assistant/aicwl/chat/prompt/SystemPromptConfig.kt`

#### Token Tracking
- **Token Tracker**: `/composeApp/src/commonMain/kotlin/ru/assistant/aicwl/chat/tokens/TokenTracker.kt`
- **Token Counter**: `/composeApp/src/commonMain/kotlin/ru/assistant/aicwl/chat/tokens/TokenCounter.kt`
- **Token Usage**: `/composeApp/src/commonMain/kotlin/ru/assistant/aicwl/chat/tokens/TokenUsage.kt`
- **Token Storage**: `/composeApp/src/commonMain/kotlin/ru/assistant/aicwl/chat/tokens/TokenStorage.kt`

### Common Patterns to Follow

#### Adding a New AI Provider

1. **Create Provider Implementation**:
```kotlin
// /composeApp/src/commonMain/kotlin/ru/assistant/aicwl/chat/provider/newprovider/NewProvider.kt
class NewProvider : AIProvider {
    override fun getProviderType() = ProviderType.NEW_PROVIDER
    override fun getProviderName() = "New Provider"
    override fun getAvailableModels() = listOf(/* models */)
    override suspend fun sendChatRequest(request: UnifiedChatRequest): Result<UnifiedChatResponse>
    // ... other methods
}
```

2. **Add Provider Type**:
```kotlin
// /composeApp/src/commonMain/kotlin/ru/assistant/aicwl/chat/provider/ProviderType.kt
enum class ProviderType {
    ZAI, QWEN, OREAL, NEW_PROVIDER
}
```

3. **Add to Factory**:
```kotlin
// /composeApp/src/commonMain/kotlin/ru/assistant/aicwl/chat/provider/AIProviderFactory.kt
fun createProvider(providerType: ProviderType): AIProvider {
    return when (providerType) {
        ProviderType.NEW_PROVIDER -> NewProvider()
        // ... existing providers
    }
}
```

4. **Add Models to Registry**:
```kotlin
// /composeApp/src/commonMain/kotlin/ru/assistant/aicwl/chat/provider/model/AIModelConfig.kt
private val newProviderModels = listOf(
    UnifiedAIModel(/* config */)
)
```

5. **Add Configuration**:
```kotlin
// /composeApp/src/commonMain/kotlin/ru/assistant/aicwl/chat/config/AppConfig.kt
expect object AppConfig {
    val newProviderApiKey: String
    val newProviderApiEndpoint: String
}
```

6. **Implement Platform-Specific Config**:
```kotlin
// /composeApp/src/androidMain/kotlin/.../chat/config/AppConfig.android.kt
actual object AppConfig {
    actual val newProviderApiKey: String
        get() = BuildConfig.NEW_PROVIDER_API_KEY
    // ...
}
```

#### Adding a New Feature to Chat

1. **Update ViewModel State**:
```kotlin
// ChatViewModel.kt
data class ChatUiState(
    // Add new state property
    val newFeatureEnabled: Boolean = false
)
```

2. **Add ViewModel Method**:
```kotlin
fun toggleNewFeature() {
    _uiState.value = _uiState.value.copy(
        newFeatureEnabled = !_uiState.value.newFeatureEnabled
    )
}
```

3. **Update UI**:
```kotlin
// ChatScreen.kt
// Add UI component that interacts with the feature
```

#### Adding Platform-Specific Code

Use the expect/actual pattern:

```kotlin
// commonMain/kotlin/ru/assistant/aicwl/chat/utils/PlatformUtils.kt
expect object PlatformUtils {
    fun platformSpecificFunction(): String
}

// androidMain/kotlin/ru/assistant/aicwl/chat/utils/PlatformUtils.android.kt
actual object PlatformUtils {
    actual fun platformSpecificFunction(): String = "Android"
}

// iosMain/kotlin/ru/assistant/aicwl/chat/utils/PlatformUtils.ios.kt
actual object PlatformUtils {
    actual fun platformSpecificFunction(): String = "iOS"
}
```

### Important Conventions

#### Code Style
- **Kotlin coding conventions**: Follow official Kotlin style guide
- **Naming**: Use camelCase for functions, PascalCase for classes
- **Comments**: KDoc for public APIs
- **Logging**: Use `Logger` utility, not `println()`

#### Architecture Rules
1. **No platform code in commonMain**: Use expect/actual
2. **Repository interfaces in commonMain**: Implementations in platform-specific
3. **ViewModels only in commonMain**: Use factories for creation
4. **State flows for reactive state**: Use `StateFlow` for all state

#### Dependency Rules
- **Presentation Layer** can depend on Domain and Data
- **Domain Layer** has no dependencies on other layers
- **Data Layer** can depend on Domain but not Presentation

#### Error Handling
- Use `Result<T>` for operations that can fail
- Return user-friendly error messages
- Log technical errors with context
- Never expose API keys in logs

#### Testing Considerations
- Write testable code with dependency injection
- Keep business logic out of Composables
- Use interfaces for repositories
- Avoid singletons where possible

### Common Tasks

#### Adding a New Temperature Profile

```kotlin
// /composeApp/src/commonMain/kotlin/ru/assistant/aicwl/chat/config/ModelConfig.kt
enum class TemperatureProfile(
    val displayName: String,
    val description: String,
    val parameters: ChatRequestParameters
) {
    // Add new profile
    CUSTOM("Custom", "Custom profile", ChatRequestParameters(
        temperature = 0.7f,
        // ... other params
    ))
}
```

#### Modifying the System Prompt

```kotlin
// /composeApp/src/commonMain/kotlin/ru/assistant/aicwl/chat/prompt/SystemPromptConfig.kt
object SystemPromptConfig {
    private const val DEFAULT_SYSTEM_PROMPT = """
        Your modified prompt here
    """.trimIndent()
}
```

#### Adding Token Tracking to a New Feature

```kotlin
// In your code that makes API calls
tokenTracker?.let { tracker ->
    tracker.recordFromApi(
        apiUsage = Usage(/* token data */),
        model = model,
        timestamp = currentTimeMillis()
    )
}
```

### Build Commands

```bash
# Android
./gradlew :composeApp:installDebug

# Desktop
./gradlew :composeApp:run
./gradlew :composeApp:packageDistributionForCurrentOS

# iOS
./gradlew :composeApp:assembleFramework
# Then open iosApp/ in Xcode
```

### Testing

```bash
# Run common tests
./gradlew :composeApp:allTests

# Run Android tests
./gradlew :composeApp:connectedAndroidTest
```

### Key Dependencies

```kotlin
// From libs.versions.toml
kotlin-multiplatform = 2.3.0
compose-multiplatform = 1.9.3
kotlinx-serialization = 1.8.0
ktor-client-core = 3.0.3
androidx-lifecycle-viewmodel-compose = 2.8.0
```

---

## Appendix

### Configuration Files

#### `config.properties` (Desktop)
```properties
# Z.AI Configuration
llm.z.api.key=your_zai_key_here

# Qwen Configuration
llm.qwen.api.key=your_qwen_key_here

# Oreal Configuration
llm.oreal.api.key=your_oreal_key_here
```

#### `local.properties` (Android)
```properties
llm.z.api.key=your_zai_key_here
sdk.dir=/path/to/android/sdk
```

### API Endpoints

- **Z.AI**: `https://api.z.ai/api/coding/paas/v4/chat/completions`
- **Qwen**: HuggingFace Inference API
- **Oreal**: HuggingFace Inference API

### Model Pricing

See `AIModelConfig.kt` for current pricing. All prices per 1M tokens in USD.

---

## Document Info

**Version**: 1.0
**Last Updated**: 2025-01-26
**Project**: AICWL (AI Chat with Kotlin)
**Architecture**: Clean Architecture + MVVM
**Platforms**: Android, iOS, Desktop
