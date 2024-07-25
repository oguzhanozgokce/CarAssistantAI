package com.oguzhanozgokce.carassistantai.ui.chat.view

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.oguzhanozgokce.carassistantai.R

class OverlayPermissionDialogFragment : DialogFragment() {

    interface OverlayPermissionDialogListener {
        fun onGrantPermissionClicked()
        fun onNoGrantPermissionClicked()
    }

    var listener: OverlayPermissionDialogListener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val inflater = requireActivity().layoutInflater
        val view = inflater.inflate(R.layout.popup_permission, null)

        val tvGrantPermission = view.findViewById<TextView>(R.id.tv_grant_permission)
        val tvNoGrantPermission = view.findViewById<TextView>(R.id.tv_no_grant_permission)

        tvGrantPermission.setOnClickListener {
            listener?.onGrantPermissionClicked()
            dismiss()
        }

        return android.app.AlertDialog.Builder(requireActivity())
            .setView(view)
            .create()
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)
        listener?.onNoGrantPermissionClicked()
    }
}