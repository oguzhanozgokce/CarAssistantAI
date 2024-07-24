package com.oguzhanozgokce.carassistantai

import android.app.PictureInPictureParams
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import android.util.Rational
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.oguzhanozgokce.carassistantai.databinding.ActivityMainBinding
import com.oguzhanozgokce.carassistantai.ui.PiPModeListener

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

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
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        enterPictureInPictureModeIfNeeded()
    }

    override fun onStop() {
        super.onStop()
        //enterPictureInPictureModeIfNeeded()
    }

    private fun enterPictureInPictureModeIfNeeded() {
        val aspectRatio = Rational(9, 16)
        val pipParams = PictureInPictureParams.Builder()
            .setAspectRatio(aspectRatio)
            .build()
        enterPictureInPictureMode(pipParams)
    }

    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration
    ) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        callPiPModeListener(isInPictureInPictureMode)
    }

    private fun callPiPModeListener(isInPictureInPictureMode: Boolean) {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val fragment = navHostFragment.childFragmentManager.primaryNavigationFragment
        if (fragment is PiPModeListener) {
            fragment.onPictureInPictureModeChanged(isInPictureInPictureMode)
        } else {
            Log.e("MainActivity", "Fragment is not PiPModeListener")
        }
    }
}

