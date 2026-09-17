package com.repforge.stepcounter

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.content.pm.ServiceInfo
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.repforge.MainActivity
import com.repforge.data.repository.StepRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import javax.inject.Inject

@AndroidEntryPoint
class StepCounterService : Service(), SensorEventListener {

    @Inject
    lateinit var repository: StepRepository

    private var sensorManager: SensorManager? = null
    private var stepSensor: Sensor? = null
    private var accelSensor: Sensor? = null
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    // Accelerometer variables for fallback
    private var isPeak = false
    private val stepThreshold = 14.5f // Higher threshold for less noise
    private var lastMagnitude = 0f
    private var lastPeakTime = 0L
    private val minStepInterval = 300L // 300ms between steps to avoid double counting
    
    // Low-pass filter variables
    private var gravity = FloatArray(3)
    private val alpha = 0.9f // Stronger filter

    override fun onCreate() {
        super.onCreate()
        Log.d("StepCounterService", "Service Created")
        
        // Ensure repository is injected
        if (!::repository.isInitialized) {
            Log.e("StepCounterService", "Repository not initialized!")
            stopSelf()
            return
        }

        // Permission check for Activity Recognition on Android 10+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (checkSelfPermission(Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
                Log.e("StepCounterService", "Activity Recognition permission not granted. Stopping service.")
                stopSelf()
                return
            }
        }

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        
        if (stepSensor != null) {
            Log.d("StepCounterService", "Using Hardware Step Counter")
            sensorManager?.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI)
        } else {
            Log.d("StepCounterService", "Hardware Step Counter not available. Falling back to Accelerometer.")
            accelSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            if (accelSensor != null) {
                sensorManager?.registerListener(this, accelSensor, SensorManager.SENSOR_DELAY_GAME)
            } else {
                Log.e("StepCounterService", "No suitable sensor found!")
            }
        }
        
        startForegroundService()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("StepCounterService", "Service Started")
        startForegroundService()
        return START_STICKY
    }

    private fun startForegroundService(steps: Int = 0) {
        val channelId = "step_counter_channel"
        val channel = NotificationChannel(
            channelId,
            "Step Counter Service",
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("RepForge Step Counter")
            .setContentText("Today's Steps: $steps")
            .setSmallIcon(android.R.drawable.ic_menu_directions)
            .setContentIntent(pendingIntent)
            .setOnlyAlertOnce(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .build()

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                ServiceCompat.startForeground(
                    this,
                    1,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH
                )
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH)
            } else {
                startForeground(1, notification)
            }
        } catch (e: Exception) {
            Log.e("StepCounterService", "Error starting foreground service", e)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        if (event.sensor.type == Sensor.TYPE_STEP_COUNTER) {
            val totalSensorSteps = event.values[0].toInt()
            serviceScope.launch {
                val todaySteps = repository.updateSteps(totalSensorSteps)
                updateNotification(todaySteps)
            }
        } else if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            // Apply low-pass filter
            gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0]
            gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1]
            gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2]

            val x = event.values[0] - gravity[0]
            val y = event.values[1] - gravity[1]
            val z = event.values[2] - gravity[2]
            
            val magnitude = kotlin.math.sqrt(x * x + y * y + z * z)
            val currentTime = System.currentTimeMillis()
            
            if (magnitude > stepThreshold && !isPeak && magnitude > lastMagnitude) {
                if (currentTime - lastPeakTime > minStepInterval) {
                    isPeak = true
                    lastPeakTime = currentTime
                    serviceScope.launch {
                        val currentSteps = repository.incrementStepManually()
                        updateNotification(currentSteps)
                    }
                }
            } else if (magnitude < stepThreshold - 2.0f) {
                isPeak = false
            }
            lastMagnitude = magnitude
        }
    }

    private fun updateNotification(steps: Int) {
        serviceScope.launch {
            val record = repository.getTodaySteps().first() // Get current stats
            
            val distance = record?.distanceKm ?: 0f
            val calories = record?.caloriesBurned ?: 0

            val notificationIntent = Intent(this@StepCounterService, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                this@StepCounterService, 0, notificationIntent,
                PendingIntent.FLAG_IMMUTABLE
            )

            val notification = NotificationCompat.Builder(this@StepCounterService, "step_counter_channel")
                .setContentTitle("RepForge Step Counter")
                .setContentText("Steps: $steps | ${"%.2f".format(distance)} km | $calories kcal")
                .setSmallIcon(android.R.drawable.ic_menu_directions)
                .setContentIntent(pendingIntent)
                .setOnlyAlertOnce(true)
                .setOngoing(true)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .build()

            val manager = getSystemService(NotificationManager::class.java)
            manager.notify(1, notification)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        Log.d("StepCounterService", "Service Destroyed")
        sensorManager?.unregisterListener(this)
        serviceScope.cancel()
    }
}
