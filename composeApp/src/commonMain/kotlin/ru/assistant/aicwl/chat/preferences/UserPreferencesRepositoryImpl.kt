package ru.assistant.aicwl.chat.preferences

/**
 * Expected declaration for platform-specific implementations.
 * Each platform provides its own implementation with platform-specific storage.
 *
 * Note: In KMP, expect classes cannot directly implement interfaces in the common source set.
 * The actual implementations on each platform will implement UserPreferencesRepository.
 */
expect class UserPreferencesRepositoryImpl {
    constructor()
}
