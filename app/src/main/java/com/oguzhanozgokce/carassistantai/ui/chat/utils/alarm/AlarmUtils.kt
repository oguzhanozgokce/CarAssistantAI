package com.oguzhanozgokce.carassistantai.ui.chat.utils.alarm

import android.content.Context
import android.content.Intent
import android.provider.AlarmClock
import android.widget.Toast
import com.oguzhanozgokce.carassistantai.R
import com.oguzhanozgokce.carassistantai.common.Constant.DESK_CLOCK_CLASS_NAME
import com.oguzhanozgokce.carassistantai.common.Constant.DESK_CLOCK_PACKAGE_NAME

object AlarmUtils {

    fun setAlarm(context: Context, hour: Int, minute: Int, message: String, sendBotMessage: (String) -> Unit) {
        val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
            putExtra(AlarmClock.EXTRA_HOUR, hour)
            putExtra(AlarmClock.EXTRA_MINUTES, minute)
            putExtra(AlarmClock.EXTRA_MESSAGE, message)
            putExtra(AlarmClock.EXTRA_SKIP_UI, true)
        }
        try {
            context.startActivity(intent)
            sendBotMessage(context.getString(R.string.alarm_set_for, "$hour:$minute"))
        } catch (e: Exception) {
            Toast.makeText(context,R.string.alarm_app_not_found, Toast.LENGTH_SHORT).show()
        }
    }

    fun startStopwatch(context: Context, sendBotMessage: (String) -> Unit) {
        val intent = Intent(AlarmClock.ACTION_SET_TIMER).apply {
            putExtra(AlarmClock.EXTRA_SKIP_UI, true)
        }
        try {
            context.startActivity(intent)
            sendBotMessage(context.getString(R.string.stopwatch_started))
        } catch (e: Exception) {
            Toast.makeText(context, R.string.stopwatch_app_not_found, Toast.LENGTH_SHORT).show()
        }
    }

    fun startTimer(context: Context, seconds: Int, message: String, sendBotMessage: (String) -> Unit) {
        val intent = Intent(AlarmClock.ACTION_SET_TIMER).apply {
            putExtra(AlarmClock.EXTRA_LENGTH, seconds)
            putExtra(AlarmClock.EXTRA_MESSAGE, message)
            putExtra(AlarmClock.EXTRA_SKIP_UI, true)
        }
        try {
            context.startActivity(intent)
            sendBotMessage(context.getString(R.string.timer_set_for, seconds / 60, seconds % 60))
        } catch (e: Exception) {
            Toast.makeText(context, R.string.timer_app_not_found, Toast.LENGTH_SHORT).show()
        }
    }


    fun showAlarms(context: Context) {
        val intent = Intent().setClassName(DESK_CLOCK_PACKAGE_NAME, DESK_CLOCK_CLASS_NAME)
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            Toast.makeText(context,R.string.alarm_app_not_found, Toast.LENGTH_SHORT).show()
        }
    }
}
