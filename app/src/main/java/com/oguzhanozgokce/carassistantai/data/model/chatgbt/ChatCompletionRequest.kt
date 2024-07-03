package com.oguzhanozgokce.carassistantai.data.model.chatgbt



data class ChatCompletionRequest(
    val model: String,
    val messages: List<ChatMessage>,
    val temperature: Double = 0.7
)