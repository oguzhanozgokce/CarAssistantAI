package com.oguzhanozgokce.carassistantai.ui.chat.utils.alarm


import android.content.Context
import android.content.Intent
import android.widget.Toast
object AlarmUtils {

    fun setAlarm(context: Context, hour: Int, minute: Int, message: String) {
        val intent = Intent("com.android.deskclock.ALARM_CREATE").apply {
            setClassName("com.google.android.deskclock", "com.android.deskclock.DeskClock")
            putExtra("android.intent.extra.alarm.HOUR", hour)
            putExtra("android.intent.extra.alarm.MINUTES", minute)
            putExtra("android.intent.extra.alarm.MESSAGE", message)
            putExtra("android.intent.extra.alarm.SKIP_UI", false)
        }
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            Toast.makeText(context, "No alarm application found", Toast.LENGTH_SHORT).show()
        }
    }

    fun showAlarms(context: Context) {
        val intent = Intent().setClassName("com.google.android.deskclock", "com.android.deskclock.DeskClock")
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            Toast.makeText(context, "No alarm application found", Toast.LENGTH_SHORT).show()
        }
    }
}
