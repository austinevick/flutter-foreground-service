package com.example.flutter_foreground_service

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.location.Location
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.flutter_foreground_service.notification.NotificationHelper
import com.example.flutter_foreground_service.service.LocationForegroundService
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.android.FlutterFragmentActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodChannel
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date

class MainActivity : FlutterFragmentActivity() {
    private val CHANNEL = "flutter_foreground_service"

    private var exampleService: LocationForegroundService? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var serviceBoundState: Boolean = false
    private var latitude: String = ""
    private var longitude: String = ""


    // we need notification permission to be able to display a notification for the foreground service
    private val notificationPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) {
            // if permission was denied, the service can still run only the notification won't be visible
        }


    // we need location permission to be able to start the service
    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        when {
            permissions.getOrDefault(android.Manifest.permission.ACCESS_FINE_LOCATION, false) -> {
                // Precise location access granted, service can run
                startForegroundService()
            }

            permissions.getOrDefault(android.Manifest.permission.ACCESS_COARSE_LOCATION, false) -> {
                // Only approximate location access granted, service can still run.
                startForegroundService()
            }

            else -> {
                // No location access granted, service can't be started as it will crash
                Toast.makeText(this, "Location permission is required!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        EventChannel(flutterEngine.dartExecutor.binaryMessenger, "timer")
            .setStreamHandler(TransmitLocation(fusedLocationClient))

        MethodChannel(
            flutterEngine.dartExecutor.binaryMessenger,
            CHANNEL
        ).setMethodCallHandler { call, result ->
            when (call.method) {
                "startAndStopService" -> {
                    val notificationTitle = call.argument<String>("title")
                    val notificationText = call.argument<String>("text")
                    NotificationHelper.setNotificationText(notificationText!!, notificationTitle!!)
                    result.success(onStartOrStopForegroundServiceClick())
                    Log.d("asd","$latitude $longitude")
                }

                "getUserLocation" -> {
                    result.success("$latitude $longitude")
                }

                "shareLocation" -> {
                    val sendIntent: Intent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, "This is my text to send.")
                        type = "text/plain"
                    }
                    val shareIntent = Intent.createChooser(sendIntent, null)
                    startActivity(shareIntent)
                }

                else -> {
                    result.notImplemented()
                }
            }
        }
        checkAndRequestNotificationPermission()
        tryToBindToServiceIfRunning()
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(connection)
    }

    // needed to communicate with the service.
    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            // we've bound to ExampleLocationForegroundService, cast the IBinder and get ExampleLocationForegroundService instance.
            Log.d(TAG, "onServiceConnected")
            val binder = service as LocationForegroundService.LocalBinder
            exampleService = binder.getService()
            serviceBoundState = true
            onServiceConnected()
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            // This is called when the connection with the service has been disconnected. Clean up.
            Log.d(TAG, "onServiceDisconnected")
            serviceBoundState = false
            exampleService = null
        }
    }

    class TransmitLocation(val fusedLocationClient: FusedLocationProviderClient) :
        EventChannel.StreamHandler {
        private var handler = Handler(Looper.getMainLooper())

        override fun onListen(arguments: Any?, events: EventChannel.EventSink?) {
            val run: Runnable = object : Runnable {
                @SuppressLint("SimpleDateFormat", "MissingPermission")
                override fun run() {
                    handler.post {
                        val dateFormat = SimpleDateFormat("HH:mm:ss")
                        val time = dateFormat.format(Date())
                        fusedLocationClient.lastLocation
                            .addOnSuccessListener { location ->

                                events?.success("${location.latitude} ${location.longitude}")
                            }
                    }
                    handler.postDelayed(this, 1000)
                }
            }
            handler.postDelayed(run, 1000)
        }

        override fun onCancel(arguments: Any?) {

        }

    }

    private fun onStartOrStopForegroundServiceClick(): Boolean {
        if (exampleService == null) {
            // service is not yet running, start it after permission check
            locationPermissionRequest.launch(
                arrayOf(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
            return true
        } else {
            // service is already running, stop it
            exampleService?.stopForegroundService()
            return false
        }
    }


    /**
     * Creates and starts the ExampleLocationForegroundService as a foreground service.
     *
     * It also tries to bind to the service to update the UI with location updates.
     */
    private fun startForegroundService() {
        // start the service
        startForegroundService(Intent(this, LocationForegroundService::class.java))

        // bind to the service to update UI
        tryToBindToServiceIfRunning()
    }

    private fun tryToBindToServiceIfRunning() {
        Intent(this, LocationForegroundService::class.java).also { intent ->
            bindService(intent, connection, 0)
        }
        Log.d(TAG, "tryToBindToServiceIfRunning")
    }


    private fun onServiceConnected() {
        lifecycleScope.launch {
            // observe location updates from the service
            exampleService?.locationFlow?.map { location ->
                location
            }?.collectLatest {
                latitude = it?.latitude.toString()
                longitude = it?.longitude.toString()
            }
        }
    }


    /**
     * Check for notification permission before starting the service so that the notification is visible
     */
    private fun checkAndRequestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            when (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.POST_NOTIFICATIONS
            )) {
                android.content.pm.PackageManager.PERMISSION_GRANTED -> {
                    // permission already granted
                }

                else -> {
                    notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                }
            }
        }
    }


    companion object {
        private const val TAG = "MainActivity"
    }
}
