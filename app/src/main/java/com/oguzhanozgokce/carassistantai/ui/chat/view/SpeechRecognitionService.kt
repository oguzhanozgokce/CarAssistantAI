package com.oguzhanozgokce.carassistantai.ui.chat.view

import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.oguzhanozgokce.carassistantai.R
import java.util.Locale

class SpeechRecognitionService : Service() {

    private lateinit var speechRecognizer: SpeechRecognizer

    override fun onCreate() {
        super.onCreate()
        startForegroundService()
        startSpeechRecognition()
    }

    private fun startForegroundService() {
        val notification = NotificationCompat.Builder(this, "CHANNEL_ID")
            .setContentTitle("Speech Recognition Service")
            .setContentText("Listening for voice commands...")
            .setSmallIcon(R.drawable.svg_mic)
            .build()

        startForeground(1, notification)
    }

    private fun startSpeechRecognition() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.listening_to_you))
        }

        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onError(error: Int) {
                startSpeechRecognition() // Hata olduğunda yeniden başlat
            }
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                matches?.let {
                    if (it.isNotEmpty()) {
                        // Sesli komut işlendiğinde yapılacak işlemler
                        processVoiceCommand(it[0])
                    }
                }
                startSpeechRecognition() // Sonuç alındığında yeniden başlat
            }
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        speechRecognizer.startListening(intent)
    }

    private fun processVoiceCommand(command: String) {
        // Sesli komutu işleme
        Toast.makeText(this, "Heard: $command", Toast.LENGTH_SHORT).show()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        speechRecognizer.destroy()
    }
}