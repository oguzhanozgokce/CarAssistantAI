package com.oguzhanozgokce.carassistantai.ui.chat.utils.app

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri

object AppUtils {

    fun openApp(context: Context, packageName: String, appUrl: String, sendBotMessage: (String) -> Unit, errorMessageResId: Int) {
        val pm = context.packageManager
        try {
            pm.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
            val intent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                `package` = packageName
            }
            if (intent.resolveActivity(pm) != null) {
                context.startActivity(intent)
            } else {
                sendBotMessage(context.getString(errorMessageResId))
            }
        } catch (e: PackageManager.NameNotFoundException) {
            openAppWeb(context, sendBotMessage, appUrl, errorMessageResId)
        } catch (e: Exception) {
            sendBotMessage(context.getString(errorMessageResId))
        }
    }

    private fun openAppWeb(context: Context, sendBotMessage: (String) -> Unit, url: String, errorMessageResId: Int) {
        try {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(webIntent)
        } catch (e: Exception) {
            sendBotMessage(context.getString(errorMessageResId))
        }
    }

    fun openWebUrl(context: Context, url: String, onFailure: (String) -> Unit, errorMessageResId: Int) {
        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))

        try {
            context.startActivity(webIntent)
        } catch (e: Exception) {
            onFailure(context.getString(errorMessageResId))
        }
    }
}