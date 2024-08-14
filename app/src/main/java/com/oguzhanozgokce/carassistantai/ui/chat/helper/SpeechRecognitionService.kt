package com.oguzhanozgokce.carassistantai.ui.chat.helper

import android.Manifest
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.IBinder
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.util.Log
import androidx.core.content.ContextCompat
import java.util.Locale

class SpeechRecognitionService : Service() {

    private lateinit var speechRecognizer: SpeechRecognizer
    private val triggerWord = "kara şimşek"
    private var isListening = false

    override fun onCreate() {
        super.onCreate()

        // SpeechRecognizer'ı başlat
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizer.setRecognitionListener(recognitionListener)

        startListening()
    }

    private fun startListening() {
        if (isListening) return
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            Log.e("SpeechRecognitionService", "Microphone permission not granted")
            return
        }
        val recognitionIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
        }
        isListening = true
        speechRecognizer.startListening(recognitionIntent)
    }

    private fun stopListening() {
        if (!isListening) return
        speechRecognizer.stopListening()
        isListening = false
    }

    private val recognitionListener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle) {
            Log.d("SpeechRecognitionService", "Ready for speech")
        }
        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray) {}
        override fun onEndOfSpeech() {
            Log.d("SpeechRecognitionService", "End of speech")
            stopListening()
            startListening()
        }
        override fun onError(error: Int) {
            Log.e("SpeechRecognitionService", "Error: $error")
            stopListening()
            startListening()
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
            startListening()
        }
        override fun onPartialResults(partialResults: Bundle) {}
        override fun onEvent(eventType: Int, params: Bundle) {}
    }

    private fun sendCommandToMainActivity(command: String) {
        val intent = Intent("com.oguzhanozgokce.carassistantai.COMMAND_RECEIVED").apply {
            putExtra("COMMAND", command)
        }
        sendBroadcast(intent)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        stopListening()
        speechRecognizer.destroy()
    }
}

///--------------------------------

//class MainActivity : AppCompatActivity() {
//
//
//
//    private val REQUEST_CODE_OVERLAY_PERMISSION = 101
//    private lateinit var binding: ActivityMainBinding
//    private lateinit var messageAdapter: MessageAdapter
//    private val messageList = mutableListOf<Message>()
//    private var commandText: String = ""
//
//
//    private val recognizedTextReceiver = object : BroadcastReceiver() {
//        override fun onReceive(context: Context?, intent: Intent?) {
//            if (intent?.action == "processedCommand") {
//                val command = intent.getStringExtra("command") ?: ""
//                Log.d("MainActivity", "Received command: $command")
//
//                messageList.add(Message(command, true))
//                messageAdapter.notifyItemInserted(messageList.size - 1)
//
//                messageAdapter.addPlaceholderMessage()
//                binding.recyclerView.smoothScrollToPosition(messageList.size - 1)
//
//                sendMessageToModel(command)
//
//            }
//        }
//    }
//
//
//    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        binding = ActivityMainBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        messageAdapter = MessageAdapter(messageList,this)
//        binding.recyclerView.layoutManager = LinearLayoutManager(this)
//        binding.recyclerView.adapter = messageAdapter
//        requestOverlayPermission()
//        requestCallPermission(this)
//
//
//        binding.micButton.setOnClickListener {
//            askSpeechInput()
//        }
//
//        binding.promptButton.setOnClickListener {
//
//            commandText = binding.commandText.text.toString()
//            if (commandText.isBlank()) {
//                Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show()
//            }else{
//                hideKeyboard(this)
//
//                // Add the user's message to the list
//                messageList.add(Message(commandText, true))
//                messageAdapter.notifyItemInserted(messageList.size - 1)
//
//                // Add a placeholder message for the model's response
//                messageAdapter.addPlaceholderMessage()
//                binding.recyclerView.smoothScrollToPosition(messageList.size - 1)
//
//                // Send the message to the model
//                sendMessageToModel(commandText)
//                binding.commandText.text.clear()
//            }
//        }
//
//        val filter = IntentFilter("processedCommand")
//        ContextCompat.registerReceiver(this, recognizedTextReceiver, filter, ContextCompat.RECEIVER_EXPORTED)
//
//        startVoiceAssistantService()
//
//
//    }
//
//
//    private fun requestOverlayPermission() {
//        if (!Settings.canDrawOverlays(this)) {
//            val intent = Intent(
//                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
//                Uri.parse("package:$packageName")
//            )
//            startActivityForResult(intent, REQUEST_CODE_OVERLAY_PERMISSION)
//        }
//    }
//
//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<out String>,
//        grantResults: IntArray
//    ) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        when (requestCode) {
//            REQUEST_CALL_PERMISSION -> {
//                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
//                    requestContactsPermission(this@MainActivity)
//                } else {
//                    // Permission denied
//                }
//            }
//            REQUEST_CONTACTS_PERMISSION -> {
//                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
//                    requestSendSMSPermission(this@MainActivity)
//                } else {
//                    // Permission denied
//                }
//            }
//            REQUEST_SEND_SMS_PERMISSION -> {
//                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
//                    SendMessage.checkAudioPermissions(this@MainActivity)
//                } else {
//                    // Permission denied
//                }
//            }
//        }
//    }
//
//
//
//
//    private fun sendMessageToModel(message: String) {
//        lifecycleScope.launch(Dispatchers.IO) {
//            try {
//                val response = GeminiModel.chat.sendMessage(message)
//                response.text?.let {
//                    if (it.contains("```json")) {
//                        it.trim()
//                            .removeSurrounding("```json", "```")
//                            .trim()
//                    }else{
//                        null
//                    }
//                }
//                withContext(Dispatchers.Main) {
//                    response.text?.let { HandleCommand.handleCommand(this@MainActivity, it) }
//
//                    messageAdapter.updateLastMessage(response.text ?: "No response")
//                    binding.recyclerView.smoothScrollToPosition(messageList.size - 1)
//                }
//            } catch (e: Exception) {
//                withContext(Dispatchers.Main) {
//                    messageList.add(Message("Error: ${e.message}", false))
//                    messageAdapter.notifyItemInserted(messageList.size - 1)
//                    binding.recyclerView.smoothScrollToPosition(messageList.size - 1)
//                }
//            }
//        }
//    }
//
//
//
//    private val result =
//        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
//            if (result.resultCode == RESULT_OK && result.data != null) {
//                val results =
//                    result.data!!.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
//                            as ArrayList<String>?
//                binding.commandText.setText(results!![0])
//            }
//        }
//
//    private fun askSpeechInput() {
//        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
//            Toast.makeText(
//                this,
//                "Speech recognition is not available",
//                Toast.LENGTH_SHORT
//            )
//                .show()
//        } else {
//            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
//            intent.putExtra(
//                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
//                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
//            )
//            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
//            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Say something!")
//            result.launch(intent)
//        }
//
//
//    }
//
//    private fun hideKeyboard(activity: Activity) {
//        val inputMethodManager =
//            activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//        val view = activity.currentFocus ?: View(activity)
//        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        messageAdapter.releaseTTS()
//        unregisterReceiver(recognizedTextReceiver)
//        stopVoiceAssistantService()
//    }
//
//
//    private fun startVoiceAssistantService() {
//        val intent = Intent(this, VoiceAssistantService::class.java)
//        intent.putExtra("input", "Start listening")
//        startService(intent)
//    }
//
//    private fun stopVoiceAssistantService() {
//        val intent = Intent(this, VoiceAssistantService::class.java)
//        stopService(intent)
//    }
//
//}