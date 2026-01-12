package ru.assistant.aicwl

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform