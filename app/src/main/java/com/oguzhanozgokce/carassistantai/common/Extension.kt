package com.oguzhanozgokce.carassistantai.common

import android.view.View
import android.widget.TextView
import androidx.cardview.widget.CardView

fun View.visible() {
    visibility = View.VISIBLE
}

fun View.gone() {
    visibility = View.GONE
}

fun CardView.setupCardView(textViewId: Int, onClick: (String) -> Unit) {
    setOnClickListener {
        val message = findViewById<TextView>(textViewId).text.toString()
        onClick(message)
    }
}
