package com.oguzhanozgokce.carassistantai.data.model.gemini

data class Chat(
    val prompt: String,
    val bitmap: String,
    val isFromUser: Boolean
)