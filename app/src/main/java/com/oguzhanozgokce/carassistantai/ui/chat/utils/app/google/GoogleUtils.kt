package com.oguzhanozgokce.carassistantai.ui.chat.utils.app.google

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.oguzhanozgokce.carassistantai.R
import com.oguzhanozgokce.carassistantai.common.Constant.GOOGLE_SEARCH_URL
import com.oguzhanozgokce.carassistantai.ui.chat.utils.app.AppUtils

object GoogleUtils {
    private const val TAG = "GoogleUtils"

    fun openLink(context: Context, url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
          Toast.makeText(context, context.getString(R.string.no_app_found_to_open_link), Toast.LENGTH_SHORT).show()
        }
    }
}
