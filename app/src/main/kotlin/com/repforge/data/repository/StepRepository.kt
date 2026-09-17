package com.repforge.data.repository

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorManager
import androidx.core.content.edit
import com.repforge.data.local.dao.StepDao
import com.repforge.data.local.entities.StepEntity
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.minutes

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Singleton
class StepRepository @Inject constructor(
    private val stepDao: StepDao,
    private val firebaseAuth: FirebaseAuth,
    @ApplicationContext private val context: Context
) {
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val prefs = context.getSharedPreferences("step_prefs", Context.MODE_PRIVATE)
    private val mutex = Mutex()

    private val dateFlow = flow {
        while (true) {
            emit(getTodayDate())
            delay(1.minutes)
        }
    }.distinctUntilChanged()

    private fun getCurrentUserId(): String = firebaseAuth.currentUser?.uid ?: "guest"

    fun getTodayDate(): String = LocalDate.now().format(dateFormatter)

    fun isStepCounterAvailable(): Boolean {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        return sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) != null
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun getTodaySteps(): Flow<StepEntity?> {
        return dateFlow.flatMapLatest { today ->
            stepDao.getStepsForDateFlow(today, getCurrentUserId())
        }
    }

    fun getStepGoal(): Int = prefs.getInt("step_goal_${getCurrentUserId()}", 10000)

    fun updateStepGoal(goal: Int) {
        prefs.edit { putInt("step_goal_${getCurrentUserId()}", goal) }
    }

    fun isStepTrackingEnabled(): Boolean = prefs.getBoolean("step_tracking_enabled", true)

    fun setStepTrackingEnabled(enabled: Boolean) {
        prefs.edit { putBoolean("step_tracking_enabled", enabled) }
    }

    suspend fun updateSteps(sensorSteps: Int): Int = mutex.withLock {
        val today = getTodayDate()
        val userId = getCurrentUserId()
        val lastUpdateDate = prefs.getString("last_update_date_$userId", "")
        val lastSensorValue = prefs.getInt("last_sensor_value_$userId", sensorSteps)
        val stepGoal = getStepGoal()
        
        // Detect Day Change or Reboot
        if (lastUpdateDate != today) {
            // New day: set the current sensor value as the base for today
            prefs.edit {
                putInt("sensor_base_steps_$userId", sensorSteps)
                putString("last_update_date_$userId", today)
                putInt("last_sensor_value_$userId", sensorSteps)
            }
        } else if (sensorSteps < lastSensorValue) {
            // Reboot detected
            val currentEntry = stepDao.getStepsForDate(today, userId)
            val todayCountBeforeReboot = currentEntry?.count ?: 0
            val newBase = sensorSteps - todayCountBeforeReboot
            
            prefs.edit {
                putInt("sensor_base_steps_$userId", newBase)
                putInt("last_sensor_value_$userId", sensorSteps)
            }
        } else {
            prefs.edit {
                putInt("last_sensor_value_$userId", sensorSteps)
            }
        }

        val baseSteps = prefs.getInt("sensor_base_steps_$userId", sensorSteps)
        val todayCount = (sensorSteps - baseSteps).coerceAtLeast(0)

        // Calculations
        val distanceKm = todayCount * 0.000762f // 0.762m avg stride
        val calories = (todayCount * 0.04f).toInt()
        val activeTimeMinutes = (todayCount / 80)

        // Achievement Logic
        val personalBest = stepDao.getPersonalBestExcludingToday(today, userId) ?: 0
        val yesterday = getYesterdayDate()
        val yesterdayEntry = stepDao.getStepsForDate(yesterday, userId)
        val yesterdayCount = yesterdayEntry?.count ?: 0
        
        val isAchievement = (todayCount > personalBest || todayCount > yesterdayCount) && todayCount > 0

        // Streak Logic: Strictly consecutive calendar days
        val newStreak = if (todayCount >= stepGoal) {
            // If today is a hit, the streak is calculated including today.
            // But since we are updating today, we should calculate what the streak would be.
            // Actually, calculateCurrentStreak already checks today.
            // But we haven't inserted today's new value yet!
            
            // Let's do a simplified check for the insertion value:
            val yesterday = getYesterdayDate()
            val yesterdayEntry = stepDao.getStepsForDate(yesterday, userId)
            val yesterdayCount = yesterdayEntry?.count ?: 0
            
            if (yesterdayCount >= stepGoal) {
                // If yesterday was a hit, we look at yesterday's stored streak and add 1.
                // This assumes yesterday's streak was correct.
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
                userId = userId,
                count = todayCount,
                distanceKm = distanceKm,
                caloriesBurned = calories,
                activeTimeMinutes = activeTimeMinutes,
                streak = newStreak,
                isAchievement = isAchievement
            )
        )
        return@withLock todayCount
    }

    suspend fun incrementStepManually(): Int = mutex.withLock {
        val today = getTodayDate()
        val userId = getCurrentUserId()
        val currentEntry = stepDao.getStepsForDate(today, userId)
        val newCount = (currentEntry?.count ?: 0) + 1
        val stepGoal = getStepGoal()
        
        val personalBest = stepDao.getPersonalBestExcludingToday(today, userId) ?: 0
        val yesterday = getYesterdayDate()
        val yesterdayEntry = stepDao.getStepsForDate(yesterday, userId)
        val yesterdayCount = yesterdayEntry?.count ?: 0
        
        val isAchievement = (newCount > personalBest || newCount > yesterdayCount) && newCount > 0
        
        val newStreak = if (newCount >= stepGoal) {
            if (yesterdayCount >= stepGoal) (yesterdayEntry?.streak ?: 0) + 1 else 1
        } else 0

        val distanceKm = newCount * 0.000762f
        val calories = (newCount * 0.04f).toInt()
        val activeTimeMinutes = (newCount / 80)

        stepDao.insertOrUpdateSteps(
            StepEntity(
                date = today,
                userId = userId,
                count = newCount,
                distanceKm = distanceKm,
                caloriesBurned = calories,
                activeTimeMinutes = activeTimeMinutes,
                streak = newStreak,
                isAchievement = isAchievement
            )
        )
        return@withLock newCount
    }

    private fun getYesterdayDate(): String = LocalDate.now().minusDays(1).format(dateFormatter)

    fun getAllStepHistory(): Flow<List<StepEntity>> = stepDao.getAllSteps(getCurrentUserId())

    suspend fun deleteHistoryRecord(date: String) {
        val userId = getCurrentUserId()
        stepDao.deleteStepsForDate(date, userId)
        // Streak is calculated on the fly now, so no need to update all records
    }

    suspend fun calculateCurrentStreak(): Int {
        val userId = getCurrentUserId()
        val allHistory = stepDao.getAllStepsList(userId)
            .sortedByDescending { it.date } // Newest first
        val stepGoal = getStepGoal()
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)
        
        // Find today's and yesterday's entries
        val todayEntry = allHistory.find { it.date == today.toString() }
        val yesterdayEntry = allHistory.find { it.date == yesterday.toString() }

        val todayHit = todayEntry != null && todayEntry.count >= stepGoal
        val yesterdayHit = yesterdayEntry != null && yesterdayEntry.count >= stepGoal

        var streak = 0
        var checkDate: LocalDate

        if (todayHit) {
            streak = 1
            checkDate = yesterday
            // Check backwards from yesterday
            while (true) {
                val entry = allHistory.find { it.date == checkDate.toString() }
                if (entry != null && entry.count >= stepGoal) {
                    streak++
                    checkDate = checkDate.minusDays(1)
                } else {
                    break
                }
            }
        } else if (yesterdayHit) {
            // Today not hit, but yesterday was. The streak is what it was yesterday.
            streak = 1
            checkDate = yesterday.minusDays(1)
            while (true) {
                val entry = allHistory.find { it.date == checkDate.toString() }
                if (entry != null && entry.count >= stepGoal) {
                    streak++
                    checkDate = checkDate.minusDays(1)
                } else {
                    break
                }
            }
        }
        
        return streak
    }
}
