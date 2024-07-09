package com.oguzhanozgokce.carassistantai.ui.chat.utils.photo

import android.content.ContentUris
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.ContextCompat
import com.oguzhanozgokce.carassistantai.ui.chat.view.ChatBotFragment

object PhotoUtils {

    fun getPhotosByDateRange(fragment: ChatBotFragment, startDate: Long, endDate: Long) {
        val readPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            android.Manifest.permission.READ_MEDIA_IMAGES
        } else {
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(fragment.requireContext(), readPermission)
            != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {
            fragment.requestStoragePermission()
            return
        }

        val context = fragment.requireContext()
        val contentResolver = context.contentResolver
        val uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI

        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATE_TAKEN
        )

        val selection =
            "${MediaStore.Images.Media.DATE_TAKEN} >= ? AND ${MediaStore.Images.Media.DATE_TAKEN} <= ?"
        val selectionArgs = arrayOf(startDate.toString(), endDate.toString())

        val sortOrder = "${MediaStore.Images.Media.DATE_TAKEN} DESC"

        val cursor: Cursor? =
            contentResolver.query(uri, projection, selection, selectionArgs, sortOrder)

        cursor?.use {
            val idColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val dateTakenColumn = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN)

            while (it.moveToNext()) {
                val id = it.getLong(idColumn)
                val dateTaken = it.getLong(dateTakenColumn)
                ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

                Log.e(ChatBotFragment.TAG, "Found photo with ID: $id, taken on: $dateTaken")
            }
        }
    }

    fun openGooglePhotos(fragment: ChatBotFragment) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("content://media/external/images/media")
            setClassName("com.google.android.apps.photos", "com.google.android.apps.photos.home.HomeActivity")
        }
        if (intent.resolveActivity(fragment.requireContext().packageManager) != null) {
            fragment.startActivity(intent)
        } else {
            fragment.sendBotMessage("Google Photos app not installed.")
        }
    }

}