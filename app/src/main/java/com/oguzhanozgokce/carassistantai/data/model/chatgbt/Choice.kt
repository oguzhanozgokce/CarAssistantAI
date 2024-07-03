package com.oguzhanozgokce.carassistantai.data.model.chatgbt

data class Choice(
    val index: Int,
    val message: ChatMessage,
    val finish_reason: String
)