package com.oguzhanozgokce.carassistantai.ui.chat

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.oguzhanozgokce.carassistantai.R
import com.oguzhanozgokce.carassistantai.common.gone
import com.oguzhanozgokce.carassistantai.common.visible
import com.oguzhanozgokce.carassistantai.data.model.Message
import com.oguzhanozgokce.carassistantai.data.repo.OpenAiRepository
import com.oguzhanozgokce.carassistantai.databinding.FragmentChatBotBinding
import com.oguzhanozgokce.carassistantai.ui.chat.adapter.MessageAdapter
import com.oguzhanozgokce.carassistantai.ui.chat.utils.CameraUtils
import com.oguzhanozgokce.carassistantai.ui.chat.utils.CommandProcessor
import com.oguzhanozgokce.carassistantai.ui.chat.utils.ContactUtils
import com.oguzhanozgokce.carassistantai.ui.chat.utils.GoogleUtils
import com.oguzhanozgokce.carassistantai.ui.chat.utils.MapUtils
import com.oguzhanozgokce.carassistantai.ui.chat.utils.PhotoUtils
import com.oguzhanozgokce.carassistantai.ui.chat.utils.YouTubeUtils
import java.util.Locale

class ChatBotFragment : Fragment() {
    private var _binding: FragmentChatBotBinding? = null
    private val binding get() = _binding!!
    private val adapter by lazy { MessageAdapter() }
    private val commandProcessor by lazy { CommandProcessor(this) }
    private val viewModel: ChatBotViewModel by viewModels()


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

    private val speechRecognizerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == AppCompatActivity.RESULT_OK && result.data != null) {
            result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.let { results ->
                if (results.isNotEmpty()) {
                    binding.editTextMessage.setText(results[0])
                }
            }
        }
    }

    private val requestStoragePermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // İzin verildiğinde yapılacak işlemler
        } else {
            sendBotMessage("Gerekli izinler verilmedi.")
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
    ): View? {
        _binding = FragmentChatBotBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupButtonSend()
        setupMicButton()
        setupCardViews()


        // Observe the chat response
        viewModel.chatResponse.observe(viewLifecycleOwner, Observer { result ->
            result.fold(
                onSuccess = { response ->
                    sendBotMessage(response)
                },
                onFailure = { error ->
                    sendBotMessage(error.message ?: "An error occurred")
                }
            )
        })
    }

    private fun setupRecyclerView() {
        binding.recyclerView.apply {
            adapter = this@ChatBotFragment.adapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun setupMicButton() {
        binding.buttonMic.setOnClickListener {
            startVoiceInput()
        }
    }


    private fun startVoiceInput() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "Konuşun...")
        }
        try {
            speechRecognizerLauncher.launch(intent)
        } catch (e: Exception) {
            e.printStackTrace()
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

    private fun setupCardViews() {
        setupCardView(binding.cardView1, R.id.textViewQuestion1)
        setupCardView(binding.cardView2, R.id.textViewQuestion2)
        setupCardView(binding.cardView3, R.id.textViewQuestion3)
    }

    private fun setupCardView(cardView: CardView, textViewId: Int) {
        cardView.setOnClickListener {
            val message = cardView.findViewById<TextView>(textViewId).text.toString()
            sendMessage(Message(message, false))
            switchToChatMode()
            commandProcessor.processCommand(message)
        }
    }

    private fun sendMessage(message: Message) {
        adapter.addMessage(message)
        binding.recyclerView.scrollToPosition(adapter.itemCount - 1)
    }

    fun sendBotMessage(message: String) {
        val botMessage = Message(message, true)
        adapter.addMessage(botMessage)
        binding.recyclerView.scrollToPosition(adapter.itemCount - 1)
    }

    private fun switchToChatMode() {
        binding.questionLayout.gone()
        binding.recyclerView.visible()
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
        val pm = requireContext().packageManager
        try {
            // Check if Spotify is installed
            pm.getPackageInfo("com.spotify.music", PackageManager.GET_ACTIVITIES)

            // Create an intent to launch Spotify
            val intent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                `package` = "com.spotify.music"
            }

            if (intent.resolveActivity(pm) != null) {
                startActivity(intent)
            } else {
                sendBotMessage("Spotify uygulaması açılırken bir hata oluştu.")
            }
        } catch (e: PackageManager.NameNotFoundException) {
            sendBotMessage("Spotify uygulaması yüklü değil.")
        }
    }

    fun openCamera() {
        CameraUtils.openCamera(this)
    }

    fun sendOpenAiRequest(userMessage: String) {
        viewModel.sendMessage(userMessage)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
