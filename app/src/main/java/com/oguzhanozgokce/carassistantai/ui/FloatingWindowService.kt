package com.oguzhanozgokce.carassistantai.ui

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import com.oguzhanozgokce.carassistantai.MainActivity
import com.oguzhanozgokce.carassistantai.R
import com.oguzhanozgokce.carassistantai.common.gone
import com.oguzhanozgokce.carassistantai.common.visible

class FloatingWindowService : Service() {
    private var windowManager: WindowManager? = null
    private var floatingView: View? = null
    private var layoutParams: WindowManager.LayoutParams? = null
    private var relativeLayout: View? = null

    override fun onCreate() {
        super.onCreate()
        Log.e("FloatingWindowService", "Service started")

        try {
            windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
            val inflater = getSystemService(LAYOUT_INFLATER_SERVICE) as LayoutInflater
            floatingView = inflater.inflate(R.layout.floating_view_layout, null, false)
            relativeLayout = floatingView?.findViewById(R.id.relativeLayout)
            Log.e("FloatingWindowService", "Floating view inflated")

            layoutParams = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                android.graphics.PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.START
                x = 0
                y = 100
            }
            Log.e("FloatingWindowService", "LayoutParams created")

            // Floating view'i ekleyin
            windowManager?.addView(floatingView, layoutParams)
            Log.e("FloatingWindowService", "Floating view added")

            // Floating view'e tıklama ve kaydırma işlemlerini ekleyin
            floatingView?.setOnTouchListener(TouchListener())
            Log.e("FloatingWindowService", "TouchListener set")

            // Call startForegroundService here to ensure the service starts in the foreground
            startForegroundService()
            showMicButton()
        } catch (e: Exception) {
            Log.e("FloatingWindowService", "Error adding floating view", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        floatingView?.let {
            windowManager?.removeView(it)
            Log.e("FloatingWindowService", "Floating view removed")
        }
        hideMicButton()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startForegroundService() {
        try {
            val channelId = "floating_window_service"
            val channelName = "Floating Window Service"
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW)
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)

            val notification = NotificationCompat.Builder(this, channelId)
                .setContentTitle("Floating Window Service")
                .setContentText("Running")
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .build()

            startForeground(1, notification)
            Log.e("FloatingWindowService", "Foreground service started")
        } catch (e: Exception) {
            Log.e("FloatingWindowService", "Error starting foreground service", e)
        }
    }

    private inner class TouchListener : View.OnTouchListener {
        private var initialX: Int = 0
        private var initialY: Int = 0
        private var initialTouchX: Float = 0f
        private var initialTouchY: Float = 0f

        override fun onTouch(view: View, motionEvent: MotionEvent): Boolean {
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    initialX = layoutParams?.x ?: 0
                    initialY = layoutParams?.y ?: 0
                    initialTouchX = motionEvent.rawX
                    initialTouchY = motionEvent.rawY
                    Log.d("FloatingWindowService", "Touch down at ($initialTouchX, $initialTouchY)")
                    return true
                }
                MotionEvent.ACTION_MOVE -> {
                    layoutParams?.x = initialX + (motionEvent.rawX - initialTouchX).toInt()
                    layoutParams?.y = initialY + (motionEvent.rawY - initialTouchY).toInt()
                    windowManager?.updateViewLayout(floatingView, layoutParams)
                    Log.d("FloatingWindowService", "Touch move to (${layoutParams?.x}, ${layoutParams?.y})")
                    return true
                }
                MotionEvent.ACTION_UP -> {
                    view.performClick()
                    Log.d("FloatingWindowService", "Touch up")

                    // MainActivity'yi başlat ve mikrofon dinlemeyi başlat
                    val intent = Intent(this@FloatingWindowService, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    intent.putExtra("START_MIC", true)
                    startActivity(intent)

                    return true
                }
            }
            return false
        }
    }

    private fun showMicButton() {
        relativeLayout?.visible()
    }
    private fun hideMicButton() {
        relativeLayout?.gone()
    }
}