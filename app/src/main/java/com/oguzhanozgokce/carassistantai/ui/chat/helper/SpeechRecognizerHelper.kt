package com.oguzhanozgokce.carassistantai.ui.chat.helper

import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.oguzhanozgokce.carassistantai.R
import java.util.Locale


class SpeechRecognizerHelper(
    fragment: Fragment,
    private val onResult: (String) -> Unit
) {
    private val context = fragment.requireContext()


    private val speechRecognizerLauncher = fragment.registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == AppCompatActivity.RESULT_OK && result.data != null) {
            result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.let { results ->
                if (results.isNotEmpty()) {
                    onResult(results[0])
                }
            }
        }
    }

    @Synchronized
    fun startVoiceInput() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT,context.getString(R.string.listening_to_you))
        }
        try {
            speechRecognizerLauncher.launch(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
