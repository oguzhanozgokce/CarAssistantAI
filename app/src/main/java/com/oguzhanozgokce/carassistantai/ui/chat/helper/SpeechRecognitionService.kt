package com.oguzhanozgokce.carassistantai.ui.chat.helper

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.oguzhanozgokce.carassistantai.R
import java.util.Locale

class SpeechRecognitionService : Service() {

    private lateinit var speechRecognizer: SpeechRecognizer
    private val triggerWord = "kara şimşek"
    private var isListening = false
    private lateinit var handler: Handler

    override fun onCreate() {
        super.onCreate()
        handler = Handler(Looper.getMainLooper())

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizer.setRecognitionListener(recognitionListener)

        createNotificationChannel()
        startForeground(NOTIFICATION_ID, createNotification("Listening for voice commands"))

        startListening()
    }

    private fun startListening() {
        handler.post {
            if (isListening) return@post
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                Log.e("SpeechRecognitionService", "Microphone permission not granted")
                return@post
            }
            val recognitionIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            }
            isListening = true
            speechRecognizer.startListening(recognitionIntent)
        }
    }

    private fun stopListening() {
        handler.post {
            if (!isListening) return@post
            speechRecognizer.stopListening()
            isListening = false
        }
    }

    private val recognitionListener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle) {}
        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray) {}
        override fun onEndOfSpeech() {
            stopListening()
            handler.postDelayed({ startListening() }, 2000)
        }
        override fun onError(error: Int) {
            stopListening()
            handler.postDelayed({ startListening() }, 2000)
        }
        override fun onResults(results: Bundle) {
            val matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (!matches.isNullOrEmpty()) {
                val recognizedText = matches[0]
                if (recognizedText.lowercase(Locale.getDefault()).contains(triggerWord)) {
                    val command = recognizedText.lowercase(Locale.getDefault()).substringAfter(triggerWord).trim()
                    Log.d("SpeechRecognitionService", "Received command: $command")
                    sendCommandToMainActivity(command)
                }
            }
            stopListening()
            handler.postDelayed({ startListening() }, 2000)
        }
        override fun onPartialResults(partialResults: Bundle) {}
        override fun onEvent(eventType: Int, params: Bundle) {}
    }

    private fun sendCommandToMainActivity(command: String) {
        val intent = Intent("com.oguzhanozgokce.carassistantai.COMMAND_RECEIVED").apply {
            putExtra("COMMAND", command)
            Log.d("SpeechRecognitionService", "Sending command to MainActivity: $command")
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Speech Recognition Service",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Channel for Speech Recognition Service"
        }
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    private fun createNotification(contentText: String): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Speech Recognition Service")
            .setContentText(contentText)
            .setSmallIcon(R.drawable.icon_mic)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        stopListening()
        speechRecognizer.destroy()
    }

    companion object {
        private const val CHANNEL_ID = "SpeechRecognitionServiceChannel"
        private const val NOTIFICATION_ID = 1
    }
}
