package com.oguzhanozgokce.carassistantai

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.oguzhanozgokce.carassistantai.databinding.ActivityMainBinding
import com.oguzhanozgokce.carassistantai.ui.chat.helper.VoiceAssistantService
import com.oguzhanozgokce.carassistantai.ui.chat.view.ChatBotFragment


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private val REQUEST_CODE_PERMISSIONS = 102
    private val REQUEST_CODE_OVERLAY_PERMISSION = 101

    private val commandReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == "processedCommand") {
                val command = intent.getStringExtra("command")
                command?.let {
                    Log.d("MainActivity", "Received command: $it")
                    handleCommand(it)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Gerekli izinleri kontrol edin ve isteyin
        if (!checkPermissions()) {
            requestPermissions()
        } else {
            startVoiceAssistantService()
        }

        requestOverlayPermission()

        val filter = IntentFilter("processedCommand")
        ContextCompat.registerReceiver(
            this,
            commandReceiver,
            filter,
            ContextCompat.RECEIVER_EXPORTED
        )
    }

    private fun handleCommand(command: String) {

        val chatBotFragment =
            supportFragmentManager.findFragmentByTag(ChatBotFragment::class.java.simpleName)

        if (chatBotFragment != null && chatBotFragment.isAdded) {
            Log.d("MainActivity", "Received command: $command")
            (chatBotFragment as ChatBotFragment).receiveVoiceCommand(command)
        } else {
            Log.d("MainActivity", "Launching ChatBotFragment")
            val fragment = ChatBotFragment().apply {
                arguments = Bundle().apply {
                    putString("COMMAND", command)
                }
            }
            supportFragmentManager.beginTransaction()
                .replace(R.id.nav_host_fragment, fragment, ChatBotFragment::class.java.simpleName)
                .addToBackStack(null)
                .commitAllowingStateLoss()
        }
    }





    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private fun checkPermissions(): Boolean {
        val microphonePermission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        val foregroundServiceMicrophonePermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.FOREGROUND_SERVICE_MICROPHONE
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }

        return microphonePermission && foregroundServiceMicrophonePermission
    }

    private fun requestPermissions() {
        val permissions = mutableListOf(Manifest.permission.RECORD_AUDIO)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.FOREGROUND_SERVICE_MICROPHONE)
        }

        ActivityCompat.requestPermissions(
            this,
            permissions.toTypedArray(),
            REQUEST_CODE_PERMISSIONS
        )
    }

    private fun requestOverlayPermission() {
        if (!Settings.canDrawOverlays(this)) {
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivityForResult(intent, REQUEST_CODE_OVERLAY_PERMISSION)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                startVoiceAssistantService()
            } else {
                Log.e("MainActivity", "Gerekli izinler verilmedi")
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(commandReceiver)
        stopVoiceAssistantService()
    }

    private fun startVoiceAssistantService() {
        val intent = Intent(this, VoiceAssistantService::class.java)
        intent.putExtra("input", "Start listening")
        startService(intent)
    }

    private fun stopVoiceAssistantService() {
        val intent = Intent(this, VoiceAssistantService::class.java)
        stopService(intent)
    }
}
