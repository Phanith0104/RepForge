package com.repforge.stepcounter

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.repforge.MainActivity
import com.repforge.data.repository.StepRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class StepCounterService : Service(), SensorEventListener {

    @Inject
    lateinit var repository: StepRepository

    private lateinit var sensorManager: SensorManager
    private var stepSensor: Sensor? = null
    private var accelSensor: Sensor? = null
    private val serviceScope = CoroutineScope(Dispatchers.IO)

    // Accelerometer variables for fallback
    private var isPeak = false
    private val stepThreshold = 13.5f // Magnitude threshold
    private var lastMagnitude = 0f

    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
        
        if (stepSensor != null) {
            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI)
        } else {
            // Fallback to Accelerometer for devices without hardware step counter
            accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            if (accelSensor != null) {
                sensorManager.registerListener(this, accelSensor, SensorManager.SENSOR_DELAY_GAME)
            }
        }
        
        startForegroundService()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
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
            .build()

        startForeground(1, notification)
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
            // Magnitude-based peak detection fallback
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            
            val magnitude = kotlin.math.sqrt(x * x + y * y + z * z)
            
            if (magnitude > stepThreshold && !isPeak && magnitude > lastMagnitude) {
                isPeak = true
                serviceScope.launch {
                    val currentSteps = repository.incrementStepManually()
                    updateNotification(currentSteps)
                }
            } else if (magnitude < stepThreshold - 1f) {
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
                .build()

            val manager = getSystemService(NotificationManager::class.java)
            manager.notify(1, notification)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
        serviceScope.cancel()
    }
}
