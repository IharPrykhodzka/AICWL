package ru.assistant.aicwl.chat.ui

import androidx.compose.runtime.Composable

/**
 * Платформенно-зависимая фабрика ViewModel.
 * На Android: использует lifecycle ViewModel с правильным scope.
 * На iOS/Desktop: использует remember для простого управления экземпляром.
 */
@Composable
expect fun chatViewModel(): ChatViewModel
