package com.oguzhanozgokce.carassistantai.data.model.json

data class Command(
    val type: String,
    val target: String,
    val action: String,
    val parameters: Parameters
)
