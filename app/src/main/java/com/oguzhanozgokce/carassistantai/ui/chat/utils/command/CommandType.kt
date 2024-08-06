package com.oguzhanozgokce.carassistantai.ui.chat.utils.command

enum class CommandType(val type: String) {
    OPEN("open"),
    NAVIGATE("navigate"),
    TWEET("tweet"),
    CALL("call"),
    MESSAGE("message"),
    ALARM("alarm"),
    SHOW("show"),
    UNKNOWN("unknown");

    companion object {
        fun fromString(type: String): CommandType {
            return entries.firstOrNull { it.type.equals(type, ignoreCase = true) } ?: UNKNOWN
        }
    }
}
