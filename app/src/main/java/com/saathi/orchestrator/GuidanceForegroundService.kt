package com.saathi.orchestrator

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder

/** Keeps a visible, user-controlled guidance session alive while the user switches apps. */
class GuidanceForegroundService : Service() {
    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            (getSystemService(NotificationManager::class.java)).createNotificationChannel(
                NotificationChannel(CHANNEL, "Saathi guidance", NotificationManager.IMPORTANCE_LOW)
            )
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notification = android.app.Notification.Builder(this, CHANNEL)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Saathi guidance is active")
            .setContentText("Saathi is waiting for the next supported screen. Tap Stop to end guidance.")
            .setOngoing(true)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "Stop", Intent(this, GuidanceForegroundService::class.java).setAction(ACTION_STOP).let {
                android.app.PendingIntent.getService(this, 1, it, android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE)
            })
            .build()
        if (intent?.action == ACTION_STOP) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        } else startForeground(NOTIFICATION_ID, notification)
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val CHANNEL = "saathi_guidance"
        private const val NOTIFICATION_ID = 43
        private const val ACTION_STOP = "com.saathi.action.STOP_GUIDANCE"
        fun start(context: Context) {
            val intent = Intent(context, GuidanceForegroundService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) context.startForegroundService(intent) else context.startService(intent)
        }
        fun stop(context: Context) = context.stopService(Intent(context, GuidanceForegroundService::class.java))
    }
}
