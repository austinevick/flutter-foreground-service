package com.example.flutter_foreground_service.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.example.flutter_foreground_service.service.LocationForegroundService

class MyBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val stopServiceIntent = intent?.getStringExtra("STOP")
        if (stopServiceIntent != null) {
            val serviceIntent = Intent(context, LocationForegroundService::class.java)
            context?.stopService(serviceIntent)
            Toast.makeText(context, "Foreground Service stopped", Toast.LENGTH_SHORT).show()
        }

        if (intent != null) {
            if (intent.action.equals(Intent.ACTION_BOOT_COMPLETED)) {
                val serviceIntent = Intent(context, LocationForegroundService::class.java)
                context?.startForegroundService(serviceIntent)
                Log.d("Receiver", "onReceive:Foreground service started")
            }
        }
    }
}