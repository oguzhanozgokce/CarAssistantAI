package com.oguzhanozgokce.carassistantai.data.model.message

data class Message(
    val content: String,
    val isBotMessage: Boolean,
    val isLoading: Boolean = false
)

