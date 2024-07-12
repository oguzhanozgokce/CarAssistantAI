package com.oguzhanozgokce.carassistantai.ui.chat.utils.mail

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.oguzhanozgokce.carassistantai.R


object MailUtils {

    private const val TAG = "MailUtils"

    fun openMailApp(context: Context, sendBotMessage: (String) -> Unit) {
        try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
            }

            if (intent.resolveActivity(context.packageManager) != null) {
                context.startActivity(intent)
            } else {
                sendBotMessage(context.getString(R.string.no_email_app_found))
                openWebMail(context, sendBotMessage)
            }
        } catch (e: Exception) {
            sendBotMessage(context.getString(R.string.unexpected_error_email_app))
            openWebMail(context, sendBotMessage)
        }
    }

    private fun openWebMail(context: Context, sendBotMessage: (String) -> Unit) {
        try {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://mail.google.com"))
            if (webIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(webIntent)
            } else {
                sendBotMessage(context.getString(R.string.no_email_or_browser_found))
                Log.e(TAG, context.getString(R.string.no_browser_found_log))
            }
        } catch (e: Exception) {
            sendBotMessage(context.getString(R.string.unexpected_error_web_mail))
            Log.e(TAG, context.getString(R.string.unexpected_error_web_mail_log), e)
        }
    }
}



