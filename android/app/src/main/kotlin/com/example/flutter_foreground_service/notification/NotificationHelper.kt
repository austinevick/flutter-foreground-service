package com.example.flutter_foreground_service.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.flutter_foreground_service.MainActivity
import com.example.flutter_foreground_service.R
import kotlinx.coroutines.flow.MutableStateFlow

internal object NotificationHelper {
    private const val NOTIFICATION_CHANNEL_ID = "general_notification_channel"

     private val _notificationTextFlow = MutableStateFlow("Hello")
    private val _notificationTitleFlow = MutableStateFlow("Hi")
    val notificationTitleFlow: MutableStateFlow<String> = _notificationTitleFlow
    val notificationTextFlow: MutableStateFlow<String> = _notificationTextFlow
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
        return NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("Foreground service")
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
            .build()
    }

}