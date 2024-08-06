package com.oguzhanozgokce.carassistantai.ui.chat.utils.command

enum class CommandTarget(val target: String) {
    YOUTUBE("youtube"),
    GALLERY("gallery"),
    CAMERA("camera"),
    CLOCK("clock"),
    WHATSAPP("whatsapp"),
    STOPWATCH("stopwatch"),
    TIMER("timer"),
    SAVE_NOTE("save_note"),
    MAIL("mail"),
    GOOGLE_SEARCH("google_search"),
    GOOGLE_MAPS("googlemaps"),
    TWITTER("twitter");

    companion object {
        fun fromString(target: String): CommandTarget {
            return entries.firstOrNull { it.target.equals(target, ignoreCase = true) } ?: TWITTER
        }
    }
}
