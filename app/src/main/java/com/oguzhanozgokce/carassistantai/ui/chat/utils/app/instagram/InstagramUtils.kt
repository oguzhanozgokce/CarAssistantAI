package com.oguzhanozgokce.carassistantai.ui.chat.utils.app.instagram

import android.content.Context
import com.oguzhanozgokce.carassistantai.R
import com.oguzhanozgokce.carassistantai.common.Constant.INSTAGRAM_PACKAGE_NAME
import com.oguzhanozgokce.carassistantai.common.Constant.INSTAGRAM_WEB_URL
import com.oguzhanozgokce.carassistantai.ui.chat.utils.app.AppUtils

object InstagramUtils {
    fun openInstagram(context: Context, sendBotMessage: (String) -> Unit) {
        AppUtils.openApp(context, INSTAGRAM_PACKAGE_NAME, INSTAGRAM_WEB_URL, sendBotMessage, R.string.error_opening_instagram_app)
    }
}