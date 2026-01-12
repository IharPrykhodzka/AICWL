package ru.assistant.aicwl.chat.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun chatViewModel(): ChatViewModel = remember { ChatViewModel() }
