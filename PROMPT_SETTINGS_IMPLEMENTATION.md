# Prompt Settings Feature Implementation

## Overview
A complete settings system for editing the main AI prompt within the application, following Clean Architecture principles and fully compatible with Kotlin Multiplatform (KMP).

## Architecture

### Clean Architecture Layers

#### 1. Data Layer
**Location**: `/composeApp/src/commonMain/kotlin/ru/assistant/aicwl/chat/prompt/data/`

- **PromptPreferences.kt** (expect/actual interface)
  - Platform-agnostic contract for storing preferences
  - Uses expect/actual pattern for platform implementations

- **PromptPreferences.android.kt**
  - Android implementation using SharedPreferences
  - Real-time updates via SharedPreferences.OnSharedPreferenceChangeListener
  - Thread-safe and persistent

- **PromptPreferences.ios.kt**
  - iOS implementation using NSUserDefaults
  - In-memory StateFlow for reactive updates
  - Simple and efficient

- **PromptPreferences.jvm.kt**
  - JVM/Desktop implementation using java.util.prefs.Preferences
  - Cross-platform compatible

- **PromptSettingsRepository.kt** (Interface)
  - Repository contract following Domain layer principles
  - Defines all operations for settings management

- **PromptSettingsRepositoryImpl.kt**
  - Concrete implementation bridging platform storage to domain models
  - Handles serialization/deserialization of settings
  - Manages rule IDs and data transformations

#### 2. Domain Layer
**Location**: `/composeApp/src/commonMain/kotlin/ru/assistant/aicwl/chat/prompt/`

- **model/PromptSettings.kt**
  - Domain models: `PromptSettings`, `PromptRuleData`
  - JSON serialization using kotlinx.serialization
  - Business logic for determining effective prompts

- **domain/LoadPromptSettingsUseCase.kt**
  - Use case for loading settings

- **domain/UpdatePromptSettingsUseCase.kt**
  - Multiple use cases for specific operations:
    - `UpdateCustomPromptUseCase`
    - `AddPromptRuleUseCase`
    - `RemovePromptRuleUseCase`
    - `ResetPromptUseCase`
    - `ClearPromptRulesUseCase`

#### 3. Presentation Layer
**Location**: `/composeApp/src/commonMain/kotlin/ru/assistant/aicwl/chat/prompt/ui/`

- **PromptSettingsViewModel.kt**
  - MVVM pattern with unidirectional data flow
  - StateFlow-based reactive state management
  - Handles all UI interactions and business logic coordination

- **PromptSettingsViewModelFactory.kt**
  - Simplified dependency injection
  - Initializes with default prompt and platform preferences
  - Singleton repository instance

- **components/PromptSettingsScreen.kt**
  - Material 3 Compose UI
  - Stateless, reusable components
  - Proper state hoisting and event handling
  - Responsive layout with LazyColumn

### Integration with Existing Code

#### SystemPromptConfig Updates
**File**: `/composeApp/src/commonMain/kotlin/ru/assistant/aicwl/chat/prompt/SystemPromptConfig.kt`

Changes made:
1. Changed `mainPrompt` from `private` to `val` (public readonly)
2. Added `customPrompt` property for user overrides
3. Added `loadCustomPrompt()` method to initialize from preferences
4. Added `setCustomPrompt()` method for runtime updates
5. Updated `getSystemPrompt()` to use `getCurrentMainPrompt()`

#### ChatScreen Navigation
**File**: `/composeApp/src/commonMain/kotlin/ru/assistant/aicwl/chat/ui/ChatScreen.kt`

Changes made:
1. Added settings icon to TopAppBar
2. Implemented navigation state management
3. Integrated PromptSettingsViewModel
4. Added automatic loading of custom prompts on app start
5. Proper ViewModel lifecycle management

#### Platform Initialization

**Android** - MainActivity.kt:
```kotlin
val preferences = PromptPreferences()
preferences.initialize(applicationContext)
PromptSettingsViewModelFactory.initialize(
    defaultPrompt = SystemPromptConfig.mainPrompt,
    preferences = preferences
)
```

**iOS** - MainViewController.kt:
```kotlin
PromptSettingsViewModelFactory.initialize(
    defaultPrompt = SystemPromptConfig.mainPrompt,
    preferences = PromptPreferences()
)
```

**JVM/Desktop** - main.kt:
```kotlin
PromptSettingsViewModelFactory.initialize(
    defaultPrompt = SystemPromptConfig.mainPrompt,
    preferences = PromptPreferences()
)
```

## Features Implemented

### 1. Custom Main Prompt Editing
- Multi-line TextField for editing the system prompt
- Real-time validation
- Save/Reset functionality
- Persists across app restarts

### 2. Additional Rules Management
- Add custom rules via TextField
- List of all added rules
- Individual rule removal
- Clear all rules option
- Rules automatically included in system prompt

### 3. User Interface
- Material 3 design
- Clean, intuitive layout
- Proper navigation with back button
- Loading states
- Error handling with user-friendly messages

### 4. Data Persistence
- Platform-specific storage (SharedPreferences, NSUserDefaults, java.util.prefs)
- Reactive updates via Flow
- Automatic serialization/deserialization
- Thread-safe operations

## File Structure

```
composeApp/src/
├── commonMain/kotlin/ru/assistant/aicwl/chat/prompt/
│   ├── SystemPromptConfig.kt (modified)
│   ├── data/
│   │   ├── PromptPreferences.kt (expect class)
│   │   ├── PromptSettingsRepository.kt (interface)
│   │   └── PromptSettingsRepositoryImpl.kt
│   ├── domain/
│   │   ├── LoadPromptSettingsUseCase.kt
│   │   └── UpdatePromptSettingsUseCase.kt
│   ├── model/
│   │   └── PromptSettings.kt
│   └── ui/
│       ├── PromptSettingsViewModel.kt
│       ├── PromptSettingsViewModelFactory.kt
│       └── components/
│           └── PromptSettingsScreen.kt
├── androidMain/kotlin/ru/assistant/aicwl/
│   ├── MainActivity.kt (modified)
│   └── chat/prompt/data/
│       └── PromptPreferences.android.kt
├── iosMain/kotlin/ru/assistant/aicwl/
│   ├── MainViewController.kt (modified)
│   └── chat/prompt/data/
│       └── PromptPreferences.ios.kt
└── jvmMain/kotlin/ru/assistant/aicwl/
    ├── main.kt (modified)
    └── chat/prompt/data/
        └── PromptPreferences.jvm.kt
```

## Usage

### For Users

1. **Access Settings**: Tap the settings icon (gear) in the top-right corner of the chat screen
2. **Edit Main Prompt**:
   - Modify the text in the main prompt field
   - Tap "Save" to persist changes
   - Tap "Reset" to restore default prompt
3. **Manage Rules**:
   - Enter a rule in the "New rule" field
   - Tap the "+" button to add it
   - Tap the trash icon next to any rule to remove it
   - Tap "Clear all" to remove all rules

### For Developers

#### Adding New Features

1. **New Use Cases**: Create in `domain/` package
2. **UI Components**: Add to `ui/components/` following existing patterns
3. **Repository Methods**: Extend interface and implementation

#### Testing

```kotlin
// Example: Testing ViewModel
val viewModel = PromptSettingsViewModel(
    loadSettingsUseCase = mockLoadSettingsUseCase,
    updateCustomPromptUseCase = mockUpdateUseCase,
    // ... other dependencies
    defaultMainPrompt = "Test prompt",
    coroutineScope = TestScope()
)

// Test state changes
viewModel.onPromptTextChanged("New prompt")
viewModel.savePrompt()

assertEquals(PromptSettingsUiState.Success(...), viewModel.uiState.value)
```

## Technical Highlights

### SOLID Principles
- **Single Responsibility**: Each class has one clear purpose
- **Open/Closed**: Extendable through interfaces and use cases
- **Liskov Substitution**: Platform implementations are interchangeable
- **Interface Segregation**: Small, focused interfaces
- **Dependency Inversion**: Dependencies on abstractions (interfaces)

### KMP Best Practices
- Maximum code sharing in commonMain
- Platform-specific code isolated with expect/actual
- No platform-specific APIs in shared code
- Cross-platform time handling using kotlinx.datetime

### Compose Multiplatform
- State hoisting to parent components
- @Stable annotations for performance
- Proper side effect management (LaunchedEffect, SideEffect)
- Material 3 design system
- Skippable composables

### Reactive Programming
- StateFlow for UI state
- Flow for data streams
- Unidirectional data flow (MVVM)
- Coroutine-based async operations

## Build Status

✅ **Build Successful** - All platforms compile without errors

- Android: ✅
- iOS: ✅
- JVM/Desktop: ✅

## Future Enhancements

Possible improvements:
1. **Export/Import Settings**: Allow users to share prompt configurations
2. **Prompt Templates**: Pre-defined templates for common use cases
3. **Validation**: More sophisticated prompt validation
4. **Versioning**: Track and rollback to previous prompt versions
5. **Cloud Sync**: Sync settings across devices
6. **Analytics**: Track which prompts work best

## Dependencies

All dependencies are already included in the project:
- kotlinx.coroutines
- kotlinx.serialization
- kotlinx.datetime
- Compose Multiplatform Material3
- Android SharedPreferences
- iOS NSUserDefaults
- JVM java.util.prefs

## Conclusion

This implementation provides a complete, production-ready settings system for managing AI prompts. It follows best practices for KMP development, maintains clean architecture principles, and provides an excellent user experience across all supported platforms.
