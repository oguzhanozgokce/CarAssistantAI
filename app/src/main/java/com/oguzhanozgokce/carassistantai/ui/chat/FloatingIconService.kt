package com.oguzhanozgokce.carassistantai.ui.chat

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.*
import androidx.core.app.NotificationCompat
import com.oguzhanozgokce.carassistantai.MainActivity
import com.oguzhanozgokce.carassistantai.R

class FloatingIconService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var floatingView: View

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        startForegroundService()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager

        floatingView = LayoutInflater.from(this).inflate(R.layout.floating_icon, null)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

        params.x = 0
        params.y = 100
        params.gravity = Gravity.TOP or Gravity.START

        windowManager.addView(floatingView, params)

        val iconView = floatingView.findViewById<CustomImageView>(R.id.floating_icon)

        iconView.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f

            override fun onTouch(v: View?, event: MotionEvent): Boolean {
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = params.x
                        initialY = params.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        if (kotlin.math.abs(event.rawX - initialTouchX) < 10 && kotlin.math.abs(
                                event.rawY - initialTouchY
                            ) < 10
                        ) {
                            v?.performClick()
                            val intent =
                                Intent(this@FloatingIconService, MainActivity::class.java).apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                            startActivity(intent)
                            stopSelf()
                        }
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        params.x = initialX + (event.rawX - initialTouchX).toInt()
                        params.y = initialY + (event.rawY - initialTouchY).toInt()
                        windowManager.updateViewLayout(floatingView, params)
                        return true
                    }
                }
                return false
            }
        })

        iconView.setOnClickListener {
            val intent = Intent(this@FloatingIconService, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            startActivity(intent)
            stopSelf()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::floatingView.isInitialized) {
            windowManager.removeView(floatingView)
        }
    }

    private fun startForegroundService() {
        val notificationChannelId = "FOREGROUND_SERVICE_CHANNEL"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                notificationChannelId,
                "Foreground Service Channel",
                NotificationManager.IMPORTANCE_LOW
            )
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(this, notificationChannelId)
            .setContentTitle("Floating Icon Service")
            .setContentText("Service is running")
            .setSmallIcon(R.drawable.chat_gpt_icon)
            .build()

        startForeground(1, notification)
    }
}
