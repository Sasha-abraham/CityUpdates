package com.sasha.cityupdates.models

data class ChatMessage(
    val message: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)