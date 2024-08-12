package com.oguzhanozgokce.carassistantai

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.oguzhanozgokce.carassistantai.databinding.ActivityMainBinding
import com.oguzhanozgokce.carassistantai.ui.chat.helper.SpeechRecognitionService
import com.oguzhanozgokce.carassistantai.ui.chat.view.ChatBotFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private val commandReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val command = intent.getStringExtra("COMMAND")
            command?.let {
                Log.d("MainActivity", "Received command: $it")
                handleCommand(it)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        LocalBroadcastManager.getInstance(this).registerReceiver(
            commandReceiver,
            IntentFilter("com.oguzhanozgokce.carassistantai.COMMAND_RECEIVED")
        )

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        if (!checkMicrophonePermission()) {
            requestMicrophonePermission()
        }

        registerReceiver(
            commandReceiver,
            IntentFilter("com.oguzhanozgokce.carassistantai.COMMAND_RECEIVED"), RECEIVER_EXPORTED
        )
    }

    private fun handleCommand(command: String) {
        val chatBotFragment = supportFragmentManager.findFragmentByTag(ChatBotFragment::class.java.simpleName)

        if (chatBotFragment == null) {
            Log.d("MainActivity", "Launching ChatBotFragment")
            val fragment = ChatBotFragment()
            fragment.arguments = Bundle().apply {
                putString("COMMAND", command)
            }
            supportFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment, fragment, ChatBotFragment::class.java.simpleName)
                .addToBackStack(null)
                .commitAllowingStateLoss()

        } else {
            Log.d("MainActivity", "Received command: $command")
            (chatBotFragment as ChatBotFragment).receiveVoiceCommand(command)
        }
    }


    override fun onResume() {
        super.onResume()
        stopBackgroundService()
        Log.d("MainActivity", "Activity resumed")
    }

    override fun onPause() {
        super.onPause()
        startBackgroundService()
        Log.d("MainActivity", "Activity paused")
    }

    override fun onStop() {
        super.onStop()
        startBackgroundService()
        Log.d("MainActivity", "Activity stopped")
    }

    private fun startBackgroundService() {
        if (checkMicrophonePermission()) {
            val serviceIntent = Intent(this, SpeechRecognitionService::class.java)
            Log.d("MainActivity", "Starting background service")
            ContextCompat.startForegroundService(this, serviceIntent)
        } else {
            Log.e("MainActivity", "Microphone permission not granted")
        }
    }

    private fun stopBackgroundService() {
        val serviceIntent = Intent(this, SpeechRecognitionService::class.java)
        stopService(serviceIntent)
    }

    private fun checkMicrophonePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestMicrophonePermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.RECORD_AUDIO),
            REQUEST_MICROPHONE_PERMISSION
        )
    }

    companion object {
        private const val REQUEST_MICROPHONE_PERMISSION = 1
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_MICROPHONE_PERMISSION) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                Log.d("MainActivity", "Microphone permission granted")
            } else {
                Log.e("MainActivity", "Microphone permission denied")
            }
        }
    }
}

