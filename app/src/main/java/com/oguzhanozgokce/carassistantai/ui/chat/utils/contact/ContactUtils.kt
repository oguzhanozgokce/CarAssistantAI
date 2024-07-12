package com.oguzhanozgokce.carassistantai.ui.chat.utils.contact

import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.provider.ContactsContract
import androidx.core.content.ContextCompat
import com.oguzhanozgokce.carassistantai.R
import com.oguzhanozgokce.carassistantai.ui.chat.view.ChatBotFragment

object ContactUtils {

    fun findContactAndCall(fragment: ChatBotFragment, contactName: String) {
        if (ContextCompat.checkSelfPermission(
                fragment.requireContext(),
                android.Manifest.permission.READ_CONTACTS
            ) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                fragment.requireContext(),
                android.Manifest.permission.CALL_PHONE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            fragment.requestContactPermissionsLauncher.launch(
                arrayOf(
                    android.Manifest.permission.READ_CONTACTS,
                    android.Manifest.permission.CALL_PHONE
                )
            )
            return
        }

        val contentResolver = fragment.requireContext().contentResolver
        val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )

        val cursor: Cursor? = contentResolver.query(
            uri,
            projection,
            "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} LIKE ?",
            arrayOf("%$contactName%"),
            null
        )

        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

                val name = it.getString(nameIndex)
                val number = it.getString(numberIndex)

                val messageSendingText = fragment.getString(R.string.contact_calling)
                fragment.sendBotMessage("$name $messageSendingText")
                makePhoneCall(fragment, number)
            } else {
                val contactNotFoundText = fragment.getString(R.string.contact_not_found, contactName)
                fragment.sendBotMessage(contactNotFoundText)
            }
        }
    }

    private fun makePhoneCall(fragment: ChatBotFragment, phoneNumber: String) {
        val callIntent = Intent(Intent.ACTION_CALL, Uri.parse("tel:$phoneNumber"))
        if (ContextCompat.checkSelfPermission(
                fragment.requireContext(),
                android.Manifest.permission.CALL_PHONE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fragment.startActivity(callIntent)
        } else {
            fragment.requestContactPermissionsLauncher.launch(
                arrayOf(
                    android.Manifest.permission.READ_CONTACTS,
                    android.Manifest.permission.CALL_PHONE
                )
            )
        }
    }

    fun findContactAndSendMessage(fragment: ChatBotFragment, contactName: String, message: String) {
        if (ContextCompat.checkSelfPermission(
                fragment.requireContext(),
                android.Manifest.permission.READ_CONTACTS
            ) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                fragment.requireContext(),
                android.Manifest.permission.SEND_SMS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            fragment.requestContactPermissionsLauncher.launch(
                arrayOf(
                    android.Manifest.permission.READ_CONTACTS,
                    android.Manifest.permission.SEND_SMS
                )
            )
            return
        }

        val contentResolver = fragment.requireContext().contentResolver
        val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
        val projection = arrayOf(
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
            ContactsContract.CommonDataKinds.Phone.NUMBER
        )

        val cursor: Cursor? = contentResolver.query(
            uri,
            projection,
            "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} LIKE ?",
            arrayOf("%$contactName%"),
            null
        )

        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numberIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)

                val name = it.getString(nameIndex)
                val number = it.getString(numberIndex)

                val messageSendingText = fragment.getString(R.string.contact_message_sending)
                fragment.sendBotMessage("$name $messageSendingText")
                sendSMS(fragment, number, message)
            } else {
                val contactNotFoundText = fragment.getString(R.string.contact_not_found, contactName)
                fragment.sendBotMessage(contactNotFoundText)
            }
        }
    }

    private fun sendSMS(fragment: ChatBotFragment, phoneNumber: String, message: String) {
        val smsIntent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("sms:$phoneNumber")
            putExtra("sms_body", message)
        }
        if (ContextCompat.checkSelfPermission(
                fragment.requireContext(),
                android.Manifest.permission.SEND_SMS
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fragment.startActivity(smsIntent)
        } else {
            fragment.requestContactPermissionsLauncher.launch(
                arrayOf(
                    android.Manifest.permission.READ_CONTACTS,
                    android.Manifest.permission.SEND_SMS
                )
            )
        }
    }
}