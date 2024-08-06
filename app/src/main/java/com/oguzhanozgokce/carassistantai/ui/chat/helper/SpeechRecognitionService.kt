package com.oguzhanozgokce.carassistantai.ui.chat.helper

import android.app.Service
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
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.oguzhanozgokce.carassistantai.common.Constant.MY_INTENT
import java.util.Locale
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class SpeechRecognitionService : Service() {

    private lateinit var speechRecognizer: SpeechRecognizer
    private val triggerWord = "kara şimşek"
    private var isListening = false
    private val mainHandler = Handler(Looper.getMainLooper())
    private val executorService = Executors.newSingleThreadExecutor()

    override fun onCreate() {
        super.onCreate()
        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            Log.e("SpeechService", "Speech recognition is not available on this device.")
            stopSelf()
            return
        }
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (checkMicrophonePermission()) {
            startListening()
            Log.d("serves", "onStartCommand: ")
        } else {
            Log.e("SpeechService", "Microphone permission not granted")
        }
        return START_STICKY
    }

    private fun startListening() {
        if (isListening) return
        Log.d("serves", "Starting listening")

        val recognitionIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, packageName)
        }

        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                Log.d("SpeechService", "Ready for speech")
            }

            override fun onBeginningOfSpeech() {
                Log.d("SpeechService", "Speech begun")
            }

            override fun onRmsChanged(rmsdB: Float) {}

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                Log.d("SpeechService", "End of speech")
                isListening = false
                startListening() // Restart listening after speech ends
                Log.d("serves", "onEndOfSpeech: ")
//                speechRecognizer.startListening(recognitionIntent)
            }

            override fun onError(error: Int) {
                Log.e("SpeechService", "Error occurred: $error")
                isListening = false

                when (error) {
                    SpeechRecognizer.ERROR_NO_MATCH, SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> {
                        Log.d("serves", "onError NO match: ")

                        mainHandler.postDelayed({
                            startListening()
                        }, 1000)
                    }
                    SpeechRecognizer.ERROR_CLIENT -> {
                        Log.e(
                            "SpeechService",
                            "Client error, check microphone permission and service availability."
                        )
                        Log.d("serves", "onError ERROR_CLIENT ")

                    }
                    else -> {
                        handleError(error)
                    }
                }
            }

            override fun onResults(results: Bundle?) {
                results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.let { result ->
                    if (result.isNotEmpty()) {
                        Log.d("serves, ","onResults girdi")
                        val recognizedText = result[0].lowercase(Locale.getDefault())
                        Log.d("SpeechService", "Recognized: $recognizedText")
                        if (recognizedText.contains(triggerWord.lowercase(Locale.getDefault()))) {
                            val command = recognizedText.substringAfter(triggerWord).trim()
                            Log.d("SpeechService", "Command: $command")
                            sendCommandToFragment(command)
                        }
                    }
                }
                isListening = false
                startListening() // Restart listening for continuous recognition
                Log.d("serves", "onResults çıktı ")
            }


            override fun onPartialResults(partialResults: Bundle?) {
                partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.let { partialResults ->
                        if (partialResults.isNotEmpty()) {
                            val partialText = partialResults.joinToString(separator = " ")
                                .lowercase(Locale.getDefault())
                            Log.d("SpeechService", "Partial result: $partialText")
                            if (partialText.contains(triggerWord.lowercase(Locale.getDefault()))) {
                                val command = partialText.substringAfter(triggerWord).trim()
                                Log.d("SpeechService", "Partial command: $command")
                            }
                        }
                    }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        executorService.execute {
            try {
                TimeUnit.MILLISECONDS.sleep(500) // Delay to prevent immediate restart issues
                mainHandler.post {
                    speechRecognizer.startListening(recognitionIntent)
                    isListening = true
                    Log.d("SpeechService", "Listening started")
                }
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }
    }
    private fun sendCommandToFragment(command: String) {
        val intent = Intent(MY_INTENT).apply {
            putExtra("COMMAND", command)
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun checkMicrophonePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun handleError(error: Int) {
        when (error) {
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY, SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> {
                mainHandler.postDelayed({
                    Log.e("SpeechService", "Error recognizer busy or insufficient permissions")
                    startListening()
                    Log.d("serves", "handleError")

                }, 2000)
            }

            else -> {
                mainHandler.postDelayed({
                    Log.e("SpeechService", "Error occurred: $error")
                    startListening()
                    Log.d("serves", "delay")

                }, 1000)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        speechRecognizer.destroy()
        executorService.shutdownNow()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}
