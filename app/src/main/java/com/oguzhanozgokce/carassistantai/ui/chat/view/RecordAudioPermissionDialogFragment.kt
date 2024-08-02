package com.oguzhanozgokce.carassistantai.ui.chat.view

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.oguzhanozgokce.carassistantai.R

class RecordAudioPermissionDialogFragment : DialogFragment() {
    interface RecordAudioPermissionDialogListener {
        fun onGrantAudioPermissionClicked()
        fun onContinueWithoutAudioPermissionClicked()
    }

    var listener: RecordAudioPermissionDialogListener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.popup_mic_permission, null)
        val tvGrantPermission = view.findViewById<TextView>(R.id.tv_grant_permission)
        tvGrantPermission.setOnClickListener {
            listener?.onGrantAudioPermissionClicked()
            dismiss()
        }
        return android.app.AlertDialog.Builder(requireActivity())
            .setView(view)
            .create()

    }
    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        listener?.onContinueWithoutAudioPermissionClicked()
    }
}