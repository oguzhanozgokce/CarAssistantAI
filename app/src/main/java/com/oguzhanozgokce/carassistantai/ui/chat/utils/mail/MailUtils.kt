package com.oguzhanozgokce.carassistantai.ui.chat.utils.mail

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log



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
                sendBotMessage("No email app found to open. Trying to open web mail...")
                openWebMail(context, sendBotMessage)
            }
        } catch (e: Exception) {
            sendBotMessage("An unexpected error occurred while opening the email app.")
            openWebMail(context, sendBotMessage)
        }
    }

    private fun openWebMail(context: Context, sendBotMessage: (String) -> Unit) {
        try {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://mail.google.com"))
            if (webIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(webIntent)
            } else {
                sendBotMessage("No email app or web browser found to open the web mail.")
                Log.e(TAG, "No web browser found to open the web mail.")
            }
        } catch (e: Exception) {
            sendBotMessage("An unexpected error occurred while opening the web mail.")
            Log.e(TAG, "An unexpected error occurred while opening the web mail", e)
        }
    }
}


