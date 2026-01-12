package ru.assistant.aicwl.chat.ui

import androidx.compose.runtime.Composable

/**
 * Platform-specific ViewModel factory.
 * On Android: uses lifecycle ViewModel with proper scope.
 * On iOS/Desktop: uses remember for simple instance management.
 */
@Composable
expect fun chatViewModel(): ChatViewModel
