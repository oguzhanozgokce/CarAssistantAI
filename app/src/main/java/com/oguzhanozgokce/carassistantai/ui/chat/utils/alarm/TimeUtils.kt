package com.oguzhanozgokce.carassistantai.ui.chat.utils.alarm

object TimeUtils {

    fun extractTimeFromCommand(command: String): Pair<Int, Int>? {
        val regex = Regex("(\\d{1,2}):(\\d{2})")
        val matchResult = regex.find(command)
        return matchResult?.let {
            val (hour, minute) = it.destructured
            hour.toIntOrNull()?.let { h ->
                minute.toIntOrNull()?.let { m ->
                    return Pair(h, m)
                }
            }
        }
        return null
    }
}