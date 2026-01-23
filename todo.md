# TODO - AICWL Project

## Оставшиеся задачи и улучшения

Этот список содержит задачи, которые не были выполнены в ходе текущего рефакторинга, но могут быть полезны для дальнейшего развития проекта.

---

## 🔴 ВЫСОКИЙ ПРИОРИТЕТ

### 1. Интеграция UserPreferencesRepository в UI

**Почему:** Репозиторий создан, но не используется в ChatViewModel и UI. Пользователь не может сохранить выбор провайдера и модели.

**Задачи:**
- [ ] Добавить UserPreferencesRepository в ChatViewModel
- [ ] Реализовать сохранение/загрузку выбранного провайдера
- [ ] Реализовать сохранение/загрузку выбранной модели
- [ ] Добавить UI для переключения между провайдерами
- [ ] Показывать только сконфигурированные провайдеры в UI

**Файлы:**
- `ChatViewModel.kt`
- UI экраны выбора модели/провайдера

**Пример кода:**
```kotlin
class ChatViewModel(
    private val chatHistoryRepository: ChatHistoryRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {
    init {
        loadUserPreferences()
    }

    private fun loadUserPreferences() {
        viewModelScope.launch {
            val provider = userPreferencesRepository.getSelectedProvider()
            val model = userPreferencesRepository.getDefaultModel()
            _uiState.value = _uiState.value.copy(
                selectedProvider = provider,
                selectedModel = model
            )
        }
    }
}
```

---

### 2. Добавить Unit-тесты для провайдеров

**Почему:** Критическая бизнес-логика (мапперы, провайдеры) не покрыта тестами. Любое изменение может сломать функциональность.

**Задачи:**
- [ ] Тесты для ZAIRequestMapper/ZAIResponseMapper
- [ ] Тесты для OpenAIRequestMapper/OpenAIResponseMapper
- [ ] Тесты для AnthropicRequestMapper/AnthropicResponseMapper
- [ ] Тесты для AIProviderFactory
- [ ] Тесты для ApiKeyValidator
- [ ] Тесты для UserPreferencesRepository (моки)

**Файлы:**
- `composeApp/src/commonTest/kotlin/ru/assistant/aicwl/chat/provider/`
- `composeApp/src/commonTest/kotlin/ru/assistant/aicwl/chat/data/`

---

## 🟡 СРЕДНИЙ ПРИОРИТЕТ

### 3. Миграция на единый MessageRole

**Почему:** В проекте существует дубликат enum `MessageRole` (в `ChatApiModels.kt` и `unified/UnifiedChatMessage.kt`). Это создаёт путаницу и потенциальные ошибки.

**Текущая ситуация:**
- `ru.assistant.aicwl.chat.data.MessageRole` — используется в UI и ChatViewModel
- `ru.assistant.aicwl.chat.data.unified.MessageRole` — используется в провайдерах

**Задачи:**
- [ ] Заменить использование `data.MessageRole` на `unified.MessageRole` во всех файлах
- [ ] Удалить дубликат из `ChatApiModels.kt`
- [ ] Обновить импорты в:
  - `ChatViewModel.kt`
  - `EnhancedChatMessage.kt`
  - `InterviewHistoryEntry.kt`
  - Все UI компоненты

**Риски:** Средние — требуется изменение множества файлов

---

### 4. Реализовать DI контейнер

**Почему:** В настоящее время зависимости создаются вручную (singleton объекты, фабрики). DI упростит тестирование и управление lifecycle.

**Рекомендуемые библиотеки:**
- **Koin** — проще, подходит для KMP
- **Kodein** — альтернатива Koin

**Задачи:**
- [ ] Добавить Koin в `build.gradle.kts`
- [ ] Создать модули DI:
  - `dataProviderModule` — репозитории
  - `domainModule` — use cases, ChatAgent
  - `presentationModule` — ViewModels
- [ ] Заменить singleton объекты на DI
- [ ] Управлять lifecycle через DI

**Пример:**
```kotlin
val dataProviderModule = module {
    single { AIProviderFactory }
    single { chatAgent }
    single<UserPreferencesRepository> { UserPreferencesRepositoryImpl(get()) }
}
```

---

### 5. Централизованная обработка ошибок

**Почему:** Каждый провайдер обрабатывает ошибки по-своему. Нет единообразного пользовательского опыта.

**Задачи:**
- [ ] Создать `ErrorHandler` с общими типами ошибок
- [ ] Определить `AiError` sealed class:
  - `NetworkError`
  - `AuthenticationError`
  - `RateLimitError`
  - `InvalidRequestError`
  - `ServerError`
  - `UnknownError`
- [ ] Обеспечить локализованные сообщения для пользователей
- [ ] Добавить retry логику для временных ошибок

**Файлы:**
- `ErrorHandler.kt`
- `AiError.kt`

---

### 6. Добавить аналитику использования

**Почему:** Полезно знать, какие модели используются, как часто, сколько запросов делается. Это поможет оптимизировать расходы.

**Задачи:**
- [ ] Создать `AnalyticsService`
- [ ] Логировать:
  - Количество запросов по провайдерам
  - Количество запросов по моделям
  - Ошибки и их типы
  - Время ответа
- [ ] Добавить estimation стоимости запросов (уже есть в `UnifiedAIModel`)

---

## 🟢 НИЗКИЙ ПРИОРИТЕТ

### 7. Streaming Support (SSE)

**Почему:** Некоторые провайдеры поддерживают потоковые ответы. Это улучшит UX для длинных ответов.

**Задачи:**
- [ ] Добавить метод `streamChatRequest()` в `AIProvider`
- [ ] Реализовать SSE для каждого провайдера
- [ ] Добавить Flow<String> для потоковых ответов
- [ ] Обновить UI для отображения потока

**Сложность:** Высокая

---

### 8. Добавить токены в качестве оплаты

**Почему:** Возможно, пользователи захотят использовать свои собственные API ключи.

**Задачи:**
- [ ] Добавить UI для ввода API ключей
- [ ] Сохранять ключи в UserPreferencesRepository (безопасно!)
- [ ] Добавить валидацию ключей при вводе
- [ ] Показывать стоимость каждого запроса

**Безопасность:**
- Использовать зашифрованное хранилище (Keychain на iOS, Keystore на Android)

---

### 9. Улучшить документацию

**Почему:** Хорошая документация помогает новым разработчикам быстрее разобраться.

**Задачи:**
- [ ] Добавить KDoc для всех публичных методов
- [ ] Создать ARCHITECTURE.md с описанием архитектуры
- [ ] Добавить CONTRIBUTING.md
- [ ] Создать диаграммы (Mermaid) для:
  - Flow данных от UI к API
  - Структура провайдеров
  - Lifecycle компонентов

---

### 10. Оптимизация производительности

**Почему:** При большом количестве сообщений в истории приложение может замедляться.

**Задачи:**
- [ ] Пагинация истории чата
- [ ] Ленивая загрузка сообщений
- [ ] Кэширование ответов AI
- [ ] Оптимизация JSON сериализации/десериализации
- [ ] Профилирование памяти и CPU

---

## 📋 ЧЕК-ЛИСТ ДЛЯ DEPLOYMENT

Перед релизом новой версии убедитесь, что:

- [ ] Все API ключи работают (проверить на каждом провайдере)
- [ ] UserPreferencesRepository сохраняет настройки
- [ ] История чата сохраняется и загружается корректно
- [ ] Логи в production не содержат чувствительных данных
- [ ] HttpClient закрывается при выходе из приложения
- [ ] UI не зависает при долгих запросах
- [ ] Обработка ошибок показывает понятные сообщения
- [ ] Приложение работает на всех трёх платформах (Android, JVM, iOS)

---

## 📊 Метрики качества

Целевые показатели для проекта:

| Метрика | Текущее | Цель |
|---------|---------|------|
| Test Coverage | ~0% | >70% |
| Code Duplication | Medium | Low |
| Build Time (Android) | ? | <5 min |
| APK Size | ? | <50 MB |
| Crash Rate | ? | <0.1% |

---

## 🔄 План следующего спринта

Рекомендуемый порядок задач:

1. **Интеграция UserPreferencesRepository** (2-3 дня)
2. **Unit-тесты для провайдеров** (3-4 дня)
3. **Централизованная обработка ошибок** (2 дня)
4. **DI контейнер** (2-3 дня)

Итого: **~2 недели** работы

---

*Обновлено: 2026-01-23*
