package com.oguzhanozgokce.carassistantai.ui.chat.utils.app.mail

import android.content.Context
import android.content.Intent
import com.oguzhanozgokce.carassistantai.R
import com.oguzhanozgokce.carassistantai.common.Constant.MAIL_WEB_URL
import com.oguzhanozgokce.carassistantai.ui.chat.utils.app.AppUtils


object MailUtils {

    fun openMailApp(context: Context, email: String, subject: String, body: String, sendBotMessage: (String) -> Unit) {
        val mailIntent = Intent(Intent.ACTION_SEND).apply {
            type = "message/rfc822"
            putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }

        try {
            val chooser = Intent.createChooser(mailIntent, context.getString(R.string.choose_email_app))
            context.startActivity(chooser)
        } catch (e: Exception) {
            sendBotMessage(context.getString(R.string.no_email_app_found))
            AppUtils.openAppWeb(context, { message ->
                sendBotMessage(message)
            }, MAIL_WEB_URL, R.string.no_email_or_browser_found)
        }
    }
}