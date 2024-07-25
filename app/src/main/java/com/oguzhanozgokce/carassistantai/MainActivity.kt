package com.oguzhanozgokce.carassistantai

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import androidx.activity.enableEdgeToEdge
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.oguzhanozgokce.carassistantai.ui.FloatingWindowService
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.oguzhanozgokce.carassistantai.databinding.ActivityMainBinding
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import com.oguzhanozgokce.carassistantai.ui.chat.view.OverlayPermissionDialogFragment

class MainActivity : AppCompatActivity() , OverlayPermissionDialogFragment.OverlayPermissionDialogListener{
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    private val overlayPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (Settings.canDrawOverlays(this)) {
            Log.e("MainActivity", "Overlay permission granted")
        } else {
            Toast.makeText(
                this,
                "Overlay permission is needed to display the floating window",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom)
            insets
        }
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        binding.root.setBackgroundColor(resources.getColor(R.color.background_color, null))

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // İzin kontrolü
        if (!Settings.canDrawOverlays(this)) {
            showOverlayPermissionDialog()
        }
    }

    private fun showOverlayPermissionDialog() {
        val dialog = OverlayPermissionDialogFragment()
        dialog.listener = this
        dialog.show(supportFragmentManager, "OverlayPermissionDialog")
    }

    private fun requestOverlayPermission() {
        val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
        overlayPermissionLauncher.launch(intent)
    }

    private fun startFloatingService() {
        val startServiceIntent = Intent(this, FloatingWindowService::class.java)
        ContextCompat.startForegroundService(this, startServiceIntent)
        Log.e("MainActivity", "Floating window service started")
    }

    private fun stopFloatingService() {
        val stopServiceIntent = Intent(this, FloatingWindowService::class.java)
        stopService(stopServiceIntent)
        Log.e("MainActivity", "Floating window service stopped")
    }


    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun onStart() {
        super.onStart()
        stopFloatingService()
        //handleIntent(intent)
        Log.e("MainActivity", "onResume")
    }

    override fun onStop() {
        super.onStop()
        if (Settings.canDrawOverlays(this)) {
            startFloatingService()
        }
        Log.e("MainActivity", "onPause")
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        //handleIntent(intent)
        Log.e("MainActivity", "onNewIntent")
    }

    override fun onDestroy() {
        super.onDestroy()
        stopFloatingService()
    }

    override fun onGrantPermissionClicked() {
        requestOverlayPermission()
    }

    override fun onNoGrantPermissionClicked() {
        Toast.makeText(
            this,
            "Overlay permission is needed to display the floating window",
            Toast.LENGTH_SHORT
        ).show()
    }

//    private fun handleIntent(intent: Intent?) {
//        intent?.let {
//            if (it.getBooleanExtra("START_MIC", false)) {
//                val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
//                val fragment = navHostFragment.childFragmentManager.primaryNavigationFragment
//                if (fragment is ChatBotFragment) {
//                    //fragment.startVoiceInput()
//                    Log.e("MainActivity", "ChatBotFragment found, start voice input")
//                } else {
//                    Log.e("MainActivity", "ChatBotFragment not found")
//                }
//            }
//        }
//    }
}
