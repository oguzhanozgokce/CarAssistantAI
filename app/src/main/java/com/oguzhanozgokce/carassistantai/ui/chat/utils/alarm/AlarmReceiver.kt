package com.oguzhanozgokce.carassistantai.ui.chat.utils.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val message = intent.getStringExtra("ALARM_MESSAGE") ?: "Alarm!"
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }
}