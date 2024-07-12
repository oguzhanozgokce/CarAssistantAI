package com.oguzhanozgokce.carassistantai.ui.chat.utils.app.photo

import android.content.Context
import com.oguzhanozgokce.carassistantai.R
import com.oguzhanozgokce.carassistantai.common.Constant.PHOTOS_PACKAGE_NAME
import com.oguzhanozgokce.carassistantai.common.Constant.PHOTOS_WEB_URL
import com.oguzhanozgokce.carassistantai.ui.chat.utils.app.AppUtils

object PhotoUtils {
    fun openGooglePhotos(context: Context, sendBotMessage: (String) -> Unit) {
        AppUtils.openApp(context, PHOTOS_PACKAGE_NAME, PHOTOS_WEB_URL, sendBotMessage, R.string.google_photos_not_installed)
    }
}
