package com.example.flutter_foreground_service.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.flutter_foreground_service.MainActivity
import com.example.flutter_foreground_service.R
import com.example.flutter_foreground_service.receiver.MyBroadcastReceiver
import kotlinx.coroutines.flow.MutableStateFlow

internal object NotificationHelper {
    private const val NOTIFICATION_CHANNEL_ID = "general_notification_channel"

    private val _notificationTextFlow = MutableStateFlow("Hello")
    val notificationTextFlow: MutableStateFlow<String> = _notificationTextFlow

    private val _notificationTitleFlow = MutableStateFlow("Hi")
    val notificationTitleFlow: MutableStateFlow<String> = _notificationTitleFlow

    fun setNotificationText(notificationText: String, notificationTitle: String) {
        notificationTextFlow.value = notificationText
        _notificationTextFlow.value = notificationText
        Log.d("text", "setNotificationText: $notificationText")
        Log.d("title", "setNotificationText: $notificationTitle")
    }

    fun createNotificationChannel(context: Context) {
        val notificationManager =
            context.getSystemService(Service.NOTIFICATION_SERVICE) as NotificationManager

        // create the notification channel
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            "General notification channel",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationManager.createNotificationChannel(channel)
    }

    fun buildNotification(
        context: Context
    ): Notification {
        val intent = Intent(context, MyBroadcastReceiver::class.java).apply {
            putExtra("STOP","STOP")
        }

        val pendingIntent = PendingIntent.getBroadcast(context,0,intent,PendingIntent.FLAG_IMMUTABLE)

        return NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(_notificationTitleFlow.value)
            .setContentText(_notificationTextFlow.value)
            .setSmallIcon(R.drawable.launch_background)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .setContentIntent(Intent(context, MainActivity::class.java).let { notificationIntent ->
                PendingIntent.getActivity(
                    context,
                    0,
                    notificationIntent,
                    PendingIntent.FLAG_IMMUTABLE
                )
            })
            .addAction(0,"STOP",pendingIntent)
            .build()
    }

}