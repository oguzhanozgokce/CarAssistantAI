package com.oguzhanozgokce.carassistantai.ui.chat.utils



import android.content.Intent
import android.provider.MediaStore
import com.oguzhanozgokce.carassistantai.ui.chat.ChatBotFragment

object CameraUtils {

    private const val REQUEST_IMAGE_CAPTURE = 1

    fun openCamera(fragment: ChatBotFragment) {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(fragment.requireContext().packageManager) != null) {
            fragment.startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
        } else {
            fragment.sendBotMessage("Camera application not found.")
        }
    }
}
