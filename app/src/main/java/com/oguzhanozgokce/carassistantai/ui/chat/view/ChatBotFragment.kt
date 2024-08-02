package com.oguzhanozgokce.carassistantai.ui.chat.view
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.asTextOrNull
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import com.oguzhanozgokce.carassistantai.BuildConfig
import com.oguzhanozgokce.carassistantai.R
import com.oguzhanozgokce.carassistantai.common.Constant.GEMINI_MODEL_NAME
import com.oguzhanozgokce.carassistantai.common.Constant.SYSTEM_INSTRUCTIONS
import com.oguzhanozgokce.carassistantai.common.gone
import com.oguzhanozgokce.carassistantai.common.viewBinding
import com.oguzhanozgokce.carassistantai.common.visible
import com.oguzhanozgokce.carassistantai.data.model.message.Message
import com.oguzhanozgokce.carassistantai.databinding.FragmentChatBotBinding
import com.oguzhanozgokce.carassistantai.ui.chat.adapter.MessageAdapter
import com.oguzhanozgokce.carassistantai.ui.chat.helper.SpeechRecognizerHelper
import com.oguzhanozgokce.carassistantai.ui.chat.utils.CommandProcessor
import com.oguzhanozgokce.carassistantai.ui.chat.utils.alarm.AlarmUtils
import com.oguzhanozgokce.carassistantai.ui.chat.utils.app.google.GoogleUtils
import com.oguzhanozgokce.carassistantai.ui.chat.utils.app.google.SearchGoogle
import com.oguzhanozgokce.carassistantai.ui.chat.utils.app.mail.MailUtils
import com.oguzhanozgokce.carassistantai.ui.chat.utils.app.map.MapUtils
import com.oguzhanozgokce.carassistantai.ui.chat.utils.app.notes.NoteUtils
import com.oguzhanozgokce.carassistantai.ui.chat.utils.app.photo.PhotoUtils
import com.oguzhanozgokce.carassistantai.ui.chat.utils.app.twitter.TwitterUtils
import com.oguzhanozgokce.carassistantai.ui.chat.utils.app.whatsapp.WhatsAppUtils
import com.oguzhanozgokce.carassistantai.ui.chat.utils.app.youtube.YouTubeUtils
import com.oguzhanozgokce.carassistantai.ui.chat.utils.camera.CameraUtils
import com.oguzhanozgokce.carassistantai.ui.chat.utils.contact.ContactUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class ChatBotFragment : Fragment(R.layout.fragment_chat_bot)  {
    private val binding by viewBinding(FragmentChatBotBinding::bind)
    private val adapter by lazy { MessageAdapter() }
    private val commandProcessor by lazy { CommandProcessor(this) }
    private val viewModel: ChatBotViewModel by viewModels()
    private lateinit var speechRecognizerHelper: SpeechRecognizerHelper
    private lateinit var searchGoogle: SearchGoogle

    private val commandReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val command = intent?.getStringExtra("COMMAND")
            command?.let {
                receiveVoiceCommand(it)
            }
        }
    }

    val requestContactPermissionsLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            // Handle the case where all permissions are granted
        } else {
            sendBotMessage(getString(R.string.permissions_not_granted))
        }
    }
    val cameraActivityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            // Handle the camera result here
        } else {
            sendBotMessage(getString(R.string.camera_app_not_found))
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        observeViewModel()
        searchGoogle = SearchGoogle(requireContext())
        speechRecognizerHelper = SpeechRecognizerHelper(this) { userMessage ->
            sendMessage(Message(userMessage, false))
            showLoadingAnimation()
            sendToGeminiAndProcessCommand(userMessage) { jsonResponse ->
                hideLoadingAnimation()
                commandProcessor.processCommand(jsonResponse)
            }
        }
        if (savedInstanceState == null) {
            viewModel.setChatMode(false)
            sendMessage(Message("Welcome!", true))
            sendMessage(Message(getString(R.string.app_intro),true))
        }

        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(commandReceiver, IntentFilter("com.oguzhanozgokce.carassistantai.SpeechCommand"))


    }

    private fun setupUI() {
        setupRecyclerView()
        setupButtonSend()
        setupMicButton()
    }
    private fun observeViewModel() {
        viewModel.messages.observe(viewLifecycleOwner) { messages ->
            adapter.setMessages(messages)
            scrollToBottom()
        }


        viewModel.isChatMode.observe(viewLifecycleOwner) { isChatMode ->
            if (isChatMode) {
                switchToChatMode()
            } else {
                showCardViews()
            }
        }
    }
    private fun setupRecyclerView() {
        binding.recyclerView.apply {
            adapter = this@ChatBotFragment.adapter
            layoutManager = LinearLayoutManager(context)
        }
    }
    private fun setupMicButton() {
        binding.buttonMic.setOnClickListener {
            speechRecognizerHelper.startVoiceInput()
        }
    }
    private fun setupButtonSend() {
        binding.buttonSend.setOnClickListener {
            val message = binding.editTextMessage.text.toString()
            if (message.isNotEmpty()) {
                sendMessage(Message(message, false))
                binding.editTextMessage.text.clear()
                switchToChatMode()
                showLoadingAnimation()
                sendToGeminiAndProcessCommand(message) { response ->
                    hideLoadingAnimation()
                    commandProcessor.processCommand(response)
                }
            }
        }
    }
    fun receiveVoiceCommand(command: String) {
        sendMessage(Message(command, false))
        showLoadingAnimation()
        Log.d("VoiceCommand", command)
        sendToGeminiAndProcessCommand(command) { jsonResponse ->
            hideLoadingAnimation()
            commandProcessor.processCommand(jsonResponse)
        }
    }
    private fun sendMessage(message: Message) {
        viewModel.addMessage(message)
        scrollToBottom()
    }
    fun sendBotMessage(message: String) {
        val botMessage = Message(message, true)
        viewModel.addMessage(botMessage)
        scrollToBottom()
    }
    private fun scrollToBottom() {
        binding.recyclerView.post {
            if (adapter.itemCount > 0) {
                binding.recyclerView.scrollToPosition(adapter.itemCount - 1)
                binding.scrollView?.post {
                    binding.scrollView!!.fullScroll(View.FOCUS_DOWN)
                }
            }
        }
    }
    private fun switchToChatMode() {
        binding.recyclerView.visible()
    }
    private fun showCardViews() {
        binding.recyclerView.gone()
    }
    private fun showLoadingAnimation() {
        val loadingMessage = Message("", isBotMessage = true, isLoading = true)
        viewModel.addMessage(loadingMessage)
        binding.recyclerView.scrollToPosition(adapter.itemCount - 1)
    }
    fun hideLoadingAnimation() {
        viewModel.removeLoadingMessage()
    }
    fun sendToGeminiAndProcessCommand(prompt: String, callback: (String) -> Unit) {
        val generativeModel = GenerativeModel(
            modelName = GEMINI_MODEL_NAME,
            apiKey = BuildConfig.GEMINI_API_KEY,
            generationConfig = generationConfig {
                temperature = 1f
                topK = 64
                topP = 0.95f
                maxOutputTokens = 8192
                responseMimeType = "text/plain"
            },
            systemInstruction = content {
                text(SYSTEM_INSTRUCTIONS)
            }
        )

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    generativeModel.generateContent(prompt)
                }
                val jsonResponse = response.candidates.first().content.parts.first().asTextOrNull() ?: ""
                Log.d(getString(R.string.gemini_response), jsonResponse)
                callback(jsonResponse)
            } catch (e: Exception) {
                hideLoadingAnimation()
                sendBotMessage("${getString(R.string.an_error_occurred)}: ${e.message}")
            }
        }
    }
    fun openLink(url: String) {
        GoogleUtils.openLink(requireContext(), url)
    }
    fun searchAndOpenYouTube(query: String) {
        YouTubeUtils.searchAndOpenYouTube(this, query)
    }
    fun openGoogleMapsForDestination(destination: String) {
        MapUtils.openGoogleMapsForDestination(this, destination)
    }
    fun showPlacesOnGoogleMaps(placeType: String) {
        MapUtils.showPlacesOnGoogleMaps(this, placeType)
    }
    fun findContactAndCall(contactName: String) {
        ContactUtils.findContactAndCall(this, contactName)
    }
    fun findContactAndSendMessage(contactName: String, message: String) {
        ContactUtils.findContactAndSendMessage(this, contactName, message)
    }
    fun openGooglePhotos() {
        PhotoUtils.openGooglePhotos(requireContext()) { errorMessage ->
            sendBotMessage(errorMessage)
        }
    }
    fun openCamera() {
        CameraUtils.openCamera(this)
    }
    fun setAlarm(hour: Int, minute: Int, message: String) {
        AlarmUtils.setAlarm(requireContext(), hour, minute, message) { errorMessage ->
            sendBotMessage(errorMessage)
        }
    }
    fun startStopwatch() {
        AlarmUtils.startStopwatch(requireContext()) { errorMessage ->
            sendBotMessage(errorMessage)
        }
    }
    fun startTimer(seconds: Int, message: String) {
        AlarmUtils.startTimer(requireContext(), seconds, message) { errorMessage ->
            sendBotMessage(errorMessage)
        }
    }
    fun openClockApp() {
        AlarmUtils.showAlarms(requireContext())
    }
    fun sendNoteKeep(noteContent: String) {
        NoteUtils.openKeepWithNote(requireContext(), noteContent) { message ->
            sendBotMessage(message)
        }
    }
    fun openMailApp(email: String, subject: String, body: String) {
        MailUtils.openMailApp(requireContext(), email, subject, body) { message ->
            sendBotMessage(message)
        }
    }
    fun openWhatsApp() {
       WhatsAppUtils.openWhatsAppChatWithMyNumber(requireContext()) { message ->
            sendBotMessage(message)
        }
    }
    fun tweet(message: String) {
        TwitterUtils.tweet(requireContext(), message) { errorMessage ->
            sendBotMessage(errorMessage)
        }
    }
}