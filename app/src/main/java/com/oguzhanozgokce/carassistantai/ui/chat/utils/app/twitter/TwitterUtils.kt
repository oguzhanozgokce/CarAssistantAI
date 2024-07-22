package com.oguzhanozgokce.carassistantai.ui.chat.utils.app.twitter

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.oguzhanozgokce.carassistantai.R
import com.oguzhanozgokce.carassistantai.common.Constant.TWITTER_PACKAGE_NAME
import com.oguzhanozgokce.carassistantai.common.Constant.TWITTER_SHARE_URL
import com.oguzhanozgokce.carassistantai.common.Constant.TWITTER_WEB_URL
import com.oguzhanozgokce.carassistantai.ui.chat.utils.app.AppUtils

object TwitterUtils {
    fun openTwitterApp(context: Context, sendBotMessage: (String) -> Unit) {
        AppUtils.openApp(context, TWITTER_PACKAGE_NAME, TWITTER_WEB_URL, sendBotMessage, R.string.error_opening_twitter_app)
    }
    fun tweet(context: Context, message: String, sendBotMessage: (String) -> Unit) {
       val tweetUrl = TWITTER_SHARE_URL + Uri.encode(message)
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(tweetUrl)).apply {
            setPackage(TWITTER_PACKAGE_NAME)
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            sendBotMessage(context.getString(R.string.error_opening_twitter_app))
        }
    }
}