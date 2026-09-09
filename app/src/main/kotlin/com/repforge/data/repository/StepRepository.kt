package com.repforge.data.repository

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import androidx.core.content.edit
import com.repforge.data.local.dao.StepDao
import com.repforge.data.local.entities.StepEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.minutes

@Singleton
class StepRepository @Inject constructor(
    private val stepDao: StepDao,
    @ApplicationContext private val context: Context
) {
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val prefs = context.getSharedPreferences("step_prefs", Context.MODE_PRIVATE)

    private val dateFlow = flow {
        while (true) {
            emit(getTodayDate())
            delay(1.minutes)
        }
    }.distinctUntilChanged()

    fun getTodayDate(): String = LocalDate.now().format(dateFormatter)

    fun isStepCounterAvailable(): Boolean {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        return sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) != null
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getTodaySteps(): Flow<StepEntity?> {
        return dateFlow.flatMapLatest { today ->
            stepDao.getStepsForDateFlow(today)
        }
    }

    fun getStepGoal(): Int = prefs.getInt("step_goal", 10000)

    fun updateStepGoal(goal: Int) {
        prefs.edit { putInt("step_goal", goal) }
    }

    suspend fun updateSteps(sensorSteps: Int): Int {
        val today = getTodayDate()
        val lastUpdateDate = prefs.getString("last_update_date", "")
        val lastSensorValue = prefs.getInt("last_sensor_value", sensorSteps)
        val stepGoal = getStepGoal()
        
        // Detect Day Change or Reboot
        if (lastUpdateDate != today) {
            // New day: set the current sensor value as the base for today
            prefs.edit {
                putInt("sensor_base_steps", sensorSteps)
                putString("last_update_date", today)
                putInt("last_sensor_value", sensorSteps)
            }
        } else if (sensorSteps < lastSensorValue) {
            // Reboot detected
            val currentEntry = stepDao.getStepsForDate(today)
            val todayCountBeforeReboot = currentEntry?.count ?: 0
            val newBase = sensorSteps - todayCountBeforeReboot
            
            prefs.edit {
                putInt("sensor_base_steps", newBase)
                putInt("last_sensor_value", sensorSteps)
            }
        } else {
            prefs.edit {
                putInt("last_sensor_value", sensorSteps)
            }
        }

        val baseSteps = prefs.getInt("sensor_base_steps", sensorSteps)
        val todayCount = (sensorSteps - baseSteps).coerceAtLeast(0)

        // Calculations
        val distanceKm = todayCount * 0.000762f // 0.762m avg stride
        val calories = (todayCount * 0.04f).toInt()
        val activeMinutes = (todayCount / 80) // Slightly more realistic: ~80 steps/min

        // Achievement Logic
        val personalBest = stepDao.getPersonalBestExcludingToday(today) ?: 0
        val yesterday = getYesterdayDate()
        val yesterdayEntry = stepDao.getStepsForDate(yesterday)
        val yesterdayCount = yesterdayEntry?.count ?: 0
        
        val isAchievement = (todayCount > personalBest || todayCount > yesterdayCount) && todayCount > 0

        // Streak Logic
        val newStreak = if (todayCount >= stepGoal) {
            if (yesterdayCount >= stepGoal) {
                (yesterdayEntry?.streak ?: 0) + 1
            } else {
                1
            }
        } else {
            0
        }

        stepDao.insertOrUpdateSteps(
            StepEntity(
                date = today,
                count = todayCount,
                distanceKm = distanceKm,
                caloriesBurned = calories,
                activeTimeMinutes = activeMinutes,
                streak = newStreak,
                isAchievement = isAchievement
            )
        )
        return todayCount
    }

    suspend fun incrementStepManually(): Int {
        val today = getTodayDate()
        val currentEntry = stepDao.getStepsForDate(today)
        val newCount = (currentEntry?.count ?: 0) + 1
        val stepGoal = getStepGoal()
        
        // We use the same update logic but just increment by 1
        // Note: For accelerometer fallback, baseSteps logic is skipped as we are counting increments.
        
        // Achievement & Streak Logic (Simplified for increment)
        val personalBest = stepDao.getPersonalBestExcludingToday(today) ?: 0
        val yesterday = getYesterdayDate()
        val yesterdayEntry = stepDao.getStepsForDate(yesterday)
        val isAchievement = (newCount > personalBest || newCount > (yesterdayEntry?.count ?: 0)) && newCount > 0
        
        val newStreak = if (newCount >= stepGoal) {
            if ((yesterdayEntry?.count ?: 0) >= stepGoal) (yesterdayEntry?.streak ?: 0) + 1 else 1
        } else 0

        val distanceKm = newCount * 0.000762f
        val calories = (newCount * 0.04f).toInt()
        val activeMinutes = (newCount / 80)

        stepDao.insertOrUpdateSteps(
            StepEntity(
                date = today,
                count = newCount,
                distanceKm = distanceKm,
                caloriesBurned = calories,
                activeTimeMinutes = activeMinutes,
                streak = newStreak,
                isAchievement = isAchievement
            )
        )
        return newCount
    }

    private fun getYesterdayDate(): String = LocalDate.now().minusDays(1).format(dateFormatter)

    fun getAllStepHistory(): Flow<List<StepEntity>> = stepDao.getAllSteps()
}
