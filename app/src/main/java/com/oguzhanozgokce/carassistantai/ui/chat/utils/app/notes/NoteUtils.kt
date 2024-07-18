package com.oguzhanozgokce.carassistantai.ui.chat.utils.app.notes

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.oguzhanozgokce.carassistantai.R
import com.oguzhanozgokce.carassistantai.common.Constant.KEEP_PACKAGE_NAME
import com.oguzhanozgokce.carassistantai.common.Constant.KEEP_WEB_URL
import com.oguzhanozgokce.carassistantai.common.Constant.NOTE_UTILS_TAG
import com.oguzhanozgokce.carassistantai.ui.chat.utils.app.AppUtils

object NoteUtils {

    fun openKeep(context: Context, sendBotMessage: (String) -> Unit) {
        AppUtils.openApp(context, KEEP_PACKAGE_NAME, KEEP_WEB_URL, sendBotMessage, R.string.keep_app_not_found)
    }

    fun openKeepWithNote(context: Context, noteContent: String, sendBotMessage: (String) -> Unit) {
        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, noteContent)
                setPackage(KEEP_PACKAGE_NAME)
            }
            Log.d(NOTE_UTILS_TAG, intent.toString())
            context.startActivity(intent)
            sendBotMessage(context.getString(R.string.keep_opened_with_note))
        } catch (e: Exception) {
            AppUtils.openAppWeb(context, sendBotMessage, KEEP_WEB_URL, R.string.keep_app_not_found)
        }
    }
}