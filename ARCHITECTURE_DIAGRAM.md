# Multi-Model AI Architecture Diagram

## System Architecture Overview

```
┌─────────────────────────────────────────────────────────────────────────┐
│                           USER INTERFACE                                │
│  ┌──────────────────────────────────────────────────────────────────┐   │
│  │                        ChatScreen                                │   │
│  │  ┌────────────┐  ┌────────────┐  ┌────────────┐  ┌───────────┐   │   │
│  │  │  Provider  │  │   Model    │  │ Temperature│  │  Settings │   │   │
│  │  │  Selector  │  │  Selector  │  │  Selector  │  │           │   │   │
│  │  └─────┬──────┘  └─────┬──────┘  └─────┬──────┘  └─────┬─────┘   │   │
│  │        │               │               │               │         │   │
│  │        └───────────────┴───────────────┴───────────────┘         │   │
│  │                              │                                   │   │
│  │                         Chat List                                │   │
│  │                         Input Field                              │   │
│  └──────────────────────────────┼───────────────────────────────────┘   │
└─────────────────────────────────┼───────────────────────────────────────┘
                                  │
                                  ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                      PRESENTATION LAYER                                 │
│  ┌──────────────────────────────────────────────────────────────────┐   │
│  │                         ChatViewModel                            │   │
│  │  ┌────────────────────────────────────────────────────────────┐  │   │
│  │  │  State:                                                    │  │   │
│  │  │  - selectedProvider: ProviderType                          │  │   │
│  │  │  - selectedModel: UnifiedAIModel                           │  │   │
│  │  │  - messages: List<EnhancedChatMessage>                     │  │   │
│  │  │  - isLoading: Boolean                                      │  │   │
│  │  └────────────────────────────────────────────────────────────┘  │   │
│  │                                                                  │   │
│  │  Methods:                                                        │   │
│  │  - selectProvider(type)                                          │   │
│  │  - selectModel(modelId)                                          │   │
│  │  - sendMessage()                                                 │   │
│  │  - clearChat()                                                   │   │
│  └────────────────────────────────┬─────────────────────────────────┘   │
└───────────────────────────────────┼─────────────────────────────────────┘
                                    │
                                    ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                         DOMAIN LAYER                                    │
│  ┌──────────────────────────────────────────────────────────────────┐   │
│  │                          ChatAgent                               │   │
│  │  ┌────────────────────────────────────────────────────────────┐  │   │
│  │  │  Responsibilities:                                         │  │   │
│  │  │  - Build message history                                   │  │   │
│  │  │  - Add system prompts                                      │  │   │
│  │  │  - Coordinate with AIProvider                              │  │   │
│  │  │  - Handle errors                                           │  │   │
│  │  └────────────────────────────────────────────────────────────┘  │   │
│  └────────────────────────────────┬─────────────────────────────────┘   │
│                                   │                                     │
│  ┌────────────────────────────────▼─────────────────────────────────┐   │
│  │                     AIProvider (Interface)                       │   │
│  │  ┌────────────────────────────────────────────────────────────┐  │   │
│  │  │  + sendChatRequest(request): Result<Response>              │  │   │
│  │  │  + getAvailableModels(): List<UnifiedAIModel>              │  │   │
│  │  │  + isConfigured(): Boolean                                 │  │   │
│  │  └────────────────────────────────────────────────────────────┘  │   │
│  └───────────────┬──────────────────────┬───────────────────────────┘   │
│                  │                      │                               │
│      ┌───────────┴──────┐  ┌────────────┴──────────┐                    │
│      │                  │  │                       │                    │
│      ▼                  ▼  ▼                       ▼                    │
│  ┌────────┐      ┌──────────┐           ┌──────────────┐                │
│  │  ZAI   │      │  OpenAI  │           │  Anthropic   │                │
│  │Provider│      │ Provider │           │   Provider   │                │
│  └───┬────┘      └────┬─────┘           └──────┬───────┘                │
│      │                │                        │                        │
└──────┼────────────────┼────────────────────────┼────────────────────────┘
       │                │                        │
       ▼                ▼                        ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                          DATA LAYER                                     │
│                                                                         │
│  ┌──────────────────┐  ┌──────────────────┐  ┌────────────────────┐     │
│  │  ZAI Request     │  │  OpenAI Request  │  │ Anthropic Request  │     │
│  │  Mapper          │  │  Mapper          │  │ Mapper             │     │
│  └────────┬─────────┘  └────────┬─────────┘  └─────────┬──────────┘     │
│           │                     │                      │                │
│           ▼                     ▼                      ▼                │
│  ┌──────────────────┐  ┌──────────────────┐  ┌────────────────────┐     │
│  │  ZAI Response    │  │  OpenAI Response │  │ Anthropic Response │     │
│  │  Mapper          │  │  Mapper          │  │ Mapper             │     │
│  └──────────────────┘  └──────────────────┘  └────────────────────┘     │
│           │                     │                       │               │
│           └─────────────────────┴───────────────────────┘               │
│                                 │                                       │
│                          ┌──────▼───────┐                               │
│                          │ Unified Data │                               │
│                          │   Models     │                               │
│                          │              │                               │
│                          │ - Request    │                               │
│                          │ - Response   │                               │
│                          │ - Message    │                               │
│                          └──────────────┘                               │
└─────────────────────────────────────────────────────────────────────────┘
                                   │
                                   ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                     EXTERNAL APIS                                       │
│                                                                         │
│  ┌─────────────────┐  ┌─────────────────┐  ┌─────────────────────┐      │
│  │  Z.ai API       │  │  OpenAI API     │  │  Anthropic API      │      │
│  │  glm-4.7        │  │  GPT-4o         │  │  Claude 3.5 Sonnet  │      │
│  │  glm-4.7-flash  │  │  GPT-4o-mini    │  │  Claude 3.5 Haiku   │      │
│  │  glm-4.5-air    │  │  GPT-3.5-turbo  │  │  Claude 3 Opus      │      │
│  └─────────────────┘  └─────────────────┘  └─────────────────────┘      │
└─────────────────────────────────────────────────────────────────────────┘
```

## Data Flow Diagram

```
User Input: "Hello, how are you?"
    │
    ▼
ChatScreen (UI)
    │
    ▼
ChatViewModel.sendMessage()
    │
    ├─► Build messages (system prompt + history)
    │
    ├─► Get selected provider from state
    │
    ├─► Get selected model from state
    │
    ▼
ChatAgent.chat(message, provider, model)
    │
    ├─► Create UnifiedChatRequest
    │   {
    │     providerType: OPENAI,
    │     modelId: "gpt-4o-mini",
    │     messages: [
    │       { role: SYSTEM, content: "..." },
    │       { role: USER, content: "Hello..." }
    │     ],
    │     parameters: { temperature: 0.5, ... }
    │   }
    │
    ├─► AIProviderFactory.createProvider(OPENAI)
    │       │
    │       ▼
    │   OpenAIProvider instance
    │
    ├─► provider.sendChatRequest(request)
    │       │
    │       ▼
    │   OpenAIRequestMapper.toOpenAIRequest()
    │       │
    │       ▼
    │   OpenAIChatRequest
    │       │
    │       ▼
    │   HTTP POST to api.openai.com
    │       │
    │       ▼
    │   OpenAIChatResponse (raw)
    │       │
    │       ▼
    │   OpenAIResponseMapper.toUnifiedResponse()
    │       │
    │       ▼
    │   UnifiedChatResponse
    │       │
    ▼       │
Result.success(response)
    │
    ▼
Extract content: "I'm doing well, thank you!"
    │
    ▼
Update UI State
    │
    ▼
Display in ChatScreen
```

## Provider Switching Flow

```
User clicks provider selector
    │
    ▼
Show provider dropdown
    │
    ├─► Z.ai (configured) ✓
    ├─► OpenAI (configured) ✓
    └─► Anthropic (not configured) ✗
    │
User selects OpenAI
    │
    ▼
ChatViewModel.selectProvider(OPENAI)
    │
    ├─► Save to preferences
    │
    ├─► Get default model for OpenAI
    │       │
    │       ▼
    │   GPT-4o-mini
    │
    ├─► Update UI state
    │
    └─► Auto-select default model
    │
    ▼
UI updates:
  - Provider indicator shows "OpenAI"
  - Model selector shows OpenAI models
  - Chat context is preserved
    │
User sends message
    │
    ▼
Uses OpenAI GPT-4o-mini instead of Z.ai
```

## Model Selection Flow

```
User clicks model selector
    │
    ▼
Show models for selected provider
    │
    ▼
If provider is OpenAI, show:
  ├─► GPT-4o (Senior) - Most capable
  ├─► GPT-4o-mini (Middle) - Balanced
  └─► GPT-3.5-Turbo (Junior) - Fastest
    │
User selects GPT-4o
    │
    ▼
ChatViewModel.selectModel("gpt-4o")
    │
    ├─► Save to preferences (provider=OPENAI, model=gpt-4o)
    │
    ├─► Update UI state
    │
    └─► Notify user
    │
    ▼
Next message uses GPT-4o
```

## Error Handling Flow

```
ChatViewModel.sendMessage()
    │
    ▼
ChatAgent.chat()
    │
    ▼
Provider.sendChatRequest()
    │
    ├─► Success?
    │   │
    │   ├─► Yes → Return Result.success(response)
    │   │       │
    │   │       ▼
    │   │   Display response
    │   │
    │   └─► No → Return Result.failure(exception)
    │       │
    │       ▼
    │   Handle error based on type
    │       │
    │       ├─► 401 Unauthorized → "Invalid API key"
    │       ├─► 429 Rate Limit → "Too many requests"
    │       ├─► Timeout → "Request timeout, try faster model"
    │       └─► Network → "Connection error"
    │       │
    │       ▼
    │   Display error message in chat
    │
    ▼
User can retry or switch provider
```

## Configuration Loading Flow

```
App starts
    │
    ▼
AppConfig (expect/actual)
    │
    ├─► Android: Read from BuildConfig
    ├─► Desktop: Read from config.properties
    └─► iOS: Read from local_config.txt
    │
    ▼
Load API keys:
    ├─► zApiKey: "sk-..."
    ├─► openaiApiKey: "sk-..."
    └─► anthropicApiKey: "sk-ant-..."
    │
    ▼
AIProviderFactory.isProviderConfigured()
    │
    ├─► Z.ai: true (key present)
    ├─► OpenAI: true (key present)
    └─► Anthropic: false (key empty)
    │
    ▼
UI shows available providers
    │
    ▼
User can select from configured providers
```

## Preference Persistence Flow

```
User selects provider/model
    │
    ▼
ChatViewModel.selectProvider()
    │
    ▼
UserPreferencesRepository.setSelectedProvider()
    │
    ├─► Android: SharedPreferences
    ├─► Desktop: ~/.aicwl/user_preferences.json
    └─► iOS: UserDefaults
    │
    ▼
Stored locally
    │
    ▼
App restarts
    │
    ▼
Load saved preferences
    │
    ▼
Restore last selected provider/model
    │
    ▼
User sees same provider/model as before
```
