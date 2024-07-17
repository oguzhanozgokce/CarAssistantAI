package com.oguzhanozgokce.carassistantai.ui.chat.utils.app.whatsapp

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.oguzhanozgokce.carassistantai.BuildConfig
import com.oguzhanozgokce.carassistantai.R
import com.oguzhanozgokce.carassistantai.common.Constant.WHATSAPP_PACKAGE_NAME
import com.oguzhanozgokce.carassistantai.common.Constant.WHATSAPP_URL
import com.oguzhanozgokce.carassistantai.ui.chat.utils.app.AppUtils

object WhatsAppUtils {


    private fun openWhatsApp(context: Context, sendBotMessage: (String) -> Unit) {
        AppUtils.openApp(context, WHATSAPP_PACKAGE_NAME, WHATSAPP_URL, sendBotMessage, R.string.whatsapp_not_found)
    }

    fun openWhatsAppChatWithMyNumber(context: Context, sendBotMessage: (String) -> Unit) {
        val myPhoneNumber = BuildConfig.PHONE_NUMBER
        openWhatsAppChat(context, myPhoneNumber, sendBotMessage)
    }

    private fun openWhatsAppChat(context: Context, phoneNumber: String, sendBotMessage: (String) -> Unit) {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://wa.me/$phoneNumber")
                setPackage(WHATSAPP_PACKAGE_NAME)
            }
            try {
                context.startActivity(intent)
            } catch (e: Exception) {
                openWhatsApp(context, sendBotMessage)
            }
        } catch (e: Exception) {
            sendBotMessage(context.getString(R.string.whatsapp_not_found))
        }
    }
}