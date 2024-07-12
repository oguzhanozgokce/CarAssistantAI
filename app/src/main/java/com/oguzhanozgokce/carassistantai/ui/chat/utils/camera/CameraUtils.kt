package com.oguzhanozgokce.carassistantai.ui.chat.utils.camera

import android.content.Intent
import android.provider.MediaStore
import com.oguzhanozgokce.carassistantai.R
import com.oguzhanozgokce.carassistantai.common.Constant.REQUEST_IMAGE_CAPTURE
import com.oguzhanozgokce.carassistantai.ui.chat.view.ChatBotFragment

object CameraUtils {
    fun openCamera(fragment: ChatBotFragment) {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(fragment.requireContext().packageManager) != null) {
            fragment.startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
        } else {
            fragment.sendBotMessage(fragment.getString(R.string.camera_app_not_found))
        }
    }
}
