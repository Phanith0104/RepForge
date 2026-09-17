package com.repforge.stepcounter

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import com.repforge.data.repository.StepRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var repository: StepRepository

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("BootReceiver", "Received intent: ${intent.action}")
        
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            try {
                // Check if step tracking is enabled AND if we have the necessary permissions
                val hasActivityPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    context.checkSelfPermission(Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_GRANTED
                } else true

                if (repository.isStepTrackingEnabled() && hasActivityPermission) {
                    Log.d("BootReceiver", "Step tracking enabled and permission granted. Attempting to start service.")
                    val serviceIntent = Intent(context, StepCounterService::class.java)
                    
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        try {
                            context.startForegroundService(serviceIntent)
                        } catch (e: Exception) {
                            Log.e("BootReceiver", "Background start restriction: ${e.message}")
                            // Fallback: If it fails, the service will start next time the user opens the app
                        }
                    } else {
                        context.startService(serviceIntent)
                    }
                } else {
                    Log.d("BootReceiver", "Step tracking disabled or permission missing. Skipping service start.")
                }
            } catch (e: Exception) {
                Log.e("BootReceiver", "Critical error in BootReceiver", e)
            }
        }
    }
}
