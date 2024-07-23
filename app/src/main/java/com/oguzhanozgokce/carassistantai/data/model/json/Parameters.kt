package com.oguzhanozgokce.carassistantai.data.model.json

data class Parameters(
    val query: String? = null,
    val destination: String? = null,
    val contactName: String? = null,
    val message: String? = null,
    val dateStr: String? = null,
    val time: String? = null,
    val username: String? = null,
    val placeType : String? = null,
    val seconds: Int? = null,
    val noteContent: String? = null,
    val url: String? = null
)
