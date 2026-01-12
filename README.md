# AICWL

**AI Chat with Kotlin** — кроссплатформенное приложение для общения с AI-ассистентом на базе Kotlin Multiplatform и Compose Multiplatform.

## О проекте

AICWL — это современный чат-клиент с интеграцией的大型языковой модели от Z.AI (智谱AI). Приложение позволяет общаться с AI-ассистентом на трёх платформах с единым кодом:

- 🤖 **Android** (API 27+)
- 🍎 **iOS** (arm64 и x64 simulator)
- 🖥️ **Desktop** (Windows, macOS, Linux)

## Возможности

### Функциональность
- 💬 **Чат в реальном времени** с AI-ассистентом
- 🔄 **Динамическое переключение моделей** GLM:
  - `glm-4.7` (Senior) — наиболее мощная, но медленная
  - `glm-4.6` (Middle) — сбалансированная (по умолчанию)
  - `glm-4.5-air` (Junior) — быстрая, но менее точная
- 📜 **История сообщений** с автоскроллом
- 🎨 **Material Design 3** интерфейс
- ⌨️ **Горячие клавиши** (Enter — отправить, Shift+Enter — новая строка)

### Архитектурные особенности
- 🏗️ **MVVM** паттерн с четким разделением слоёв
- 📦 **Clean Architecture** — Data, Domain, Presentation слои
- 🔌 **Expect/Actual** для платформенно-зависимой функциональности
- 🌐 **Ktor Client** для сетевых запросов

## Технологический стек

### Core
| Технология | Версия |
|------------|--------|
| Kotlin Multiplatform | 2.3.0 |
| Compose Multiplatform | 1.9.3 |
| Kotlinx Serialization | 1.8.0 |

### Networking
- **Ktor Client** 3.0.3
  - `ktor-client-core`
  - `ktor-client-okhttp` (Android)
  - `ktor-client-darwin` (iOS/macOS)
  - `ktor-client-cio` (Desktop)

### UI
- Material Design 3
- Compose UI Components
- Cross-platform theming

## Структура проекта

```
AICWL/
├── composeApp/                 # Общий модуль (shared)
│   ├── src/
│   │   ├── commonMain/        # Общий код для всех платформ
│   │   │   ├── kotlin/        # ViewModels, UI, Data models
│   │   │   └── resources/     # Общие ресурсы
│   │   ├── androidMain/       # Android-специфичный код
│   │   ├── iosMain/           # iOS-специфичный код
│   │   └── jvmMain/           # Desktop-специфичный код
│   └── build.gradle.kts       # Конфигурация composeApp
├── iosApp/                     # iOS приложение (wrapper)
│   └── iosApp/                # SwiftUI интеграция
├── gradle/                    # Gradle конфигурация
│   ├── libs.versions.toml     # Version catalog
│   └── wrapper/
├── build.gradle.kts           # Root build script
├── settings.gradle.kts        # Project settings
└── README.md                  # Этот файл
```

## Начало работы

### Предварительные требования

- **JDK** 17+
- **Android Studio** (для Android) или **IntelliJ IDEA** (для Desktop)
- **Xcode** 15+ (для iOS)

### Конфигурация API ключа

Для работы приложения необходим API ключ от Z.AI:

#### Android
```properties
# в local.properties
zai.api.key=ваш_ключ_здесь
```

#### Desktop
```properties
# в config.properties (в корне проекта)
zai.api.key=ваш_ключ_здесь
```

#### iOS

⚠️ **ВАЖНО:** `Info.plist` и `local_config.txt` добавлены в `.gitignore` для безопасности.

Есть три способа установки API ключа (в порядке приоритета):

**Способ 1: Через локальный файл (рекомендуется — безопасно для git)**
```bash
# В Xcode:
# 1. Скопируйте iosApp/iosApp/local_config.txt.example в local_config.txt
# 2. Добавьте ВАШ API ключ (только ключ, без комментов)
# 3. Добавьте local_config.txt в Xcode проект (File -> Add Files)
# 4. Убедитесь, что local_config.txt НЕ попадает в git (.gitignore)
```

**Способ 2: Программно (для быстрой разработки)**
```kotlin
// в composeApp/src/iosMain/kotlin/ru/assistant/aicwl/MainViewController.kt
ApiKeyHelper.setApiKey("ваш_ключ_здесь")
```

**Способ 3: Через Info.plist (не рекомендуется для публичных репо)**
```xml
<!-- в iosApp/iosApp/Info.plist -->
<key>LLM_Z_API_KEY</key>
<string>ваш_ключ_здесь</string>
```

### Сборка и запуск

#### Android
```bash
# Запуск на эмуляторе/устройстве
./gradlew :composeApp:installDebug

# Сборка APK
./gradlew :composeApp:assembleDebug
```

#### Desktop
```bash
# Запуск приложения
./gradlew :composeApp:run

# Сборка нативного дистрибутива
./gradlew :composeApp:packageDistributionForCurrentOS
```

#### iOS
```bash
# Сборка через Gradle (требует Xcode)
./gradlew :composeApp:assembleFramework

# Или откройте iosApp/ в Xcode и запустите оттуда
```

## Архитектура

### Слои приложения

```
┌─────────────────────────────────────────┐
│         Presentation Layer              │
│  ┌─────────────┐    ┌──────────────┐   │
│  │ Compose UI  │◄───│ ChatViewModel│   │
│  └─────────────┘    └──────────────┘   │
└─────────────────────────────────────────┘
                    │
                    ▼
┌─────────────────────────────────────────┐
│           Domain Layer                  │
│  ┌─────────────────────────────────┐   │
│  │      Chat Agent / Business      │   │
│  └─────────────────────────────────┘   │
└─────────────────────────────────────────┘
                    │
                    ▼
┌─────────────────────────────────────────┐
│            Data Layer                   │
│  ┌─────────────┐    ┌──────────────┐   │
│  │ API Models  │    │   Ktor       │   │
│  │             │◄───│   Client     │   │
│  └─────────────┘    └──────────────┘   │
└─────────────────────────────────────────┘
```

### Ключевые компоненты

- **ChatViewModel** — управление состоянием UI и бизнес-логикой
- **ChatApiClient** — HTTP клиент для коммуникации с API
- **PlatformConfiguration** — expect/actual для платформенных настроек
- **Logger** — кроссплатформенное логирование

## API Z.AI

### Эндпоинт
```
POST https://api.z.ai/api/coding/paas/v4/chat/completions
```

### Модели
| Модель | Описание |
|--------|----------|
| `glm-4.7` | Самая мощная, медленная |
| `glm-4.6` | Рекомендуемая, сбалансированная |
| `glm-4.5-air` | Быстрая, менее точная |

### Авторизация
```
Authorization: <api_key>
```
*Без префикса "Bearer"*

## Безопасность

- 🔒 API ключи хранятся в локальных конфигурационных файлах (не в git)
- 🚫 `.gitignore` настроен на исключение чувствительных данных
- ✅ Обработка ошибок без раскрытия пользователю
- 🛡️ Маскирование API ключей в логах

## Требования к платформам

| Платформа | Мин. версия |
|-----------|-------------|
| Android | API 27 (Android 8.1) |
| iOS | iOS 13+ |
| Desktop | JDK 17+ |

## Лицензия

```
Copyright © 2025. Все права защищены.
```

## Контакты

По вопросам и предложениям создавайте issues в репозитории проекта.
