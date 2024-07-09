package com.oguzhanozgokce.carassistantai.ui.chat.view

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.ai.client.generativeai.GenerativeModel
import com.oguzhanozgokce.carassistantai.R
import com.oguzhanozgokce.carassistantai.common.Constant.GEMINI_API_KEY
import com.oguzhanozgokce.carassistantai.common.gone
import com.oguzhanozgokce.carassistantai.common.setupCardView
import com.oguzhanozgokce.carassistantai.common.visible
import com.oguzhanozgokce.carassistantai.data.model.Message
import com.oguzhanozgokce.carassistantai.databinding.FragmentChatBotBinding
import com.oguzhanozgokce.carassistantai.ui.chat.adapter.MessageAdapter
import com.oguzhanozgokce.carassistantai.ui.chat.helper.SpeechRecognizerHelper
import com.oguzhanozgokce.carassistantai.ui.chat.utils.alarm.AlarmUtils
import com.oguzhanozgokce.carassistantai.ui.chat.utils.camera.CameraUtils
import com.oguzhanozgokce.carassistantai.ui.chat.utils.CommandProcessor
import com.oguzhanozgokce.carassistantai.ui.chat.utils.contact.ContactUtils
import com.oguzhanozgokce.carassistantai.ui.chat.utils.google.GoogleUtils
import com.oguzhanozgokce.carassistantai.ui.chat.utils.instagram.InstagramUtils
import com.oguzhanozgokce.carassistantai.ui.chat.utils.mail.MailUtils
import com.oguzhanozgokce.carassistantai.ui.chat.utils.map.MapUtils
import com.oguzhanozgokce.carassistantai.ui.chat.utils.photo.PhotoUtils
import com.oguzhanozgokce.carassistantai.ui.chat.utils.spotify.SpotifyUtils
import com.oguzhanozgokce.carassistantai.ui.chat.utils.youtube.YouTubeUtils
import kotlinx.coroutines.runBlocking

class ChatBotFragment : Fragment() {
    private var _binding: FragmentChatBotBinding? = null
    private val binding get() = _binding!!
    private val adapter by lazy { MessageAdapter() }
    private val commandProcessor by lazy { CommandProcessor(this) }
    private val viewModel: ChatBotViewModel by viewModels()
    private lateinit var speechRecognizerHelper: SpeechRecognizerHelper


    companion object {
        const val TAG = "ChatBotFragment"
    }

    val requestContactPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val allGranted = permissions.entries.all { it.value }
        if (allGranted) {
            // Handle the case where all permissions are granted
        } else {
            sendBotMessage("Necessary authorisations were not granted")
        }
    }

    private val requestStoragePermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Actions to be taken when authorisation is granted
        } else {
            sendBotMessage("The necessary permits were not granted.")
        }
    }


    fun requestStoragePermission() {
        val readPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            android.Manifest.permission.READ_MEDIA_IMAGES
        } else {
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        }
        requestStoragePermissionsLauncher.launch(readPermission)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChatBotBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        observeViewModel()

        speechRecognizerHelper = SpeechRecognizerHelper(this) { userMessage ->
            sendMessage(Message(userMessage, false))
            commandProcessor.processCommand(userMessage)
            //sendGeminiResponse(userMessage)
        }

        if (savedInstanceState == null) {
            viewModel.setChatMode(false)
            sendMessage(Message("Welcome!", true))
            sendMessage(Message("How can I help you?", true))
        }
    }

    private fun setupUI() {
        setupRecyclerView()
        setupButtonSend()
        setupMicButton()
    }

    private fun observeViewModel() {
        viewModel.messages.observe(viewLifecycleOwner) { messages ->
            adapter.setMessages(messages)
            binding.recyclerView.scrollToPosition(adapter.itemCount - 1)
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
                commandProcessor.processCommand(message)
            }
        }
    }


    private fun sendMessage(message: Message) {
        viewModel.addMessage(message)
        binding.recyclerView.scrollToPosition(adapter.itemCount - 1)
    }

    fun sendBotMessage(message: String) {
        val botMessage = Message(message, true)
        viewModel.addMessage(botMessage)
        binding.recyclerView.scrollToPosition(adapter.itemCount - 1)
    }

    private fun switchToChatMode() {
        binding.recyclerView.visible()
    }

    private fun showCardViews() {
        binding.recyclerView.gone()
    }

     fun showLoadingAnimation() {
        val loadingMessage = Message("", isBotMessage = true, isLoading = true)
        viewModel.addMessage(loadingMessage)
        binding.recyclerView.scrollToPosition(adapter.itemCount - 1)
    }

     fun hideLoadingAnimation() {
        viewModel.removeLoadingMessage()
    }

     fun sendGeminiResponse(prompt: String) {
        val generativeModel = GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = GEMINI_API_KEY
        )
         runBlocking {
             try {
                 val response = generativeModel.generateContent(prompt)
                 hideLoadingAnimation()
                 sendBotMessage(response.text.toString())
             } catch (e: Exception) {
                 hideLoadingAnimation()
                 sendBotMessage("An error occurred: ${e.message}")
             }
         }
    }

    fun openGoogleSearch(query: String) {
        GoogleUtils.openGoogleSearch(requireContext(), query) { errorMessage ->
            sendBotMessage(errorMessage)
        }
    }


    fun searchAndOpenYouTube(query: String) {
        YouTubeUtils.searchAndOpenYouTube(this, query)
    }

    fun openGoogleMapsForDestination(destination: String) {
        MapUtils.openGoogleMapsForDestination(this, destination)
    }

    fun findContactAndCall(contactName: String) {
        ContactUtils.findContactAndCall(this, contactName)
    }

    fun findContactAndSendMessage(contactName: String, message: String) {
        ContactUtils.findContactAndSendMessage(this, contactName, message)
    }

    fun getPhotosByDateRange(startDate: Long, endDate: Long) {
        PhotoUtils.getPhotosByDateRange(this, startDate, endDate)
    }

    fun openGooglePhotos() {
        PhotoUtils.openGooglePhotos(this)
    }

    fun openSpotify() {
        SpotifyUtils.openSpotify(requireContext()) { errorMessage ->
            sendBotMessage(errorMessage)
        }
    }

    fun searchSpotify(query: String) {
        SpotifyUtils.searchSpotify(requireContext(), query) { errorMessage ->
            sendBotMessage(errorMessage)
        }
    }

    fun openInstagram() {
        InstagramUtils.openInstagram(requireContext()) { errorMessage ->
            sendBotMessage(errorMessage)
        }
    }

    fun openInstagramProfile(username: String) {
        InstagramUtils.openInstagramProfile(requireContext(), username) { errorMessage ->
            sendBotMessage(errorMessage)
        }
    }

    fun openCamera() {
        CameraUtils.openCamera(this)
    }


    fun setAlarm(hour: Int, minute: Int, message: String) {
        AlarmUtils.setAlarm(requireContext(), hour, minute, message)

    }

    fun openMailApp() {
        MailUtils.openMailApp(requireContext()) { message ->
            sendBotMessage(message)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}