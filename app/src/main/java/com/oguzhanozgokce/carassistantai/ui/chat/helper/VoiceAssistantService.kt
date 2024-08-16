package com.oguzhanozgokce.carassistantai.ui.chat.helper

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.core.app.NotificationCompat
import com.oguzhanozgokce.carassistantai.MainActivity
import com.oguzhanozgokce.carassistantai.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

class VoiceAssistantService : Service() {

    private val channelID = "VoiceAssistantServiceChannel"
    private lateinit var speechRecognizer: SpeechRecognizer

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizer.setRecognitionListener(recognitionListener)
    }

    private val recognitionListener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle) {
            Log.d("VoiceAssistantService", "Ready for speech")
        }

        override fun onBeginningOfSpeech() {
            Log.d("VoiceAssistantService", "Beginning of speech")
        }

        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray) {}
        override fun onEndOfSpeech() {
            Log.d("VoiceAssistantService", "End of speech")
        }

        override fun onError(error: Int) {
            Log.d("VoiceAssistantService", "Error: $error")
            when (error) {
                SpeechRecognizer.ERROR_AUDIO -> Log.d(
                    "VoiceAssistantService",
                    "Audio recording error"
                )

                SpeechRecognizer.ERROR_CLIENT -> Log.d("VoiceAssistantService", "Client side error")
                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> Log.d(
                    "VoiceAssistantService",
                    "Insufficient permissions"
                )

                SpeechRecognizer.ERROR_NETWORK -> Log.d("VoiceAssistantService", "Network error")
                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> Log.d(
                    "VoiceAssistantService",
                    "Network timeout"
                )

                SpeechRecognizer.ERROR_NO_MATCH -> Log.d("VoiceAssistantService", "No match found")
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> Log.d(
                    "VoiceAssistantService",
                    "RecognitionService busy"
                )

                SpeechRecognizer.ERROR_SERVER -> Log.d("VoiceAssistantService", "Server error")
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> Log.d(
                    "VoiceAssistantService",
                    "No speech input"
                )

                else -> Log.d("VoiceAssistantService", "Unknown error")
            }
            if (error == SpeechRecognizer.ERROR_NO_MATCH) {
                Handler(Looper.getMainLooper()).postDelayed({
                    CoroutineScope(Dispatchers.Main).launch {
                        startListening()
                        Log.d("VoiceAssistantService", getString(R.string.start_listening_from_error_no_match))
                    }
                }, 6000)
            }
        }

        override fun onPartialResults(partialResults: Bundle) {}
        override fun onEvent(eventType: Int, params: Bundle) {}

        override fun onResults(results: Bundle) {
            val matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (!matches.isNullOrEmpty()) {
                val recognizedText = matches[0]
                Log.d("SpeechRecognitionService", "Received Serves command: $recognizedText")
                sendCommandToActivity(recognizedText)
                Log.d("VoiceAssistantService", "Recognized: $recognizedText")
                showNotification(getString(R.string.voice_assistant_title), getString(R.string.voice_assistant_recognized, recognizedText))
                returnToApp()
            } else {
                Log.d("VoiceAssistantService", "No results")
                showNotification(getString(R.string.voice_assistant_title), getString(R.string.voice_assistant_no_results))
            }
            CoroutineScope(Dispatchers.Main).launch {
                delay(8000)
                startListening()
                Log.d("VoiceAssistantService", getString(R.string.start_listening_from_results))
            }
        }
    }

    @SuppressLint("ForegroundServiceType")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("VoiceAssistantService", "Service started")
        val input = intent?.getStringExtra("input") ?: ""
        if (input == getString(R.string.start_listening_command)) {
            Log.d("VoiceAssistantService", "Start listening command received")
            CoroutineScope(Dispatchers.Main).launch {
                startListening()
                Log.d("VoiceAssistantService", getString(R.string.start_listening_command))
            }
        }
        val notification = createNotification(getString(R.string.voice_assistant_title), getString(R.string.voice_assistant_listening))
        startForeground(1, notification)
        return START_STICKY
    }

    private fun createNotificationChannel() {
        val serviceChannel = NotificationChannel(
            channelID,
            getString(R.string.voice_assistant_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(serviceChannel)
    }

    private fun createNotification(title: String, message: String): Notification {
        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0, notificationIntent, PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(this, channelID)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Replace with your icon
            .setContentIntent(pendingIntent)
            .build()
    }

    private fun showNotification(title: String, message: String) {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = createNotification(title, message)
        notificationManager.notify(1, notification)
    }

    private fun startListening() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, getString(R.string.say_something))
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 0L)
        intent.putExtra(
            RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS,
            0L
        )
        intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 1500L)
        speechRecognizer.startListening(intent)
    }

    private fun sendCommandToActivity(command: String) {
        val intent = Intent("processedCommand")
        intent.putExtra("command", command)
        sendBroadcast(intent)
    }

    private fun returnToApp() {
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            startActivity(intent)
        } else {
            val pendingIntent = PendingIntent.getActivity(
                this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            try {
                pendingIntent.send()
            } catch (e: PendingIntent.CanceledException) {
                e.printStackTrace()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        speechRecognizer.destroy()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
